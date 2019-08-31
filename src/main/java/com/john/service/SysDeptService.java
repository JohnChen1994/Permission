package com.john.service;

import com.google.common.base.Preconditions;
import com.john.common.RequestHolder;
import com.john.dao.SysDeptMapper;
import com.john.dao.SysUserMapper;
import com.john.exception.ParamException;
import com.john.model.SysDept;
import com.john.param.DeptParam;
import com.john.util.BeanValidator;
import com.john.util.IpUtil;
import com.john.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class SysDeptService {

    @Resource
    private SysDeptMapper sysDeptMapper;
    @Resource
    private SysUserMapper sysUserMapper;

    public void save(DeptParam param){
        // controller 中的save方法
        // 参数校验
        BeanValidator.check(param);
        if(checkExist(param.getParentId(), param.getName(), param.getId())){
            // 部门相同  抛出异常到前端
            throw new ParamException("同一层级下存在相同名称的部门");
        }
        // 通过构造方法定义类
        SysDept dept = SysDept.builder().name(param.getName()).parentId(param.getParentId())
                .seq(param.getSeq()).remark(param.getRemark()).build();
        // level 计算
        dept.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()), param.getParentId()));

        dept.setOperator(RequestHolder.getCurrentUser().getUsername());
        dept.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));  // TODO
        dept.setOperateTime(new Date());
        // insert 插入全部字段  insertSelective 值为空则不插入
        sysDeptMapper.insertSelective(dept);
    }

    public void update(DeptParam param){
        // controller 中的update方法
        // 参数校验
        BeanValidator.check(param);
        if(checkExist(param.getParentId(), param.getName(), param.getId())){
            // 部门相同  抛出异常到前端
            throw new ParamException("同一层级下存在相同名称的部门");
        }
        SysDept before = sysDeptMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(before,"待更新部门不存在");
        if(checkExist(param.getParentId(), param.getName(), param.getId())){
            // 部门相同  抛出异常到前端
            throw new ParamException("同一层级下存在相同名称的部门");
        }

        // 更新之后的部门
        SysDept after = SysDept.builder().id(param.getId()).name(param.getName()).parentId(param.getParentId())
                .seq(param.getSeq()).remark(param.getRemark()).build();
        after.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()), param.getParentId()));
        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        after.setOperateIp("127.0.0.1");  // TODO
        after.setOperateTime(new Date());

        updateWithChild(before, after);
    }

    @Transactional
    protected void updateWithChild(SysDept before, SysDept after){


        // 判断是否更新子部门
        String newLevelPrefix = after.getLevel();
        String oldLevelPrefix = before.getLevel();
        if (!after.getLevel().equals(before.getLevel())){
            List<SysDept> deptList = sysDeptMapper.getChildDeptListByLevel(before.getLevel());
            if(CollectionUtils.isNotEmpty(deptList)){
                for(SysDept dept : deptList){
                    String level = dept.getLevel();
                    if(level.indexOf(oldLevelPrefix) == 0){
                        level = newLevelPrefix + level.substring(oldLevelPrefix.length());
                        dept.setLevel(level);
                    }
                }
                sysDeptMapper.batchUpdateLevel(deptList);
            }
        }

        sysDeptMapper.updateByPrimaryKey(after);

    }

    // 检验部门是否重复
    private boolean checkExist(Integer parentId, String deptName, Integer deptId) {
        // 判断数据是否重复
        return sysDeptMapper.countByNameAndParentId(parentId, deptName, deptId) > 0;
    }

    private String getLevel(Integer deptId){
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        System.out.println(deptId + " " + dept);
        if(dept == null){
            return null;
        }
        return dept.getLevel();
    }

    // 删除部门
    public void delete(int deptId){
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        Preconditions.checkNotNull(dept, "待删除的部门不存在， 无法删除");
        if (sysDeptMapper.countByParentId(dept.getId()) > 0){
            throw new ParamException("当前部门下面有子部门， 无法删除");
        }
        if(sysUserMapper.countByDeptId(dept.getId()) > 0){
            throw new ParamException("当前部门下面有用户， 无法删除");
        }
        sysDeptMapper.deleteByPrimaryKey(deptId);
    }
}

























