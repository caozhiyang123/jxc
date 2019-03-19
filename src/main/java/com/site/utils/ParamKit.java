package com.site.utils;

import com.alibaba.fastjson.JSON;
import com.jfinal.core.Controller;

/**
 * Created by richard on 2016/7/7.
 */
public class ParamKit {

    public static <T> T getPara(Controller controller, Class<T> T) {
        String jsonStr = (String) controller.getRequest().getAttribute("jsonStr");
        T result = JSON.parseObject(jsonStr, T);
        return result;
    }
}
