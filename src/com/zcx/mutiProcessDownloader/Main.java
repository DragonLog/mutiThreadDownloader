package com.zcx.mutiProcessDownloader;

import com.zcx.mutiProcessDownloader.core.Downloader;
import com.zcx.mutiProcessDownloader.util.LogUtils;

import java.util.Scanner;

/**
 * 动力节点javaSE项目——多线程下载器
 * 技术栈：
 * 1、自定义日志工具类LogUtils结合newScheduledThreadPool线程池周期打印程序日志；
 * 2、LongAdder原子类保证实例变量可见性和原子性；
 * 3、java-I/O流（RandomAccessFile等）实现灵活的读写、拆分和组合文件；
 * 4、HttpURLConnection类网络请求实现分段（range）下载文件；
 * 5、ThreadPoolExecutor线程池运行多线程下载任务；
 * 6、CountDownLatch类等待线程任务完成后调度。
 * 综上所述实现一个具有“断点续传”功能的，根据http下载链接进行下载的并发多线程下载器项目
 */
public class Main {

    public static void main(String[] args) {  //可以带参数
        String url = null;

        if (args == null || args.length == 0) {  //不带参数就手动输入
            while (true) {
                LogUtils.info("请输入下载链接:");  //调用日志类格式整齐
                Scanner scanner = new Scanner(System.in);
                url = scanner.next();
                if (url != null) {
                    break;
                }
            }
        } else {
            url = args[0];
        }
        Downloader.download(url);  //调用核心方法进行下载
    }

}
