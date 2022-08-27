package com.hfc.pms_compiler;

import com.hfc.permissions_annotations.NeedsPermission;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import static com.google.auto.common.MoreElements.getPackage;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/** A set of all the bindings requested by a single type. */
final class BindingSet implements BindingInformationProvider {


  private final TypeName targetTypeName;
  private final ClassName bindingClassName;
  private final TypeElement enclosingElement;
  private final @Nullable BindingInformationProvider parentBinding;
  final Map<String ,Element> elementMap ;
  static final String ACTIVITY_TYPE = "android.app.Activity";
  static final String FRAGMENT_TYPE = "android.support.v4.app.Fragment";
  boolean isActivity,  isFragment;
  private BindingSet(
          TypeName targetTypeName, ClassName bindingClassName, TypeElement enclosingElement,
          @Nullable BindingInformationProvider parentBinding, Map<String, Element> elementMap, boolean isActivity, boolean isFragment) {
    this.targetTypeName = targetTypeName;
    this.bindingClassName = bindingClassName;
    this.enclosingElement = enclosingElement;

    this.parentBinding = parentBinding;

    this.elementMap = elementMap;
    this.isActivity = isActivity;
    this.isFragment = isFragment;
  }

  @Override
  public boolean constructorNeedsView() {
    return false;
  }

  @Override
  public ClassName getBindingClassName() {
    return bindingClassName;
  }

  JavaFile brewJava(int sdk, boolean debuggable) {
    TypeSpec bindingConfiguration = createType(sdk, debuggable);
    return JavaFile.builder(bindingClassName.packageName(), bindingConfiguration)
        .addFileComment("Generated code from Butter Knife. Do not modify!")
        .build();
  }

  private TypeSpec createType(int sdk, boolean debuggable) {
    TypeSpec.Builder result = TypeSpec.classBuilder(bindingClassName.simpleName())
        .addModifiers(PUBLIC)
        .addOriginatingElement(enclosingElement);
      result.addModifiers(FINAL);

    if (parentBinding != null) {
      result.superclass(parentBinding.getBindingClassName());
    }
      result.addField(targetTypeName, "target", PRIVATE);
      int index = 0 ;
     MethodSpec.Builder builder = createPermissionResultMethodBuilder();
      for (Map.Entry<String,Element> elements:elementMap.entrySet()){
        if (elements.getKey().contains(needs)){
          index++;
          Element element =elements.getValue();
          String[] permissions = element.getAnnotation(NeedsPermission.class).value();
          int flag = element.getAnnotation(NeedsPermission.class).flag();
          Element deniedElement =elementMap.get(String.join(",",permissions)+flag+denied);
          Element showrationaleElement =elementMap.get(String.join(",",permissions)+flag+showrationale);
          Element neveraskedElement =elementMap.get(String.join(",",permissions)+flag+neverasked);
          int requestCode;
          //添加fileds
          result.addField(createRequestCodeField(index,RequestCodeProvider.nextRequestCode()));
          result.addField(createPendingRequestField(index));
          result.addField(createPermissionCodeField(index,elements.getValue().getAnnotation(NeedsPermission.class).value()));
          String checkedCode="";String deniedCode="";String showrationaleCode="";String neveraskedCode="";
          String resultCheckedCode="";
          if (deniedElement!=null)
          deniedCode ="target."+deniedElement.getSimpleName()+"();";
          if (showrationaleElement!=null)
          showrationaleCode ="target."+showrationaleElement.getSimpleName()+"(handler);";
          if (neveraskedElement!=null)
          neveraskedCode ="target."+neveraskedElement.getSimpleName()+"();";
          if (element!=null){
            ExecutableElement executableElement = ((ExecutableElement) element);
            String params="";
            for (int i = 0; i < executableElement.getParameters().size(); i++) {
              String param=executableElement.getParameters().get(i).getSimpleName().toString();
              params+=","+param;
            }
            params=  params.replaceFirst(",","");
            checkedCode="target."+executableElement.getSimpleName()+"("+params+");";
          }
          //添加check方法
        result.addMethod(createWithCheckedMethod(index,(ExecutableElement) elements.getValue(),elements.getValue().getSimpleName()+"_WithCheched","requestCode"+index
                  ,"permissions"+index,checkedCode,deniedCode,showrationaleCode));
        resultCheckedCode="handler"+index+".grant();";
        createFlowCase(builder,resultCheckedCode,deniedCode,neveraskedCode,"requestCode"+index,"permissions"+index);
        }
      }
     addEndFlowCase(builder);
    result.addMethod(builder.build());

    return result.build();
  }

  private FieldSpec createPendingRequestField(int index) {
    return FieldSpec.builder(PERMISSIONHANDLE,"handler"+index)
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .build();
  }
  private FieldSpec createRequestCodeField(  int index,int value) {
    return FieldSpec.builder(TypeName.INT, "requestCode"+index)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("$L", value)
            .build();
  }

