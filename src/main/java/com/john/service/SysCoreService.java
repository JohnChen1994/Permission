package com.john.service;

import com.google.common.collect.Lists;
import com.john.beans.CacheKeyConstants;
import com.john.common.RequestHolder;
import com.john.dao.SysAclMapper;
import com.john.dao.SysRoleAclMapper;
import com.john.dao.SysRoleUserMapper;
import com.john.model.SysAcl;
import com.john.model.SysUser;
import com.john.util.JsonMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 获取相关权限或角色的service
 */
@Service
public class SysCoreService {

    @Resource
    private SysAclMapper sysAclMapper;
    @Resource
    private SysRoleUserMapper sysRoleUserMapper;
    @Resource
    private SysRoleAclMapper sysRoleAclMapper;
    @Resource
    private SysCacheService sysCacheService;

    // 1. 获取当前用户 已有的权限列表
    public List<SysAcl> getCurrentUserAclList(){
        int userId = RequestHolder.getCurrentUser().getId();
        return getUserAclList(userId);
    }

    // 2. 当前角色以分配的权限列表
    public List<SysAcl> getRoleAclList(int roleId) {
        List<Integer> aclIdList = sysRoleAclMapper.getAclIdListByRoleIdList(Lists.<Integer>newArrayList(roleId));
        if(CollectionUtils.isEmpty(aclIdList)){
            return Lists.newArrayList();
        }
        return sysAclMapper.getByIdList(aclIdList);
    }

    public List<SysAcl> getUserAclList(int userId){
        // 当前分用户配的权限
        if(isSuperAdmin()){
            return sysAclMapper.getAll();
        }
        // 当前用户分配的权限
        List<Integer> userRoleIdList = sysRoleUserMapper.getRoleIdListByUserId(userId);
        // 当前用户未分配权限， 返回空列表
        if(CollectionUtils.isEmpty(userRoleIdList)){
            return Lists.newArrayList();
        }
        // 用户以分配权限列表总和
        List<Integer> userAclIdList = sysRoleAclMapper.getAclIdListByRoleIdList(userRoleIdList);
        if(CollectionUtils.isEmpty(userAclIdList)){
            return Lists.newArrayList();
        }
        // 根据id查询器对象
        return sysAclMapper.getByIdList(userAclIdList);
    }

    public boolean isSuperAdmin(){
        // 是否是超级管理员
        // 自定义一个假的超级管理员规则， 实际中要根据项目进行修改
        // 可以时配置文件获取，可以指定某个用户， 也可以指定某个角色
        SysUser sysUser = RequestHolder.getCurrentUser();
        if(sysUser.getMail().contains("admin")){
            return true;
        }
        return false;
    }

    public boolean hasUrlAcl(String url){
        if(isSuperAdmin()){
            return true;
        }
        List<SysAcl> aclList = sysAclMapper.getByUrl(url);
        if(CollectionUtils.isEmpty(aclList)){
            return true;
        }

        //List<SysAcl> userAclList = getCurrentUserAclList();
        List<SysAcl> userAclList = getCurrentUserAclListFromCache();
        Set<Integer> userAclIdSet = userAclList.stream().map(acl -> acl.getId()).collect(Collectors.toSet());

        // 规则：只要有一个权限点有权限， 那么就认为有权限访问
        boolean hasValidAcl = false;
        for(SysAcl acl : aclList){
            // 判断一个用户是否某个权限点的访问权限
            if(acl == null || acl.getStatus() != 1){
                continue;  // 权限无效
            }
            hasValidAcl = true;
            if(userAclIdSet.contains(acl.getId())){
                return true;
            }
        }
        if(!hasValidAcl){
            return true;
        }
        return false;
    }

    public List<SysAcl> getCurrentUserAclListFromCache(){
        int userId = RequestHolder.getCurrentUser().getId();
        String cacheValue = sysCacheService.getFromCache(CacheKeyConstants.USER_ACLS, String.valueOf(userId));
        if(StringUtils.isBlank(cacheValue)){
            List<SysAcl> aclList = getCurrentUserAclList();
            if(CollectionUtils.isNotEmpty(aclList)){
                sysCacheService.saveCache(JsonMapper.obj2String(aclList), 600, CacheKeyConstants.USER_ACLS, String.valueOf(userId));

            }
            return aclList;
        }
        return JsonMapper.string2Obj(cacheValue, new TypeReference<List<SysAcl>>() {
        });
    }
}
