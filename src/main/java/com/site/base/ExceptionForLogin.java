package com.site.base;

/**
 * Created by richard on 2017-03-17.
 */
public class ExceptionForLogin extends RuntimeException {
    private String name;
    private String message;

    public ExceptionForLogin(String string) {
        super(string);
    }

    public ExceptionForLogin(String name, String text) {
        this.name = name;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
