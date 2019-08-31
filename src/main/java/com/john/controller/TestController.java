package com.john.controller;

import com.john.common.ApplicationContextHelper;
import com.john.common.JsonData;
import com.john.dao.SysAclModuleMapper;
import com.john.exception.ParamException;
import com.john.model.SysAclModule;
import com.john.param.TestVo;
import com.john.util.BeanValidator;
import com.john.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/test")
@Slf4j
public class TestController {

    // 测试项目框架搭建
    @RequestMapping("/hello")
    @ResponseBody
    public String hello(){
        log.info("hello");
        return "hello, permission";
    }

    // 测试json page 全局异常处理类
    @RequestMapping("/hello.json")
    @ResponseBody
    public JsonData hello1(){
        log.info("hello");
        throw new RuntimeException("test Exception");
        //throw new PermissionException("test Exception");
        //return JsonData.fail("hello, permission");
    }


    // 测试校验工具类Validate
    @RequestMapping("/validate.json")
    @ResponseBody
    public JsonData validate(TestVo vo) throws ParamException

    {
        log.info("validate");
        /*try {
            Map<String, String> map = BeanValidator.validateObject(vo);
            if(MapUtils.isNotEmpty(map)){
                for(Map.Entry<String, String> entry: map.entrySet()){
                    log.info("{}->{}", entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        BeanValidator.check(vo);
        //throw new RuntimeException("test Exception");
        //throw new PermissionException("test Exception");
        return JsonData.success("test validate");
    }

    // 测试上下文
    @RequestMapping("/validate1.json")
    @ResponseBody
    public JsonData validate1(TestVo vo) throws ParamException{
        log.info("hello");
        SysAclModuleMapper moduleMapper = ApplicationContextHelper.popBean(SysAclModuleMapper.class);
        SysAclModule sysAclModule = moduleMapper.selectByPrimaryKey(1);
        log.info(JsonMapper.obj2String(sysAclModule));
        BeanValidator.check(vo);
        //throw new RuntimeException("test Exception");
        //throw new PermissionException("test Exception");
        return JsonData.success("test validate");
    }



}
