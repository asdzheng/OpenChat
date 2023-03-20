package com.asdzheng.openchat.util;

/**
 * @author zhengjb
 * @date on 2023/3/19
 */

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
public class JsonUtil {

    public static final ObjectMapper mapper = new ObjectMapper();

    /**
     * @param obj
     * @return java.lang.String
     * @Name serialize
     * @Description 序列化对象（转json）
     * @Author wen
     * @Date 2019/3/30
     */
    public static String serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj.getClass() == String.class) {
            return (String) obj;
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            Log.e("json序列化出错：" + obj, e.toString());
            return null;
        }
    }

    /**
     * @param json
     * @param tClass
     * @return T
     * @Name parse
     * @Description 反序列化（json转为Bean）
     * @Author wen
     * @Date 2019/3/30
     */
    public static <T> T parse(String json, Class<T> tClass) {
        try {
            return mapper.readValue(json, tClass);
        } catch (IOException e) {
            Log.e("json解析出错：" + json, e.toString());
            return null;
        }
    }

    /**
     * @param json
     * @param eClass
     * @return java.util.List<E>
     * @Name parseList
     * @Description 反序列化（json转List）
     * @Author wen
     * @Date 2019/3/30
     */
    public static <E> List<E> parseList(String json, Class<E> eClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, eClass));
        } catch (IOException e) {
            Log.e("json解析出错：" + json, e.toString());
            return null;
        }
    }

    /**
     * @param json
     * @param kClass
     * @param vClass
     * @return java.util.Map<K, V>
     * @Name parseMap
     * @Description 反序列化（json转Map）
     * @Author wen
     * @Date 2019/3/30
     */
    public static <K, V> Map<K, V> parseMap(String json, Class<K> kClass, Class<V> vClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructMapType(Map.class, kClass, vClass));
        } catch (IOException e) {
            Log.e("json解析出错：" + json, e.toString());
            return null;
        }
    }
}