package com.john.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 业务工具类
 */
public class LevelUtil {

    // 定义分隔符
    public final static String SEPARATOR = ".";

    public final static String ROOT = "0";

    // level 计算规则
    // 0
    // 0.1
    // 0.1.2
    // 0.2
    public static String calculateLevel(String parentLevel, int parentId){
        if(StringUtils.isBlank(parentLevel)){
            // 上一层为空， 则为根层
            System.out.println("root");
            return ROOT;
        }else{
            // 拼接， 父层加id
            System.out.println("root.level");
            return StringUtils.join(parentLevel, SEPARATOR, parentId);
        }
    }
}
