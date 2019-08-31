package com.john.common;


import com.john.exception.ParamException;
import com.john.exception.PermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 全局异常捕捉类
 */
@Slf4j
public class SpringExceptionResolve implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 当前访问url
        String url = request.getRequestURI().toString();
        // 请求返回的modelandView
        ModelAndView mv;
        // 定义全局异常
        String defaultMsg = "System error";

        // .json  数据请求  .page  页面请求
        // 这里要求 项目中所有请求json数据， 都是用.json结尾
        if(url.endsWith(".json")){
            // 当前异常为自定义的异常
            if(ex instanceof PermissionException  || ex instanceof ParamException){
                JsonData result = JsonData.fail(ex.getMessage());
                mv = new ModelAndView("jsonView", result.toMap());
            }else{
                log.error("unknow json exception, url:" + url, ex);
                JsonData result = JsonData.fail(defaultMsg);
                mv = new ModelAndView("jsonView", result.toMap());

            }
        }else if(url.endsWith(".page")){  //这里要求 项目中所有请求page数据， 都是用.page结尾
            log.error("unknow page exception, url:" + url, ex);
            JsonData result = JsonData.fail(defaultMsg);
            mv = new ModelAndView("exception", result.toMap());
        }else{
            log.error("unknow exception, url:" + url, ex);
            JsonData result = JsonData.fail(defaultMsg);
            mv = new ModelAndView("jsonView", result.toMap());
        }
        return mv;
    }
}









