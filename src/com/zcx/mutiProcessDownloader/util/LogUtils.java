package com.zcx.mutiProcessDownloader.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtils { //日志工具类

    public static void info(String msg, Object... args) {  //输出正常信息
        print(msg, "-INFO-", args);
    }

    public static void error(String msg, Object... args) {  //输出异常信息
        print(msg, "-ERROR-", args);
    }

    private static void print(String msg, String level, Object... args) {
        if (args != null && args.length > 0) {
            msg = String.format(msg.replace("{}", "%s"), args);  //将{}中的信息替换为可变参数
        }
        String name = Thread.currentThread().getName(); //获取当前线程名称
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " " + name + level + msg);
    }

}
