package com.zcx.mutiThreadDownloader.core;

import com.zcx.mutiThreadDownloader.constant.Constant;
import com.zcx.mutiThreadDownloader.util.HttpUtils;
import com.zcx.mutiThreadDownloader.util.LogUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class DownLoaderTask implements Callable<Boolean> {

    private String url;  //下载文件地址
    private long startPos;  //文件开始位置
    private long endPos;  //文件结束位置
    private int part;  //文件块号
    private CountDownLatch countDownLatch;
    private long fileStarted;  //下载到一半的文件开始位置（“断点续传”）

    public DownLoaderTask(String url, long startPos, long endPos, int part, CountDownLatch countDownLatch, long fileStarted) {
        this.url = url;
        this.startPos = startPos;
        this.endPos = endPos;
        this.part = part;
        this.countDownLatch = countDownLatch;
        this.fileStarted = fileStarted;
    }

    @Override
    public Boolean call() throws Exception {    //call方法与run方法相比可以有返回值
        String httpFileName = HttpUtils.getHttpFileName(url);
        httpFileName = httpFileName + ".temp" + part;  //分块下载文件命名
        httpFileName = Constant.PATH + httpFileName;
        HttpURLConnection httpURLConnection = HttpUtils.getHttpURLConnection(url, startPos, endPos);  //去网上分段下载文件
        try(//一些对象流可以在try（）实现自动关闭
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                RandomAccessFile randomAccessFile = new RandomAccessFile(httpFileName, "rw");  //RandomAccessFile可在文件任何位置读写
                ) {
            byte[] buffer = new byte[Constant.BYTE_SIZE];
            int len = -1;
            randomAccessFile.seek(fileStarted);  //定位到当前位置写入
            while ((len = bufferedInputStream.read(buffer)) != -1) {
                DownLoadInfoThread.downSize.add(len);  //累加当前时间段的下载量
                randomAccessFile.write(buffer, 0, len);
            }
        } catch (FileNotFoundException e) {
            LogUtils.error("下载文件不存在:{}", url);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            LogUtils.error("IO异常");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            LogUtils.error("下载失败");
            e.printStackTrace();
            return false;
        } finally {  //关闭链接
            httpURLConnection.disconnect();
            countDownLatch.countDown();  //减一表示当前线程的任务已完成
        }
        return true;
    }
}
