package com.john.dto;


import com.john.model.SysAcl;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

@Setter
@Getter
@ToString
public class AclDto extends SysAcl {

    // 渲染时，是否默认选中,
    private boolean checked = false;

    // 是否有权限操作， 用户操作时， 不能超过其已有权限的上限
    private boolean hasAcl = false;

    public static AclDto adapt(SysAcl acl){
        AclDto dto = new AclDto();
        BeanUtils.copyProperties(acl, dto);
        return dto;
    }
}
