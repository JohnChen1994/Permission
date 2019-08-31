package com.john.filter;


import com.john.common.RequestHolder;
import com.john.model.SysUser;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 校验用户是否登录  用户没有登录， 跳到登录页面，登录，取出信息，放入ThrealLocal
 */
public class LoginFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        String servletPath = req.getServletPath();

        SysUser sysUser = (SysUser)req.getSession().getAttribute("user");
        if(sysUser == null){
            // 当前用户未登录， 跳转到登录页面
            String path = "/signin.jsp";
            resp.sendRedirect(path);
            return;
        }
        RequestHolder.add(sysUser);
        RequestHolder.add(req);
        filterChain.doFilter(servletRequest, servletResponse);
        return;
    }

    @Override
    public void destroy() {

    }

}
