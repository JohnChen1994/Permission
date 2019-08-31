package com.john.common;

import com.john.model.SysUser;

import javax.servlet.http.HttpServletRequest;

/**
 * 处理并发请求
 * ThreadLocal 相当于map   key : 当前进程  使用时都是单独的进程，不会冲突
 */
public class RequestHolder {

    private static final ThreadLocal<SysUser> userHolder = new ThreadLocal<SysUser>();

    private static final ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<>();

    public static void add(SysUser sysUser){
        userHolder.set(sysUser);
    }

    public static void add(HttpServletRequest request){
        requestHolder.set(request);
    }

    public static SysUser getCurrentUser(){
        return userHolder.get();
    }

    public static HttpServletRequest getCurrentRequest(){
        return requestHolder.get();
    }

    public static void remove(){
        userHolder.remove();
        requestHolder.remove();
    }
}
