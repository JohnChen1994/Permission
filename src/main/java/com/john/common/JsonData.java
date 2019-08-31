package com.john.common;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据请求，json返回 ，告知前台数据返回的信息是正常还是异常
 */
@Getter
@Setter
public class JsonData {

    // 返回结果 成功或是失败
    private boolean ret;

    // 信息
    private String msg;

    // 数据
    private Object data;

    public JsonData(boolean ret){
        this.ret = ret;
    }

    // 成功返回数据和消息
    public static JsonData success(Object object, String msg){
        JsonData jsonData = new JsonData(true);
        jsonData.data = object;
        jsonData.msg = msg;
        return  jsonData;
    }

    // 成功返回数据
    public static JsonData success(Object object){
        JsonData jsonData = new JsonData(true);
        jsonData.data = object;
        return  jsonData;
    }

    public static JsonData success(){
        JsonData jsonData = new JsonData(true);
        return  jsonData;
    }

    // 失败返回消息
    public static JsonData fail(String msg){
        JsonData jsonData = new JsonData(false);
        jsonData.msg = msg;
        return  jsonData;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("ret", ret);
        result.put("msg", msg);
        result.put("data", data);
        return result;
    }

}































