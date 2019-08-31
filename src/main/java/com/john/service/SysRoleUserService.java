package com.john.service;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.john.common.RequestHolder;
import com.john.dao.SysRoleUserMapper;
import com.john.dao.SysUserMapper;
import com.john.model.SysRoleUser;
import com.john.model.SysUser;
import com.john.util.IpUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class SysRoleUserService {

    @Resource
    private SysRoleUserMapper sysRoleUserMapper;
    @Resource
    private SysUserMapper sysUserMapper;

    // 当前角色选中的用户列表
    public List<SysUser> getListByRoleId(int roleId){
        List<Integer> userIdList = sysRoleUserMapper.getUserIdListByRoleId(roleId);
        if(CollectionUtils.isEmpty(userIdList)){
            return Lists.newArrayList();
        }
        return sysUserMapper.getByIdList(userIdList);

    }

    public void changeRoleUsers(int roleId, List<Integer> userIdList){
        List<Integer> originUserIdList = sysRoleUserMapper.getUserIdListByRoleId(roleId);
        if(originUserIdList.size() == userIdList.size()){
            Set<Integer> originUserIdSet = Sets.newHashSet(originUserIdList);
            Set<Integer> userIdSet = Sets.newHashSet(userIdList);
            originUserIdSet.removeAll(userIdSet);
            if(CollectionUtils.isEmpty(originUserIdSet)){
                return;
            }
        }
        updateRoleUsers(roleId, userIdList);
    }

    @Transactional
    protected void updateRoleUsers(int roleId, List<Integer> userIdList){
        sysRoleUserMapper.deleteByRoleId(roleId);
        if(CollectionUtils.isEmpty(userIdList)){
            return;
        }
        List<SysRoleUser> roleUserList = Lists.newArrayList();
        for(Integer userId : userIdList){
            SysRoleUser roleUser = SysRoleUser.builder().roleId(roleId).userId(userId).operator(RequestHolder.getCurrentUser().getUsername())
                    .operateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest())).operateTime(new Date()).build();
            roleUserList.add(roleUser);
        }
        sysRoleUserMapper.batchInsert(roleUserList);
    }


}
