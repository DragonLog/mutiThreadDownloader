package com.zcx.mutiProcessDownloader.core;

import com.zcx.mutiProcessDownloader.constant.Constant;
import com.zcx.mutiProcessDownloader.util.FileUtils;
import com.zcx.mutiProcessDownloader.util.HttpUtils;
import com.zcx.mutiProcessDownloader.util.LogUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Downloader {

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);  //该线程池用于周期性输出下载信息
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Constant.THREAD_NUM, Constant.THREAD_NUM, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(Constant.THREAD_NUM)); //该线程池用于多线程下载
    private static CountDownLatch countDownLatch = new CountDownLatch(Constant.THREAD_NUM);  //该对象用于阻塞调用线程直到自减为0

    public static void download(String url) {
        String httpFileName = HttpUtils.getHttpFileName(url);
        httpFileName = Constant.PATH + httpFileName;    //组合成为本地文件名

        long fileContentLength = FileUtils.getFileContentLength(httpFileName);  //获取本地文件大小

        HttpURLConnection httpURLConnection = HttpUtils.getHttpURLConnection(url);

        long contentLength = httpURLConnection.getContentLength();  //获取要下载的文件大小

        if (fileContentLength == contentLength) {
            LogUtils.info("{}已经下载完毕，无需重新下载", httpFileName);
            return;
        }

        DownLoadInfoThread downLoadInfoThread = new DownLoadInfoThread(contentLength);
        scheduledExecutorService.scheduleAtFixedRate(downLoadInfoThread, 1000, 500, TimeUnit.MILLISECONDS);  //该方法执行的线程可能不会抛出任何异常(“吃异常”)

        try {
            ArrayList<Future> list = new ArrayList<>();  //创建一个集合存储线程执行后返回的信息
            split(url, list);  //拆分文件交给多线程下载
            try {
                countDownLatch.await();  //在前面的线程完成下载前一直阻塞在这里
            } catch (InterruptedException e) {
                LogUtils.error("中断异常");
                e.printStackTrace();
            }
            if (merge(httpFileName)) {  //下载好的文件合并成功则清理之前的临时文件
                clearTemp(httpFileName);
            }
        } finally {  //关闭链接和线程池
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            scheduledExecutorService.shutdown();
            threadPoolExecutor.shutdown();
        }
    }

    public static void split(String url, ArrayList<Future> futureArrayList) {
        long httpFileConnectionLength = HttpUtils.getHttpFileConnectionLength(url);
        String fileName = HttpUtils.getHttpFileName(url);
        long size = httpFileConnectionLength / Constant.THREAD_NUM;  //确定拆分后每个文件的大小
        long finishedSize = getFinishedSize(Constant.PATH + fileName);  //获取下载前就已经下载的文件总大小
        DownLoadInfoThread.downSize.add(finishedSize);
        for (int i = 0; i < Constant.THREAD_NUM; i ++) {  //每个线程得到一个区间的下载任务
            File file = new File(Constant.PATH + fileName + ".temp" + i);  //操作需要断点续传的文件
            long startPos;
            long endPos;
            if ( !(finishedSize > 0)) { //finishedSize < 0说明不需要进行断点续传，从0开始下载文件
                startPos = i * size;
            } else { //finishedSize > 0说明需要进行断点续传
                startPos = i * size + file.length();
            }
            if (i == Constant.THREAD_NUM - 1) {  //如果是最后一段任务则endPos=0
                endPos = 0;
            } else {
                endPos = i * size + size;
            }
            if (startPos != 0) {  //如果不是第一段任务则startPos ++
                startPos ++;
            }
            if (file.length() >= size) {  //该情况说明该线程的下载任务已完成(这里可能有点瑕疵)
                startPos = endPos;
            }
            DownLoaderTask downLoaderTask = new DownLoaderTask(url, startPos, endPos, i, countDownLatch, file.length());
            Future<Boolean> future = threadPoolExecutor.submit(downLoaderTask);  //向线程池提交任务，返回值交给future
            futureArrayList.add(future);
        }
    }

    public static boolean merge(String fileName) {
        LogUtils.info("开始合并文件{}", fileName);
        byte[] buffer = new byte[Constant.BYTE_SIZE];
        int len = -1;
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(fileName, "rw")) {
            for (int i = 0; i < Constant.THREAD_NUM; i ++) {
                try(BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileName + ".temp" + i))){  //读出
                    while ((len = bufferedInputStream.read(buffer)) != -1) {
                        randomAccessFile.write(buffer, 0, len);  //写入
                    }
                }
            }
            LogUtils.info("文件合并完毕{}", fileName);
            LogUtils.info("下载成功！");
        } catch (FileNotFoundException e) {
            LogUtils.error("找不到文件！");
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            LogUtils.error("IO异常");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean clearTemp(String fileName) {  //删除所有临时文件
        for (int i = 0; i < Constant.THREAD_NUM; i ++) {
            File file = new File(fileName + ".temp" + i);
            file.delete();
        }
        return true;
    }

    public static long getFinishedSize(String fileName) {  //获取下载前就已经下载好的任务总大小
        long nowFinishedSize = 0;
        for (int i = 0; i < Constant.THREAD_NUM; i ++) {
            File file = new File(fileName + ".temp" + i);
            DownLoadInfoThread.finishedSize.add(file.length());
        }
        return DownLoadInfoThread.finishedSize.longValue();
    }

}
