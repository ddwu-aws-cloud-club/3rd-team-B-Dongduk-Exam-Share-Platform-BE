package com.somshare.somshare.util;

public class LogMaskingUtil {

    private LogMaskingUtil() {}

    public static String maskEmail(String email) {
        if (email == null) return "null";
        int at = email.indexOf("@");
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at); // a***@domain.com
    }
}
