package com.hfc.permissions_annotations.helper;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import java.lang.ref.WeakReference;

/**
 * @author hongfuchang
 * @description:
 * @email 284424243@qq.com
 * @date :2022/8/25 15:13
 **/
public class PermissionRationHandler {
    private final WeakReference<Activity> weakTarget;
    OnPermissionDeniedLisener onPermissionDeniedLisener;
    OnPermissionCheckedLisener onPermissionCheckedLisener;

    String[] permissions;
    int requestCode;
    public PermissionRationHandler(Activity target,  OnPermissionCheckedLisener onPermissionCheckedLisener,OnPermissionDeniedLisener onPermissionDeniedLisener,String[] permissions) {
        this.weakTarget = new WeakReference<>(target);
        this.onPermissionDeniedLisener =onPermissionDeniedLisener;
        this.onPermissionCheckedLisener =onPermissionCheckedLisener;
        this.permissions =permissions;
    }

    public void getAgain() {
        Activity target = weakTarget.get();
        if (target == null) return;
        ActivityCompat.requestPermissions(target, permissions, requestCode);
    }

    public void cancel() {
        if (onPermissionDeniedLisener==null) return;
            onPermissionDeniedLisener.onPermissionDenied();
    }
    //don't use this method
    public void grant() {
        if (onPermissionDeniedLisener==null) return;
        onPermissionCheckedLisener.onPermissionChecked();
    }
}
