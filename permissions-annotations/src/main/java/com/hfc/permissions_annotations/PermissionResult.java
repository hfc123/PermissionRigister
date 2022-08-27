package com.hfc.permissions_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author hongfuchang
 * @description:回调方法上注解
 * @email 284424243@qq.com
 * @date :2022/8/26 22:21
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionResult {
}