  private FieldSpec createPermissionCodeField(  int index,String[] value) {

  return FieldSpec.builder(String[].class, "permissions"+index)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("$L", "new String[]{\""+String.join("\",\"", value)+"\"}")
            .build();
  }
  protected ClassName PERMISSION_UTILS = ClassName.get("com.hfc.permissions_annotations.helper", "PermissionHelper");
  protected ClassName PERMISSIONHANDLE = ClassName.get("com.hfc.permissions_annotations.helper", "PermissionRationHandler");

  private MethodSpec createWithCheckedMethod(int index,ExecutableElement element,String name,String requestcode,String permission,String checkedCode,String deniedCode,String showrationaleCode) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder(name)
            .addModifiers(PUBLIC)
            .addModifiers(STATIC)
            .returns(TypeName.VOID)
            .addParameter(targetTypeName, "target");
    List<? extends VariableElement> list =  element.getParameters();
    for (int i = 0; i < list.size(); i++) {

      builder.addParameter(TypeName.get(list.get(i).asType()) ,list.get(i).getSimpleName().toString());
    }
    if (isActivity){
      if (showrationaleCode.equals("")){
        builder.addStatement("$T handler$N =$T.getPermissionWithCheck($N,()->{$N},null,()->{$N}, $N,$N);",PERMISSIONHANDLE,index+"",PERMISSION_UTILS,"target",checkedCode,deniedCode,requestcode,permission);
      }else {
        builder.addStatement("$T handler$N =$T.getPermissionWithCheck($N,()->{$N},(handler)->{$N},()->{$N}, $N,$N);",PERMISSIONHANDLE,index+"",PERMISSION_UTILS,"target",checkedCode,showrationaleCode,deniedCode,requestcode,permission);
      }
    }
    if (isFragment){
      if (showrationaleCode.equals("")){
        builder.addStatement("$T handler$N =$T.getPermissionWithCheck($N,()->{$N},null,()->{$N}, $N,$N);",PERMISSIONHANDLE,index+"",PERMISSION_UTILS,"target.getActivity()",checkedCode,deniedCode,requestcode,permission);
      }else {
        builder.addStatement("$T handler$N =$T.getPermissionWithCheck($N,()->{$N},(handler)->{$N},()->{$N}, $N,$N);",PERMISSIONHANDLE,index+"",PERMISSION_UTILS,"target.getActivity()",checkedCode,showrationaleCode,deniedCode,requestcode,permission);
      }
    }
    builder.addStatement(bindingClassName.simpleName()+".$N=$N","handler"+index,"handler"+index);
    return builder.build();
  }

  private static TypeName bestGuess(String type) {
    switch (type) {
      case "void": return TypeName.VOID;
      case "boolean": return TypeName.BOOLEAN;
      case "byte": return TypeName.BYTE;
      case "char": return TypeName.CHAR;
      case "double": return TypeName.DOUBLE;
      case "float": return TypeName.FLOAT;
      case "int": return TypeName.INT;
      case "long": return TypeName.LONG;
      case "short": return TypeName.SHORT;
      default:
        int left = type.indexOf('<');
        if (left != -1) {
          ClassName typeClassName = ClassName.bestGuess(type.substring(0, left));
          List<TypeName> typeArguments = new ArrayList<>();
          do {
            typeArguments.add(WildcardTypeName.subtypeOf(Object.class));
            left = type.indexOf('<', left + 1);
          } while (left != -1);
          return ParameterizedTypeName.get(typeClassName,
              typeArguments.toArray(new TypeName[typeArguments.size()]));
        }
        return ClassName.bestGuess(type);
    }
  }





  @Override public String toString() {
    return bindingClassName.toString();
  }

  static Builder newBuilder(TypeElement enclosingElement) {
    TypeMirror typeMirror = enclosingElement.asType();
    TypeName targetType = TypeName.get(typeMirror);
    if (targetType instanceof ParameterizedTypeName) {
      targetType = ((ParameterizedTypeName) targetType).rawType;
    }
    ClassName bindingClassName = getBindingClassName(enclosingElement);
    return new Builder(targetType, bindingClassName, enclosingElement);
  }

  static ClassName getBindingClassName(TypeElement typeElement) {
    String packageName = getPackage(typeElement).getQualifiedName().toString();
    String className = typeElement.getQualifiedName().toString().substring(
            packageName.length() + 1).replace('.', '$');
    return ClassName.get(packageName, className + "_Permission");
  }
  static String getBindingClassName(Element element) {
    String methodName =element.getSimpleName().toString();
    return methodName + "_WithChecked";
  }

  static final class Builder {
    private final TypeName targetTypeName;
    private final ClassName bindingClassName;
    private final TypeElement enclosingElement;
    Map<String ,Element> elementMap = new HashMap<>();
;


    private @Nullable BindingInformationProvider parentBinding;
    boolean isActivity ;
    boolean isFragment ;
    private Builder(
        TypeName targetTypeName, ClassName bindingClassName, TypeElement enclosingElement) {
      this.targetTypeName = targetTypeName;
      this.bindingClassName = bindingClassName;
      this.enclosingElement = enclosingElement;
      TypeMirror typeMirror = enclosingElement.asType();


       isActivity = isSubtypeOfType(typeMirror, ACTIVITY_TYPE);
       isFragment = isSubtypeOfType(typeMirror, FRAGMENT_TYPE);
      if (!isFragment&&!isActivity){
        throw new IllegalStateException("permission annotation must bind activy or fragment");
      }
    }

    BindingSet build() {

      return new BindingSet(targetTypeName, bindingClassName, enclosingElement, parentBinding,elementMap,isActivity,isFragment);
    }
    public void addNeedsElement(String key,Element element){
      elementMap.put(key+"needs",element);
    }
    public void addDeniedElement(String key,Element element){
      elementMap.put(key+"denied",element);
    }
    public void addNeveraskedElement(String key,Element element){
      elementMap.put(key+"neverasked",element);
    }
    public void addShowrationaleElement(String key,Element element){
      elementMap.put(key+"showrationale",element);
    }

    static boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
      if (isTypeEqual(typeMirror, otherType)) {
        return true;
      }
      if (typeMirror.getKind() != TypeKind.DECLARED) {
        return false;
      }
      DeclaredType declaredType = (DeclaredType) typeMirror;
      List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
      if (typeArguments.size() > 0) {
        StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
        typeString.append('<');
        for (int i = 0; i < typeArguments.size(); i++) {
          if (i > 0) {
            typeString.append(',');
          }
          typeString.append('?');
        }
        typeString.append('>');
        if (typeString.toString().equals(otherType)) {
          return true;
        }
      }
      Element element = declaredType.asElement();
      if (!(element instanceof TypeElement)) {
        return false;
      }
      TypeElement typeElement = (TypeElement) element;
      TypeMirror superType = typeElement.getSuperclass();
      if (isSubtypeOfType(superType, otherType)) {
        return true;
      }
      for (TypeMirror interfaceType : typeElement.getInterfaces()) {
        if (isSubtypeOfType(interfaceType, otherType)) {
          return true;
        }
      }
      return false;
    }
    private static boolean isTypeEqual(TypeMirror typeMirror, String otherType) {
      return otherType.equals(typeMirror.toString());
    }
  }
  public static String needs="needs";
  public static String denied="denied";
  public static String neverasked="neverasked";
  public static String showrationale="showrationale";

  private MethodSpec.Builder createPermissionResultMethodBuilder() {
        String targetParam = "target";
        String requestCodeParam = "requestCode";
        String grantResultsParam = "grantResults";
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onRequestPermissionsResult")
        .addModifiers(Modifier.STATIC)
        .addModifiers(PUBLIC)
        .returns(TypeName.VOID)
        .addParameter(targetTypeName, targetParam)
        .addParameter(TypeName.INT, requestCodeParam)
        .addParameter(ArrayTypeName.of(TypeName.INT), grantResultsParam);

        // For each @NeedsPermission method, add a switch case
        builder.beginControlFlow("switch ($N)", requestCodeParam);

        return builder;
        }

        public void  createFlowCase(MethodSpec.Builder builder,String checkedCode,String deniedCode,String neveraskedCode,String requestcode,String permissions){
          builder.addCode("case $N:",requestcode);
          if (isActivity){
            builder.addStatement("$T.onRequestPermissionsResult(target,()->{$N},()->{$N},()->{$N},$N,$N);", PERMISSION_UTILS, checkedCode, deniedCode, neveraskedCode,"grantResults",permissions);
          }
          if (isFragment){
            builder.addStatement("$T.onRequestPermissionsResult(target.getActivity(),()->{$N},()->{$N},()->{$N},$N,$N);", PERMISSION_UTILS, checkedCode, deniedCode, neveraskedCode,"grantResults",permissions);
          }
          builder.addStatement("break");
        }
        public void addEndFlowCase(MethodSpec.Builder builder){
          // Add the default case
          builder
                  .addCode("default:")
                  .addStatement("break")
                  .endControlFlow();
        }
}
interface BindingInformationProvider {
  boolean constructorNeedsView();
  ClassName getBindingClassName();
}

final class ClasspathBindingSet implements BindingInformationProvider {
  private boolean constructorNeedsView;
  private ClassName className;

  ClasspathBindingSet(boolean constructorNeedsView, TypeElement classElement) {
    this.constructorNeedsView = constructorNeedsView;
    this.className = BindingSet.getBindingClassName(classElement);
  }

  @Override
  public ClassName getBindingClassName() {
    return className;
  }

  @Override
  public boolean constructorNeedsView() {
    return constructorNeedsView;
  }

}
