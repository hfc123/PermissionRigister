package com.hfc.permissions_annotations.helper;

/**
 * @author hongfuchang
 * @description:
 * @email 284424243@qq.com
 * @date :2022/8/25 14:53
 **/
@FunctionalInterface
public interface OnPermissionDeniedLisener {
   void onPermissionDenied();
}
