package com.hfc.permissions_annotations.helper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.*;
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
    public  static PermissionRationHandler getPermissionWithCheck(Activity activity
            , OnPermissionCheckedLisener onPermissionCheckedLisener
            , OnPermissionShowRationaleLisener onPermissionShowRationaleLisener
            ,OnPermissionDeniedLisener onPermissionDeniedLisener ,int requestCode,String... permissions) {
        PermissionRationHandler handler =new PermissionRationHandler(activity,onPermissionCheckedLisener,onPermissionDeniedLisener,permissions);
        if (PermissionHelper.hasSelfPermissions(activity, permissions)) {
            onPermissionCheckedLisener.onPermissionChecked();
        } else {
            if (onPermissionShowRationaleLisener==null){
                   ActivityCompat.requestPermissions(activity, permissions, requestCode);
            }else {
                if (PermissionHelper.shouldShowRequestPermissionRationale(activity, permissions)) {
                    onPermissionShowRationaleLisener.onPermissionShowRationale(handler);
                } else {
                    ActivityCompat.requestPermissions(activity, permissions, requestCode);
                }
            }

        }
        return handler;
    }
    /**
     * Checks all given permissions have been granted.
     *
     * @param grantResults results
     * @return returns true if all permissions have been granted.
     */
    public static boolean verifyPermissions(int... grantResults) {
        if (grantResults.length == 0) {
            return false;
        }
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    public static void xxx(OnPermissionCheckedLisener onPermissionCheckedLisener){

    }
   public static void onRequestPermissionsResult(Activity target
           ,OnPermissionCheckedLisener onPermissionCheckedLisener
           ,OnPermissionDeniedLisener onPermissionDeniedLisener
           ,OnPermissionNeverAskAgainLisener onPermissionNeverAskAgainLisener
           ,int[] grantResults,String... permissions) {

                if (getTargetSdkVersion(target)<23 && !PermissionHelper.hasSelfPermissions(target, permissions)) {
                    onPermissionDeniedLisener.onPermissionDenied();
                    return;
                }
                if (PermissionHelper.verifyPermissions(grantResults)) {
                    onPermissionCheckedLisener.onPermissionChecked();
                } else {
                    if (!PermissionHelper.shouldShowRequestPermissionRationale(target, permissions)) {
                        onPermissionNeverAskAgainLisener.onPermissionNeverAsked();
                    } else {
                        onPermissionDeniedLisener.onPermissionDenied();
                    }
                }
    }
    /**
     * Get target sdk version.
     *
     * @param context context
     * @return target sdk version
     */
    private static volatile int targetSdkVersion = -1;
    @TargetApi(Build.VERSION_CODES.DONUT)
    public static int getTargetSdkVersion(Context context) {
        if (targetSdkVersion != -1) {
            return targetSdkVersion;
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            targetSdkVersion = packageInfo.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return targetSdkVersion;
    }
}
