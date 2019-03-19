package com.site.utils;

import com.site.core.model.User;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class Md5Utils {

	public static String toHex(String s){
        if (s == null) {
            return null;
        }
        return DigestUtils.sha1Hex(DigestUtils.md5Hex(s));
    }
	
	public static void main(String[] args) {
	    Long a = null;
        Long b = null;
        Long c = null;
        System.out.println(a+b+c);
    }
}