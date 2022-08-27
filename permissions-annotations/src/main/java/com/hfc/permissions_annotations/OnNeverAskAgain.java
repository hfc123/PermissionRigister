package com.hfc.permissions_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author hongfuchang
 * @description:
 * @email 284424243@qq.com
 * @date :2022/3/30 14:51
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnNeverAskAgain {
    String[] value();
    int flag() default 0;
}
