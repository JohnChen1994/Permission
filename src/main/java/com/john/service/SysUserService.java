package com.john.service;

import com.google.common.base.Preconditions;
import com.john.beans.PageQuery;
import com.john.beans.PageResult;
import com.john.common.RequestHolder;
import com.john.dao.SysUserMapper;
import com.john.exception.ParamException;
import com.john.model.SysUser;
import com.john.param.UserParam;
import com.john.util.BeanValidator;
import com.john.util.IpUtil;
import com.john.util.MD5Util;
import com.john.util.PasswordUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class SysUserService {

    @Resource
    SysUserMapper sysUserMapper;

    // 新增用户
    public void save(UserParam param){
        BeanValidator.check(param);
        if(checkTelephone(param.getTelephone(), param.getId())){
            throw new ParamException("电话已被占用");
        }
        if(checkEmailExist(param.getMail(), param.getId())){
            throw new ParamException("邮箱已被占用");
        }
        // 1.通过工具类生成password ,通过邮箱告知用户
        // 2.用户设置密码并确认密码
        String password = PasswordUtil.randomPassword();
        // TODO:
        password = "12345678";
        String encryptedPassword = MD5Util.encrypt(password);
        SysUser user = SysUser.builder().username(param.getUsername()).telephone(param.getTelephone())
                .mail(param.getMail()).password(encryptedPassword).deptId(param.getDeptId()).status(param.getStatus())
                .remark(param.getRemark()).build();
        user.setOperator(RequestHolder.getCurrentUser().getUsername());
        user.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));  // TODO:
        user.setOperateTime(new Date());

        // TODO: sendEmail
        sysUserMapper.insertSelective(user);
    }

    public void update(UserParam param){
        BeanValidator.check(param);
        if(checkTelephone(param.getTelephone(), param.getId())){
            throw new ParamException("电话已被占用");
        }
        if(checkEmailExist(param.getMail(), param.getId())){
            throw new ParamException("邮箱已被占用");
        }
        SysUser before = sysUserMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(before, "待更新用户不存在");
        SysUser after = SysUser.builder().id(param.getId()).username(param.getUsername()).telephone(param.getTelephone())
                .mail(param.getMail()).password(before.getPassword()).deptId(param.getDeptId()).status(param.getStatus())
                .remark(param.getRemark()).build();

        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        after.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));  // TODO:
        after.setOperateTime(new Date());
        sysUserMapper.updateByPrimaryKeySelective(after);
    }

    public boolean checkEmailExist(String mail, Integer userId){
        // 存在
        return sysUserMapper.countByMail(mail, userId)  > 0;
    }

    public boolean checkTelephone(String telephone, Integer userId){
        return sysUserMapper.countByTelephone(telephone, userId) > 0;
    }

    public SysUser findByKeyword(String keyword){
        return sysUserMapper.findByKeyword(keyword);
    }

    public PageResult<SysUser> getPageByDeptId(int deptId, PageQuery page){
        BeanValidator.check(page);
        int count = sysUserMapper.countByDeptId(deptId);
        if(count > 0){
            List<SysUser> list = sysUserMapper.getPageByDeptId(deptId, page);
            return PageResult.<SysUser>builder().total(count).data(list).build();
        }
        return PageResult.<SysUser>builder().build();
    }

    // 获取所有用户
    public List<SysUser> getAll(){
        return sysUserMapper.getAll();
    }
}



















