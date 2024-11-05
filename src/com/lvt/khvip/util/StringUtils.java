package com.lvt.khvip.util;

public class StringUtils {

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
    
    public static boolean isEmpty(Object s) {
        return s == null;
    }
}
