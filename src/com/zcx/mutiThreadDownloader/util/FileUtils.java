package com.zcx.mutiThreadDownloader.util;

import java.io.File;

public class FileUtils {

    public static long getFileContentLength(String path) {
        File file = new File(path);
        return file.exists() && file.isFile() ? file.length() : 0;  //如果文件存在且是文件则返回文件大小，否则返回0;
    }

}
