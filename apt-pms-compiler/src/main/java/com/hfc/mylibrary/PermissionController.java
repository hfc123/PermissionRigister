package com.hfc.mylibrary;

import android.util.Log;

import com.hfc.permissions_annotations.NeedsPermission;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * @author hongfuchang
 * @description:
 * @email 284424243@qq.com
 * @date :2022/3/24 11:47
 **/
@Aspect
public class PermissionController {
   // within(@hugo.weaving.DebugLog *)
//    @com.hfc.permissions_annotations.NeedsPermission
    @Pointcut("within(@com.hfc.permissions_annotations.NeedsPermission *)")
    public void withinannotationedClass(){}
//    execution(!synthetic * *(..)) && withinAnnotatedClass()
    @Pointcut("execution(!synthetic * *(..)) && withinannotationedClass()")
    public void methodinsideannotationtye(){

    }
    @Pointcut("execution(!synthetic *.new(..)) && withinannotationedClass()")
    public void constructorInsideAnnotatedType(){}
    @Pointcut("execution(@com.hfc.permissions_annotations.NeedsPermission * *(..)) || methodinsideannotationtye()")
    public void method() {}


    @Pointcut("execution(@com.hfc.permissions_annotations.NeedsPermission * *(..)) || constructorInsideAnnotatedType()")
    public void constructor() {}

    @Pointcut("execution(@java.lang.Override * onRequestPermissionsResult(..))")
    public void onRequestPermissionsResultMethod() {}
    @Pointcut("execution(@com.hfc.permissions_annotations.PermissionResult * *(..)) || onRequestPermissionsResultMethod()")
    public void onRequestPermissionsResultMethodWithAnnotation() {}
    //    @Pointcut("* com.lerendan.*.*(..)")public * *(..)
    @Pointcut("execution(public * logSomeThings(..))")
    public void testmethod() {}

    @Around("onRequestPermissionsResultMethodWithAnnotation()")
    public void modifyPermissionsResultMethod(ProceedingJoinPoint proceedingJoinPoint){

        CodeSignature signature = ((CodeSignature) proceedingJoinPoint.getSignature());
        Object object = proceedingJoinPoint.getTarget();
        try {
            proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        //  Log.d(TAG, "startLog: "+signature.getParameterTypes().length);
        try {
            Class aClass =Class.forName(object.getClass().getName()+"_Permission");

            Class[] parameterTypes =new Class[signature.getParameterTypes().length];
            Object[] objects =new Object[signature.getParameterTypes().length];
            parameterTypes[0]=object.getClass();
            parameterTypes[1]=int.class;
            parameterTypes[2]=int[].class;
            objects[0]=object;
            objects[1]=proceedingJoinPoint.getArgs()[0];
            objects[2]=proceedingJoinPoint.getArgs()[2];
            Method m1 = aClass.getMethod(signature.getName(),parameterTypes);
            m1.invoke(null,objects);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    @Around("method()&& @annotation(needPermission)")
    public void modifyNeedsPermissionMethod(ProceedingJoinPoint proceedingJoinPoint,NeedsPermission needPermission){

        CodeSignature signature = ((CodeSignature) proceedingJoinPoint.getSignature());
        Object object = proceedingJoinPoint.getTarget();
        String[] permissions=needPermission.value();
//        Log.d(TAG, "startLog: "+signature.getName());
        try {
            Class nClass =Class.forName("com.hfc.permissions_annotations.helper.PermissionHelper");
            Class contextActivityClass =Class.forName("android.content.Context");
            Class contextFragmentClass =Class.forName("android.support.v4.app.Fragment");
            boolean hasSelfPermissions=false;
            if (nClass.isAssignableFrom(object.getClass()) ){
                Method m2= nClass.getMethod("hasSelfPermissions",contextActivityClass,String[].class);
                 hasSelfPermissions = (boolean) m2.invoke(null,object,permissions);
            }
            if (contextFragmentClass.isAssignableFrom(object.getClass()) ){
                Method m2= nClass.getMethod("hasSelfPermissions",contextFragmentClass,String[].class);
                hasSelfPermissions = (boolean) m2.invoke(null,object,permissions);
            }
            if (!nClass.isAssignableFrom(object.getClass())&&!contextFragmentClass.isAssignableFrom(object.getClass())){
                throw new IllegalArgumentException("annotation bind method error;can not get fragment or activity");
            }
            if (hasSelfPermissions){
                //有权限执行该方法
                proceedingJoinPoint.proceed();
                return;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return;
        }
        try {
            Class aClass =Class.forName(object.getClass().getName()+"_Permission");

            Class[] parameterTypes =new Class[signature.getParameterTypes().length+1];
            Object[] objects =new Object[signature.getParameterTypes().length+1];
            parameterTypes[0]=object.getClass();
            objects[0]=object;
            for (int i = 1; i < signature.getParameterTypes().length + 1; i++) {
                parameterTypes[i]=signature.getParameterTypes()[i-1];
                objects[i]=proceedingJoinPoint.getArgs()[i-1];
            }
            Method m1 = aClass.getMethod(signature.getName()+"_WithCheched",parameterTypes);
            m1.invoke(null,objects);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

       /* Log.d(TAG, "startLog3: "+signature.getDeclaringTypeName());
        Log.d(TAG, "startLog3: "+needPermission.value().toString());

       Log.d(TAG, "startLog: "+((MethodSignature) proceedingJoinPoint).getMethod().getAnnotation(com.hfc.permissions_annotations.NeedsPermission.class));

        Object obj =null;*/
    /*    try {
            obj = proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }*/

  /*      for(Field field :proceedingJoinPoint.getTarget().getClass().getDeclaredFields()){
           Log.d(TAG, "startLog2: "+field.getName().toString());
       }
        for(Annotation field :proceedingJoinPoint.getTarget().getClass().getAnnotations()){
           Log.d(TAG, "startLog3: "+field.getClass().toString());
       }
        Log.d(TAG, "startLog: "+signature.getName());
        Log.d(TAG, "startLog: "+signature.getParameterNames().toString());
        Log.d(TAG, "startLog: "+signature.getModifiers());
        Log.d(TAG, "startLog: "+arrayToString(signature.getParameterNames()));
        if (signature instanceof MethodSignature &&
                ((MethodSignature) signature).getReturnType()!=void.class){
            Log.d(TAG, "startLog: "+obj.toString());
        }*/

    }
    public String arrayToString( String[]args){
        StringBuilder builder =new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            Log.d(TAG, "arrayToString: "+args[i]);
            builder.append(args[i]);
        }
        return builder.toString();
    }
}


