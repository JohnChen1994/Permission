package com.john.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.john.dao.SysAclMapper;
import com.john.dao.SysAclModuleMapper;
import com.john.dao.SysDeptMapper;
import com.john.dto.AclDto;
import com.john.dto.AclModuleLevelDto;
import com.john.dto.DeptLevelDto;
import com.john.model.SysAcl;
import com.john.model.SysAclModule;
import com.john.model.SysDept;
import com.john.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysTreeService {

    @Resource
    private SysDeptMapper sysDeptMapper;
    @Resource
    private SysAclModuleMapper sysAclModuleMapper;
    @Resource
    private SysCoreService sysCoreService;
    @Resource
    private SysAclMapper sysAclMapper;

    public List<AclModuleLevelDto> userAclTree(int userId){
        List<SysAcl> userAclList = sysCoreService.getUserAclList(userId);
        List<AclDto> aclDtoList = Lists.newArrayList();
        for(SysAcl acl : userAclList){
            AclDto dto = AclDto.adapt(acl);
            dto.setHasAcl(true);
            dto.setChecked(true);
            aclDtoList.add(dto);
        }
        return aclListToTree(aclDtoList);
    }

    public List<AclModuleLevelDto> roleTree(int roleId){
        // 权限模块和权限点生成权限树
        // 1. 当前用户已经分配的权限点
        List<SysAcl> userAclList = sysCoreService.getCurrentUserAclList();
        // 2. 当前角色分配的权限点
        List<SysAcl> roleAclList = sysCoreService.getRoleAclList(roleId);
        // 3. 当前系统所有权限点
        List<AclDto> aclDtoList = Lists.newArrayList();

        //取出用户以分配的AclId
        Set<Integer> userAclIdSet = userAclList.stream().map(sysAcl -> sysAcl.getId()).collect(Collectors.toSet());
        // 角色以分配的Id集合  stream java8 高级语法，遍历，map 将取出的值转换
        Set<Integer> roleAclIdSet = roleAclList.stream().map(sysAcl -> sysAcl.getId()).collect(Collectors.toSet());

        // 并集
        List<SysAcl> allAclList = sysAclMapper.getAll();
        /*Set<SysAcl> aclSet = new HashSet<>(roleAclList);
        aclSet.addAll(userAclList);*/
        //Set<SysAcl> aclSet = new HashSet<>(allAclList);

        // 遍历系统所有权限点，判断当前用户是否用权限访问
        for(SysAcl acl : allAclList){
            AclDto dto = AclDto.adapt(acl);
            if(userAclIdSet.contains(acl.getId())){
                dto.setHasAcl(true);
            }
            if(roleAclIdSet.contains(acl.getId())){
                dto.setChecked(true);
            }
            aclDtoList.add(dto);
        }
        return aclListToTree(aclDtoList);
    }

    public List<AclModuleLevelDto> aclListToTree(List<AclDto> aclDtoList){
        if(CollectionUtils.isEmpty(aclDtoList)){
            return Lists.newArrayList();
        }
        List<AclModuleLevelDto> aclModuleLevelDtoList = aclModuleTree();

        Multimap<Integer, AclDto> moduleIdAclMap = ArrayListMultimap.create();

        for(AclDto acl : aclDtoList){
            if(acl.getStatus() == 1){
                moduleIdAclMap.put(acl.getAclModuleId(), acl);
            }
        }
        bindAclsWithOrder(aclModuleLevelDtoList, moduleIdAclMap);
        return aclModuleLevelDtoList;
    }


    // 将权限点绑定在权限模块树上
    public void bindAclsWithOrder(List<AclModuleLevelDto> aclModuleLevelDtoList, Multimap<Integer, AclDto> moduleIdAclMap){
        if(CollectionUtils.isEmpty(aclModuleLevelDtoList)){
            return;
        }
        for(AclModuleLevelDto dto : aclModuleLevelDtoList){
            List<AclDto> aclDtoList =(List<AclDto>) moduleIdAclMap.get(dto.getId());
            if(CollectionUtils.isNotEmpty(aclDtoList)){
                Collections.sort(aclDtoList,aclSeqComparator);
                dto.setAclList(aclDtoList);
            }
            bindAclsWithOrder(dto.getAclModuleList(), moduleIdAclMap);
        }
    }

    public List<AclModuleLevelDto> aclModuleTree(){
        // 取出所有权限模块
        List<SysAclModule> aclModuleList = sysAclModuleMapper.getAllAclModule();
        // 遍历树形结构
        List<AclModuleLevelDto> dtoList = Lists.newArrayList();
        for(SysAclModule aclModule : aclModuleList){
            dtoList.add(AclModuleLevelDto.adapt(aclModule));
        }
        return aclModultListToTree(dtoList);
    }

    // 根据当前结构适配出权限树
    public List<AclModuleLevelDto> aclModultListToTree(List<AclModuleLevelDto> dtoList){
        // 准备数据，
        if(CollectionUtils.isEmpty(dtoList)){
            return Lists.newArrayList();  // 返回一个空的树
        }
        // level -> [aclmodule1, aclmodule2, ... ] Map<String, List<Object>>
        Multimap<String, AclModuleLevelDto> levelAclModuleMap = ArrayListMultimap.create();
        List<AclModuleLevelDto> rootList = Lists.newArrayList();

        for(AclModuleLevelDto dto : dtoList){
            levelAclModuleMap.put(dto.getLevel(), dto);
            if(LevelUtil.ROOT.equals(dto.getLevel())){
                rootList.add(dto);
            }
        }
        Collections.sort(rootList, aclModuleLevelDtoComparator);
        transformAclModuleTree(rootList, LevelUtil.ROOT, levelAclModuleMap);
        return rootList;
    }

    // 转换为树形结构方法
    public void transformAclModuleTree(List<AclModuleLevelDto> dtoList, String level, Multimap levelAclModuleMap){
        for(int i = 0  ; i < dtoList.size(); i++){
            // 遍历出当前层
            AclModuleLevelDto dto = dtoList.get(i);
            // 层级的level
            String nextLevel = LevelUtil.calculateLevel(level, dto.getId());
            //下一个层级的列表
            List<AclModuleLevelDto> tempList = (List<AclModuleLevelDto>)levelAclModuleMap.get(nextLevel);
            // 判断下一层是否有权限模块
            if(CollectionUtils.isNotEmpty(tempList)){
                // 排序
                Collections.sort(tempList,aclModuleLevelDtoComparator);
                // set方法
                dto.setAclModuleList(tempList);
                // 递归处理下一层
                transformAclModuleTree(tempList, nextLevel, levelAclModuleMap);
            }
        }
    }


    public List<DeptLevelDto> deptTree(){
        // 取出基本数据
        List<SysDept> deptList = sysDeptMapper.getAllDept();

        // 数据封装
        List<DeptLevelDto> dtoList = Lists.newArrayList();
        for(SysDept dept : deptList){
            DeptLevelDto dto = DeptLevelDto.adapt(dept);
            dtoList.add(dto);
        }
        return deptListToTree(dtoList);
    }

    public List<DeptLevelDto> deptListToTree(List<DeptLevelDto> deptLevelList){
        if(CollectionUtils.isEmpty(deptLevelList)){
            return Lists.newArrayList();
        }
        // level -> [dept1, dept2, ...]  数据核心组装  Multimap:  Map<String, List<Object>>
        Multimap<String, DeptLevelDto> levelDeptMap = ArrayListMultimap.create();
        List<DeptLevelDto> rootList = Lists.newArrayList();

        // 处理首层 root
        for(DeptLevelDto dto : deptLevelList){
            levelDeptMap.put(dto.getLevel(), dto);
            if(LevelUtil.ROOT.equals(dto.getLevel())){
                rootList.add(dto);
            }
        }

        // 按照seq从小到大排序
        Collections.sort(rootList, new Comparator<DeptLevelDto>(){
            public int compare(DeptLevelDto o1, DeptLevelDto o2){
                return o1.getSeq() - o2.getSeq();
            }
        });
        // 递归生成树
        transformDeptTree(rootList, LevelUtil.ROOT, levelDeptMap);
        return rootList;
    }

    // 递归排序 level:0, 0, all 0->0.1 0.2
    // level : 0.1
    // level : 0.2
    // 当前层级列表， 层级  ， 辅助计算map  递归三要素
    public void transformDeptTree(List<DeptLevelDto> deptLevelList, String level, Multimap<String, DeptLevelDto> levelDeptMap){
        // 按层级处理
        for(int i = 0; i < deptLevelList.size(); i++){
            // 遍历该层的每个元素
            DeptLevelDto deptLevelDto = deptLevelList.get(i);
            // 处理当前层级的数据
            String nextLevel = LevelUtil.calculateLevel(level, deptLevelDto.getId());
            // 处理下一层
            List<DeptLevelDto> tempDeptList = (List<DeptLevelDto>)levelDeptMap.get(nextLevel);
            if(CollectionUtils.isNotEmpty(tempDeptList)){
                // 排序
                Collections.sort(tempDeptList, deptSeqComparator);
                // 设置下一层部门
                deptLevelDto.setDeptList(tempDeptList);
                // 进入到下一层处理
                transformDeptTree(tempDeptList, nextLevel, levelDeptMap);
            }
        }
    }

    public Comparator<DeptLevelDto> deptSeqComparator = new Comparator<DeptLevelDto>() {
        @Override
        public int compare(DeptLevelDto o1, DeptLevelDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };

    // 权限模块比较器
    public Comparator<AclModuleLevelDto> aclModuleLevelDtoComparator = new Comparator<AclModuleLevelDto>() {
        @Override
        public int compare(AclModuleLevelDto o1, AclModuleLevelDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };

    public Comparator<AclDto> aclSeqComparator = new Comparator<AclDto>() {
        @Override
        public int compare(AclDto o1, AclDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };
}
