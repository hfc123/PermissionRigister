package com.hfc.apttest;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hfc.permissions_annotations.NeedsPermission;
import com.hfc.permissions_annotations.OnNeverAskAgain;
import com.hfc.permissions_annotations.OnPermissionDenied;
import com.hfc.permissions_annotations.OnShowRationale;
import com.hfc.permissions_annotations.PermissionResult;
import com.hfc.permissions_annotations.helper.PermissionHelper;
import com.hfc.permissions_annotations.helper.PermissionRationHandler;

/**
 * @author hongfuchang
 * @description:
 * @email 284424243@qq.com
 * @date :2022/8/27 1:20
 **/
public class FragmentTest extends Fragment {
    private static  String TAG = FragmentTest.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_main,container,false);
        logSomeThings("hello","aspectj","hello","world");
        return view;
    }

    @NeedsPermission(value = { Manifest.permission.CAMERA},flag = 1)
    public void logSomeThings(String...args){
        for (String str: args) {
            Log.d(TAG, "NeedsPermission: "+str);
        }
    }
    @OnNeverAskAgain(value = { Manifest.permission.CAMERA},flag = 1)
    public void insertmethod(){

        Log.d(TAG, "OnNeverAskAgain: ");
        com.hfc.permissions_annotations.helper.PermissionHelper.xxx(()->{});
    }
    @OnPermissionDenied(value = {Manifest.permission.CAMERA},flag = 1)
    public void insertmethod2(){

        Log.d(TAG, "OnPermissionDenied: ");
        PermissionHelper.xxx(()->{});
    }
    @OnShowRationale(value ={ Manifest.permission.CAMERA},flag = 1)
    public void insertmethod3(PermissionRationHandler handler){

        Log.d(TAG, "OnShowRationale: ");
        handler.getAgain();
    }
    @Override
    @PermissionResult
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "onRequestPermissionsResult: ");
    }
}
