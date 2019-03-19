package com.site.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JacksonUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JacksonUtil.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        //空值也序列化
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //忽略Json中的属性在java对象上不存在
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //日期统一格式
        MAPPER.setDateFormat(SIMPLE_DATE_FORMAT);
    }

    /**
     * JavaBean ==> json
     *
     * @param bean
     * @return
     */
    public static <T> String bean2Json(T bean) {
        try {
            return MAPPER.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            LOG.error("JavaBean ===> JSON ERROR : {}", e.getMessage());
        }
        return null;
    }

    /**
     * JavaBean ==> map
     *
     * @param bean
     * @return
     */
    public static <T> Map<String, Object> bean2Map(T bean) {
        return json2Map(bean2Json(bean));
    }

    /**
     * json ==> Object
     *
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T json2Obj(String json, Class<T> clazz) {
        try {
            JsonNode jsonNode = MAPPER.readTree(json);
            JsonNode body = jsonNode.get("body");
            if (jsonNode.get("head") != null && body != null && jsonNode.size() == 2) {
                return MAPPER.treeToValue(body, clazz);
            }
            return MAPPER.treeToValue(jsonNode, clazz);
//            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            LOG.error("JSON ==> {} ERROR : {}", clazz.getClass().getName(), e.getMessage());
        }
        return null;
    }

    /**
     * 从请求的json中提取header对象
     */
    public static <T> T json2RequestHead(String json, Class<T> clazz) {
        try {
            JsonNode jsonNode = MAPPER.readTree(json);
            JsonNode head = jsonNode.get("head");
            if (jsonNode.get("body") != null && head != null && jsonNode.size() == 2) {
                return MAPPER.treeToValue(head, clazz);
            }
            return MAPPER.treeToValue(jsonNode, clazz);
        } catch (IOException e) {
            LOG.error("JSON ==> {} ERROR : {}", clazz.getClass().getName(), e.getMessage());
        }
        return null;
    }


    /**
     * json ==> Object[]
     *
     * @param json
     * @param classes
     * @param <T>
     * @return
     */
    public static <T> Class[] json2Array(String json, Class<T>[] classes) {
        try {
            return MAPPER.readValue(json, classes.getClass());
        } catch (IOException e) {
            LOG.error("JSON ==> {} ERROR : {}", classes.getClass().getName(), e.getMessage());
        }
        return null;
    }

    /**
     * json ==> Map
     *
     * @param json
     * @return
     */
    public static Map<String, Object> json2Map(String json) {
        return json2Obj(json, HashMap.class);
    }


    /**
     * json ==> List
     *
     * @param json
     * @return
     */
    public static <T> List<T> json2ListBean(String json, Class<T> clzz) {
        JavaType javaType = getCollectionType(ArrayList.class, clzz);
        try {
            return MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            LOG.error("JSON ==> List ERROR : {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取泛型的Collection Type
     *
     * @param collectionClass 泛型的Collection
     * @param elementClasses  元素类
     * @return JavaType Java类型
     * @since 1.0
     */
    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return MAPPER.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    /**
     * 复杂的BEAN，两层结构
     *
     * @param json            字符串
     * @param collectionClass 容器中CLASS
     * @param bean            泛型中的CLASS
     * @return 获取泛型的对象
     */
    public static <T> T json2Bean(String json, Class<T> collectionClass, Class<?>... bean) {
        JavaType javaType = getCollectionType(collectionClass, bean);
        try {
            return MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            LOG.error("JSON ==> {} ERROR : {}", collectionClass.getClass().getName() + "_" + bean.getClass().getName(),
                    e.getMessage());
        }
        return null;
    }

    public static <T> T convertMapToBean(Class type, Map map)
            throws IntrospectionException, IllegalAccessException,
            InstantiationException, InvocationTargetException {
        BeanInfo beanInfo = Introspector.getBeanInfo(type); // 获取类属性
        T obj = (T) type.newInstance(); // 创建 JavaBean 对象

        // 给 JavaBean 对象的属性赋值
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();

            if (map.containsKey(propertyName)) {
                // 下面一句可以 try 起来，这样当一个属性赋值失败的时候就不会影响其他属性赋值。
                Object value = map.get(propertyName);

                Object[] args = new Object[1];
                args[0] = value;

                descriptor.getWriteMethod().invoke(obj, args);
            }
        }
        return obj;
    }

    /**
     * 如果需要构建复杂的JSON格式,找MAPPER吧
     *
     * @return
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }

}