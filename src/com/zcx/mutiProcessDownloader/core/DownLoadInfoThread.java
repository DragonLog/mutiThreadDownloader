package com.zcx.mutiProcessDownloader.core;

import com.zcx.mutiProcessDownloader.constant.Constant;
import com.zcx.mutiProcessDownloader.util.LogUtils;

import java.util.concurrent.atomic.LongAdder;

public class DownLoadInfoThread implements Runnable {

    private long httpFileContentLength;  //要下载文件的总大小

    public static LongAdder finishedSize = new LongAdder();  //原子变量,代表执行下载任务前,已下载文件的大小（“断点续传”）

    public DownLoadInfoThread(long httpFileContentLength) {
        this.httpFileContentLength = httpFileContentLength;
    }

    public static LongAdder downSize = new LongAdder();  //原子变量,代表当前时间段结束时下载文件的大小

    public long preSize;  //代表当前时间段开始时下载文件的大小

    @Override
    public void run() {
        String httpFileSize = String.format("%.2f", (double) httpFileContentLength / Constant.MB);  //目标文件总大小（换算单位为mb）
        int speed = (int) ((downSize.longValue() - preSize) / 1024);  //计算下载速度
        preSize = downSize.longValue();
        long remainSize = httpFileContentLength - finishedSize.longValue() - downSize.longValue();  //计算剩余下载文件大小
        String remainTime = String.format("%.2f", (double) remainSize / 1024 / speed);  //计算剩余时间
        if ("infinity".equalsIgnoreCase(remainTime)) { //当speed趋近0时，剩余时间可能为无限大
            remainTime = "-";
        }
        String currentFileSize = String.format("%.2f", (double) downSize.longValue() / Constant.MB);  //计算当前时间段已下载文件总大小
        String downInfo = String.format("已下载 %sMB/%sMB, 速度 %skb/s, 剩余时间 %ss", currentFileSize, httpFileSize, speed, remainTime); //日志输出计算信息
        LogUtils.info(downInfo);
    }
}
