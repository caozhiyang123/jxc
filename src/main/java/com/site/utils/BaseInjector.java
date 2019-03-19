package com.site.utils;

import com.site.utils.ToolFunction;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Table;
import com.jfinal.plugin.activerecord.TableMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by HonePhy on 16/7/1.
 */
public class BaseInjector {

    public static <T> T injectModel(Class<T> modelClass, HttpServletRequest request, boolean skipConvertError) {
        String modelName = modelClass.getSimpleName();
        return (T)injectModel(modelClass, StrKit.firstCharToLowerCase(modelName), request, skipConvertError);
    }

    public static final <T> T injectModel(Class<T> modelClass, String modelName, HttpServletRequest request, boolean skipConvertError) {
        Object temp = createInstance(modelClass);
        if (temp instanceof Model == false) {
            throw new IllegalArgumentException("getModel only support class of Model, using getBean for other class.");
        }

        Model<?> model = (Model<?>)temp;
        Table table = TableMapping.me().getTable(model.getClass());
        if (table == null) {
            throw new ActiveRecordException("The Table mapping of model: " + modelClass.getName() +
                    " not exists or the ActiveRecordPlugin not start.");
        }

        String modelNameAndDot = StrKit.notBlank(modelName) ? modelName + "." : null;
        Map<String, String[]> parasMap = (Map<String, String[]>)request.getSession().getAttribute("parameterMap");
        // 对 paraMap进行遍历而不是对table.getColumnTypeMapEntrySet()进行遍历，以便支持 CaseInsensitiveContainerFactory
        // 以及支持界面的 attrName有误时可以感知并抛出异常避免出错
        for (Map.Entry<String, String[]> entry : parasMap.entrySet()) {
            String paraName = entry.getKey();
            String attrName;
            if (modelNameAndDot != null) {
                if (paraName.startsWith(modelNameAndDot)) {
                    attrName = paraName.substring(modelNameAndDot.length());
                } else {
                    continue ;
                }
            } else {
                attrName = paraName;
            }

            Class<?> colType = table.getColumnType(attrName);
            if (colType == null) {
                if (skipConvertError) {
                    continue ;
                } else {
                    throw new ActiveRecordException("The model attribute " + attrName + " is not exists.");
                }
            }

            try {
                String[] paraValueArray = entry.getValue();
                String paraValue = (paraValueArray != null && paraValueArray.length > 0) ? paraValueArray[0] : null;

                Object value = paraValue != null ? ToolFunction.me.convert(colType, paraValue) : null;
                model.set(attrName, value);
            } catch (Exception e) {
                if (skipConvertError == false) {
                    throw new RuntimeException("Can not convert parameter: " + paraName, e);
                }
            }
        }

        return (T)model;
    }

    private static <T> T createInstance(Class<T> objClass) {
        try {
            return objClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
