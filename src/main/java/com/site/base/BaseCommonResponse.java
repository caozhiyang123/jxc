package com.site.base;

import java.io.Serializable;

/**
 * Created by HonePhy on 16/3/29.
 */
public class BaseCommonResponse implements Serializable {

    private int code;

    private String message;     //提示信息 success  或者是 error

    public BaseCommonResponse() {
        code = ErrorCode.SUCCESS;
        message = "success";
    }

    public BaseCommonResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class ErrorCode {

        final public static int SUCCESS = 0;        //成功 0

        final public static int FAILED = 1;         //失败 1

        final public static int OTHER_ERROR = 2;    //其他错误 2
    }
}
