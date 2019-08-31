package com.john.controller;

import com.john.model.SysUser;
import com.john.service.SysUserService;
import com.john.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
//@RequestMapping("/user")
public class UserController {

    @Resource
    private SysUserService sysUserService;

    @RequestMapping("/logout.page")
    public void logout(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException{
        // 删除session
        req.getSession().invalidate();
        // 跳转
        String path = "signin.jsp";
        resp.sendRedirect(path);

    }

    @RequestMapping("/login.page")
    public void login(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        SysUser sysUser = sysUserService.findByKeyword(username);
        String errorMsg = "";
        String ret = req.getParameter("ret");

        if (StringUtils.isBlank(username)){
            errorMsg = "用户名不能为空";
        }else if(StringUtils.isBlank(password)){
            errorMsg = "密码不可以为空";
        }else if(sysUser == null){
            errorMsg = "查询不到指定用户";
        }else if(!sysUser.getPassword().equals(MD5Util.encrypt(password))){
            errorMsg = "用户名或密码错误";
        }else if(sysUser.getStatus() != 1){
            errorMsg = "用户已被冻结， 请联系管理员";
        }else{
            // login success
            req.getSession().setAttribute("user", sysUser);
            if(StringUtils.isNotBlank(ret)){
                resp.sendRedirect(ret);
            }else{
                resp.sendRedirect("/admin/index.page");  //TODO:
            }
        }

        req.setAttribute("error", errorMsg);
        req.setAttribute("username", sysUser);
        if(StringUtils.isNotBlank(ret)){
            req.setAttribute("ret", ret);
        }
        String path = "signin.jsp";
        req.getRequestDispatcher(path).forward(req, resp);

    }

}



















