package com.hfc.apttest;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;

/**
 * @author hongfuchang
 * @description:
 * @email 284424243@qq.com
 * @date :2022/8/23 17:49
 **/
public class PermissionHelper {


    //判断是否有权限
    public static boolean hasSelfPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (!hasSelfPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }
    //判断是否有权限
    public static boolean hasSelfPermissions(Fragment fragment, String... permissions) {
        for (String permission : permissions) {
            if (!hasSelfPermission(fragment.getContext(), permission)) {
                return false;
            }
        }
        return true;
    }
    private static boolean hasSelfPermission(Context context, String permission) {
        try {
            return PermissionChecker.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        } catch (RuntimeException t) {
            return false;
        }
    }
    public static boolean shouldShowRequestPermissionRationale(Activity activity, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }
}
