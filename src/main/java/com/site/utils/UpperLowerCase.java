package com.site.utils;

import com.jfinal.plugin.activerecord.Model;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by HonePhy on 16/7/11.
 */
public class UpperLowerCase {

    private static Map<String,String> tokens = new HashMap<>();
    static {
        tokens.put("A", "_a");
        tokens.put("B", "_b");
        tokens.put("C", "_c");
        tokens.put("D", "_d");
        tokens.put("E", "_e");
        tokens.put("F", "_f");
        tokens.put("G", "_g");
        tokens.put("H", "_h");
        tokens.put("I", "_i");
        tokens.put("J", "_j");
        tokens.put("K", "_k");
        tokens.put("L", "_l");
        tokens.put("M", "_m");
        tokens.put("N", "_n");
        tokens.put("O", "_o");
        tokens.put("P", "_p");
        tokens.put("Q", "_q");
        tokens.put("R", "_r");
        tokens.put("S", "_s");
        tokens.put("T", "_t");
        tokens.put("U", "_u");
        tokens.put("V", "_v");
        tokens.put("W", "_w");
        tokens.put("X", "_x");
        tokens.put("Y", "_y");
        tokens.put("Z", "_z");
    };

    public static List<Map> convert2UpperCase(List<? extends Model> models) {
        List<Map> rs = new ArrayList<>();
        for (Model model : models) {
            rs.add(convert2UpperCase(model));
        }
        return rs;
    }

    public static Map convert2UpperCase(Model m) {
        Map map = new HashMap();
        Set<Map.Entry<String, Object>> set = m._getAttrsEntrySet();
        for (Map.Entry entry : set) {
            map.put(underlineToCamelhump(entry.getKey().toString()), entry.getValue());
        }
        return map;
    }

    public static String underlineToCamelhump(String str) {
        Matcher matcher = Pattern.compile("_[a-z]").matcher(str);
        StringBuilder builder = new StringBuilder(str);
        for (int i = 0; matcher.find(); i++) {
            builder.replace(matcher.start() - i, matcher.end() - i, matcher.group().substring(1).toUpperCase());
        }
        return builder.toString();
    }

    public static String toUnderLine(String field) {
        String patternString = "(" + StringUtils.join(tokens.keySet(), "|") + ")";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(field);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
            matcher.appendReplacement(sb, tokens.get(matcher.group(1)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
