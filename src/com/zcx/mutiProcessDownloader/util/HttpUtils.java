package com.zcx.mutiProcessDownloader.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpUtils {

    public static long getHttpFileConnectionLength(String url) {
        int contentLength;
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = getHttpURLConnection(url);
            contentLength = httpURLConnection.getContentLength();  //获取下载文件大小
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();  //关闭链接
            }
        }
        return contentLength;
    }

    public static HttpURLConnection getHttpURLConnection(String url, long startPos, long  endPos) {  //重载方法
        HttpURLConnection httpURLConnection = getHttpURLConnection(url);
        LogUtils.info("下载的区间是:{}-{}", startPos, endPos);  //分段下载要指定区间

        if (endPos != 0) {  //endPos!=0说明下载的不是最后一段
            httpURLConnection.setRequestProperty("RANGE", "bytes=" + startPos + "-" + endPos);
        } else { //endPos=0说明下载的是最后一段
            httpURLConnection.setRequestProperty("RANGE", "bytes=" + startPos + "-");
        }
        return httpURLConnection;
    }

    public static HttpURLConnection getHttpURLConnection(String url) {  //重载方法
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection httpUrlConnection = (HttpURLConnection) httpUrl.openConnection();
            httpUrlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.163 Safari/535.1"); //设置请求头，让普通java程序网络请求变为浏览器网络请求
            return httpUrlConnection;
        } catch (MalformedURLException e) {
            LogUtils.error("协议出错");
            e.printStackTrace();
        } catch (IOException e) {
            LogUtils.error("IO异常");
            e.printStackTrace();
        }
        return null;
    }

    public static String getHttpFileName(String url) {  //获取下载文件的文件名
        int index = url.lastIndexOf("/");
        return url.substring(index + 1);
    }

}
