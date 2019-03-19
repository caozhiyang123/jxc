package com.site.utils;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Table;
import com.jfinal.plugin.activerecord.TableMapping;
import sun.misc.BASE64Decoder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

/**
 * 公用方法工具类
 *
 * @author WangSQ
 */
public class ToolFunction {
    public static final ToolFunction me = new ToolFunction();

    /**
     * 对象转换成字符串
     *
     * @param obj
     * @return
     */
    public String objToString(Object obj) {
        try {
            return obj.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 对象转换成整数
     *
     * @param obj
     * @return
     */
    public int objToInt(Object obj) {
        try {
            return Integer.parseInt(obj.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 对象转换成Long
     *
     * @param obj
     * @return
     */
    public Long objToLong(Object obj) {
        try {
            return Long.parseLong(obj.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 对象转换成double
     *
     * @param obj
     * @return
     */
    public double objToDouble(Object obj) {
        try {
            return Double.parseDouble(obj.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 对象转换成布尔
     *
     * @param obj
     * @return
     */
    public boolean objToBoolean(Object obj) {
        try {
            if (obj == null || "".equals(obj.toString().trim()))
                return false;
            String value = obj.toString().trim().toLowerCase();
            if ("1".equals(value) || "true".equals(value))
                return true;
            else if ("0".equals(value) || "false".equals(value))
                return false;
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * 判断是否是数字
     *
     * @param obj
     * @return
     */
    public static boolean isNumeric(Object obj) {
        if (obj == null) {
            return false;
        } else {
            char[] chars = obj.toString().toCharArray();
            int length = chars.length;
            if (length < 1) {
                return false;
            } else {
                int i = 0;
                if (length > 1 && chars[0] == 45) {
                    i = 1;
                }
                while (i < length) {
                    if (!Character.isDigit(chars[i])) {
                        return false;
                    }

                    ++i;
                }
                return true;
            }
        }
    }

    /**
     * 替换Map中的键
     *
     * @param map
     * @param oldKeys
     * @param newKeys
     * @return
     */
    public Map<String, Object> changeMapKey(Map<String, Object> map, String[] oldKeys, String[] newKeys) {
        for (int i = 0; i < oldKeys.length; i++) {
            if (map.get(oldKeys[i]) != null) {
                map.put(newKeys[i], map.get(oldKeys[i]));
                map.remove(oldKeys[i]);
            }
        }
        return map;
    }

    public String timestampToTime(String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = sdf.format(new Date((long) (objToDouble(timestamp) * 1000)));
        return date;
    }

    /**
     * 在字符串后面用指定字符填补
     *
     * @param str    被填补字符串
     * @param filler 填补字符
     * @param n      填补位数
     * @return String
     */
    public String fillString(String str, char filler, int n) {
        if (str == null) {
            return null;
        }
        if (str.length() >= n) {
            return str;
        }
        StringBuffer sbf = new StringBuffer(str);
        for (int i = 0; i < n - str.length(); i++) {
            sbf.append(filler);
        }
        return sbf.toString();
    }


    /**
     * map转换成model
     *
     * @param map
     * @param m
     * @param params
     * @return
     */
    public Model<?> mapToModel(Map<String, Object> map, Model<?> m, String[] params) {
        if (map == null) return null;
        Table table = TableMapping.me().getTable(m.getClass());
        if (params == null || params.length == 0) {
            Set<String> set = map.keySet();
            for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
                String key = it.next();
                if (table.hasColumnLabel(key))
                    m.set(key, map.get(key));
            }
        } else {
            for (int i = 0; i < params.length; i++) {
                String key = params[i];
                if (table.hasColumnLabel(key))
                    m.set(key, objToString(map.get(key)));
            }
        }
        return m;
    }

    /**
     * model转换成Map
     *
     * @param m
     * @param params
     * @return
     */
    public Map<String, Object> modelToMap(Model<?> m, String[] params) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        try {
            if (params == null || params.length == 0) {
                Set<Map.Entry<String, Object>> set = m._getAttrsEntrySet();
                for (Map.Entry<String, Object> entry : set) {
                    map.put(entry.getKey(), entry.getValue());
                }
            } else {
                for (int i = 0; i < params.length; i++) {
                    map.put(params[i], m.get(params[i]));
                }
            }
            return map;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据入参params更新Model内容，如需更新数据库还需要调用update()方法
     *
     * @param model_old
     * @param map
     * @param params
     * @return
     */
    public Model<?> updateModel(Model<?> model_old, Map<String, Object> map, String[] params) {
        Table table = TableMapping.me().getTable(model_old.getClass());
        if (params == null || params.length == 0) {
            Set<Map.Entry<String, Object>> set = map.entrySet();
            for (Map.Entry<String, Object> entry : set) {
                if (table.hasColumnLabel(entry.getKey()))
                    model_old.set(entry.getKey(), entry.getValue());
            }
        } else {
            for (int i = 0; i < params.length; i++) {
                String key = params[i];
                if (table.hasColumnLabel(key) && map.containsKey(key)) {
                    Class<?> xclass = table.getColumnTypeMap().get(key);
                    model_old.set(key, map.get(key));
                    if (xclass.getName().equals("java.lang.Long")) {
                        model_old.set(key, Long.valueOf(map.get(key).toString()));
                    }
                }
            }
        }
        return model_old;
    }

    /**
     * url参数替换
     *
     * @param url
     * @param name
     * @param accessToken
     * @return
     */
    public static String replaceAccessTokenReg(String url, String name, String accessToken) {
//		if (StringUtils.isNotBlank(url) && StringUtils.isNotBlank(accessToken)) {
//			url = url.replaceAll("(" + name + "=[^&]*)", name + "=" + accessToken);
//		}
//	    String pattern =  "(^?|^&)"+name+"=[^&]*"; 
//	    System.out.println(pattern);
        String replaceText = name + '=' + accessToken;
        if (url.matches(".*(\\?|\\&)" + name + "=.*")) {
            url = url.replaceAll("(" + name + "=[^&]*)", replaceText);
        } else {
            /*System.out.println(url.matches("\\w*\\&"+name+"=([^&]*)"));*/
            if (url.matches(".*\\?.*")) {
                url = url + '&' + replaceText;

            } else {
                url = url + '?' + replaceText;
            }
        }
        return url;
    }


    /**
     * 根据类型转换入参类型
     *
     * @param type
     * @param s
     * @return
     * @throws ParseException
     */
    public final Object convert(Class<?> type, String s) throws ParseException {
        String timeStampPattern = "yyyy-MM-dd HH:mm:ss";
        String datePattern = "yyyy-MM-dd";
        int timeStampLen = timeStampPattern.length();
        // mysql type: varchar, char, enum, set, text, tinytext, mediumtext, longtext
        if (type == String.class) {
            return ("".equals(s) ? null : s);    // 鐢ㄦ埛鍦ㄨ〃鍗曞煙涓病鏈夎緭鍏ュ唴瀹规椂灏嗘彁浜よ繃鏉�"", 鍥犱负娌℃湁杈撳叆,鎵�互瑕佽浆鎴�null.
        }
        s = s.trim();
        if ("".equals(s)) {    // 鍓嶉潰鐨�String璺宠繃浠ュ悗,鎵�湁鐨勭┖瀛楃涓插叏閮借浆鎴�null,  杩欐槸鍚堢悊鐨�
            return null;
        }
        // 浠ヤ笂涓ょ鎯呭喌鏃犻渶杞崲,鐩存帴杩斿洖, 娉ㄦ剰, 鏈柟娉曚笉鎺ュ彈null涓�s 鍙傛暟(缁忔祴璇曟案杩滀笉鍙兘浼犳潵null, 鍥犱负鏃犺緭鍏ヤ紶鏉ョ殑涔熸槸"")


        // mysql type: int, integer, tinyint(n) n > 1, smallint, mediumint
        if (type == Integer.class || type == int.class) {
            return Integer.parseInt(s);
        }

        // mysql type: bigint
        if (type == Long.class || type == long.class) {
            return Long.parseLong(s);
        }

        // java.util.Date 绫诲瀷涓撲负浼犵粺 java bean 甯︽湁璇ョ被鍨嬬殑 setter 鏂规硶杞崲鍋氬噯澶囷紝涓囦笉鍙幓鎺�
        // 缁忔祴璇�JDBC 涓嶄細杩斿洖 java.util.Data 绫诲瀷銆俲ava.sql.Date, java.sql.Time,java.sql.Timestamp 鍏ㄩ儴鐩存帴缁ф壙鑷�java.util.Data, 鎵�互 getDate鍙互杩斿洖杩欎笁绫绘暟鎹�
        if (type == Date.class) {
            if (s.length() >= timeStampLen) {    // if (x < timeStampLen) 鏀圭敤 datePattern 杞崲锛屾洿鏅鸿兘
                // Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]
                // return new java.util.Date(java.sql.Timestamp.valueOf(s).getTime());	// error under jdk 64bit(maybe)
                return new SimpleDateFormat(timeStampPattern).parse(s);
            } else {
                // return new java.util.Date(java.sql.Date.valueOf(s).getTime());	// error under jdk 64bit
                return new SimpleDateFormat(datePattern).parse(s);
            }
        }

        // mysql type: date, year
        if (type == java.sql.Date.class) {
            if (s.length() >= timeStampLen) {    // if (x < timeStampLen) 鏀圭敤 datePattern 杞崲锛屾洿鏅鸿兘
                // return new java.sql.Date(java.sql.Timestamp.valueOf(s).getTime());	// error under jdk 64bit(maybe)
                return new java.sql.Date(new SimpleDateFormat(timeStampPattern).parse(s).getTime());
            } else {
                // return new java.sql.Date(java.sql.Date.valueOf(s).getTime());	// error under jdk 64bit
                return new java.sql.Date(new SimpleDateFormat(datePattern).parse(s).getTime());
            }
        }

        // mysql type: time
        if (type == java.sql.Time.class) {
            return java.sql.Time.valueOf(s);
        }

        // mysql type: timestamp, datetime
        if (type == java.sql.Timestamp.class) {
            if (s.length() >= timeStampLen) {
                return java.sql.Timestamp.valueOf(s);
            } else {
                return new java.sql.Timestamp(new SimpleDateFormat(datePattern).parse(s).getTime());
            }
        }

        // mysql type: real, double
        if (type == Double.class || type == double.class) {
            return Double.parseDouble(s);
        }

        // mysql type: float
        if (type == Float.class || type == float.class) {
            return Float.parseFloat(s);
        }

        // mysql type: bit, tinyint(1)
        if (type == Boolean.class || type == boolean.class) {
            String value = s.toLowerCase();
            if ("1".equals(value) || "true".equals(value)) {
                return Boolean.TRUE;
            } else if ("0".equals(value) || "false".equals(value)) {
                return Boolean.FALSE;
            } else {
                throw new RuntimeException("Can not parse to boolean type of value: " + s);
            }
        }

        // mysql type: decimal, numeric
        if (type == java.math.BigDecimal.class) {
            return new java.math.BigDecimal(s);
        }

        // mysql type: unsigned bigint
        if (type == java.math.BigInteger.class) {
            return new java.math.BigInteger(s);
        }

        // mysql type: binary, varbinary, tinyblob, blob, mediumblob, longblob. I have not finished the test.
        if (type == byte[].class) {
            return s.getBytes();
        }

        throw new RuntimeException(type.getName() + " can not be converted, please use other type of attributes in your model!");
    }

    // 解密
    public static String getFromBase64(String s) {
        byte[] b = null;
        String result = null;
        if (s != null) {
            BASE64Decoder decoder = new BASE64Decoder();
            try {
                b = decoder.decodeBuffer(s);
                result = new String(b, "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    
    public static List<String> str2List(String str)
    {
    	List<String> list = new ArrayList<String>();
    	
    	if (StringUtils.isEmpty(str))
    	{
    		return list;
    	}
    	String[] arr =  str.split(",");
    	
    	for (String s : arr)
    	{
    		if (!StringUtils.isEmpty(s))
    		{
    			list.add(s);
    		}
    	}
    	
    	return list;
    }
    
    public static String getLevel(String levels)
    {
    	if (StringUtils.isEmpty(levels))
    	{
    		return "";
    	}
    	
    	if (levels.indexOf(",") != -1)
    	{
        	String[] arr = levels.split(",");
        	
        	List<String> levelList = new ArrayList<String>();
        	
        	for (String str : levelList)
        	{
        		if (!StringUtils.isEmpty(str))
        		{
        			levelList.add(str);
        		}
        	}
        	
        	if (levelList.isEmpty() || levelList.size() == 2)
        	{
        		return "";
        	}
        	
        	if (levelList.size() == 1)
        	{
        		return levelList.get(0);
        	}
    	}
    	else
    	{
    		return levels;
    	}
    	
    	return "";
    }
}
