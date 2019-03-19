package com.site.utils;

import com.site.base.BaseController;

/**
 * 图片工具类
 */
public class ImageUtil {
    private static final String ESCAPE_CHARACTER  = "#_notice";

    /**
     * 图片路径转成特殊字符
     * @return
     */
    public static String imgPathToEscapeChar(String content) {
        return content.replaceAll(BaseController.SERVER_PATH, ESCAPE_CHARACTER);
    }

    /**
     * 特殊字符转成图片路径
     * @return
     */
    public static String escapeCharToImgPath(String content) {
        return content.replaceAll(ESCAPE_CHARACTER, BaseController.SERVER_PATH);
    }

    public static void main(String[] args) {
        String a = "style=\"white-space: normal;\"";
        System.out.println(a.replaceAll("\"", "\'"));
    }
}
