package com.hfc.pms_compiler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper class providing app-level unique request codes
 * for a round trip of the annotation processor.
 */
class RequestCodeProvider {

    static  AtomicInteger currentCode = new  AtomicInteger(0);

    public static int nextRequestCode (){
        return currentCode.getAndIncrement();
    }
}