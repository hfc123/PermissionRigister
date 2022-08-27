package com.hfc.pms_compiler;

import com.squareup.javapoet.TypeName;

/**
 * @author hongfuchang
 * @description:
 * @email 284424243@qq.com
 * @date :2022/8/25 1:11
 **/
public class FieldBean {

    public  TypeName type;
    public  String name;
    public  String value;
    public  String format="\\$L";
//    Element element;
    public TypeName getType() {
        return type;
    }

    public void setType(TypeName type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
