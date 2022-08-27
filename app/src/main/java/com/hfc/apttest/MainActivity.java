package com.hfc.apttest;

import android.Manifest;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.hfc.permissions_annotations.NeedsPermission;
import com.hfc.permissions_annotations.OnNeverAskAgain;
import com.hfc.permissions_annotations.OnPermissionDenied;
import com.hfc.permissions_annotations.OnShowRationale;
import com.hfc.permissions_annotations.PermissionResult;
import com.hfc.permissions_annotations.helper.PermissionHelper;
import com.hfc.permissions_annotations.helper.PermissionRationHandler;

import java.lang.annotation.Retention;
import java.lang.reflect.Method;


public class MainActivity extends AppCompatActivity {

    private static  String TAG = MainActivity.class.getSimpleName();
    FragmentTest fragment ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*在activity对应java类中通过getFragmentManager()
         *获得FragmentManager，用于管理ViewGrop中的fragment
         * */
      FragmentManager fragmentManager=getSupportFragmentManager();
        /*FragmentManager要管理fragment（添加，替换以及其他的执行动作）
         *的一系列的事务变化，需要通过fragmentTransaction来操作执行
         */
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //实例化要管理的fragment
        fragment = new FragmentTest();
        //通过添加（事务处理的方式）将fragment加到对应的布局中
        fragmentTransaction.add(R.id.container,fragment);
        //事务处理完需要提交
        fragmentTransaction.commit();
//        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA}, 22);
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
        PermissionHelper.xxx(()->{});
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

        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
