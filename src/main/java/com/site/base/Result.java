package com.site.base;

import com.jfinal.plugin.activerecord.Page;

public class Result {

    private int code = 0;
    private int count = 0;
    private String message;
    private Object data;

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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setPage(Page page) {
        this.setData(page.getList());
        this.setCount(page.getTotalRow());
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addError(String message) {
        this.code = 1;
        this.message = message;
    }

    public void addError(String message, String col) {
        this.code = 1;
        this.message = message;
        this.data = col;
    }
}
