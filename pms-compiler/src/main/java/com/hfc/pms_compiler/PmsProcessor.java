package com.hfc.pms_compiler;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.hfc.permissions_annotations.NeedsPermission;
import com.hfc.permissions_annotations.OnNeverAskAgain;
import com.hfc.permissions_annotations.OnPermissionDenied;
import com.hfc.permissions_annotations.OnShowRationale;
import com.squareup.javapoet.JavaFile;
import com.sun.source.util.Trees;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * @author hongfuchang
 * @description:
 * @email 284424243@qq.com
 * @date :2022/3/30 15:27
 **/
@AutoService(Processor.class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.DYNAMIC)
final  public class PmsProcessor extends AbstractProcessor {
    private static final String OPTION_SDK_INT = "Pms.minSdk";
    private static final String OPTION_DEBUGGABLE = "butterknife.debuggable";
    private static final String ANIMATION_TYPE = "android.view.animation.Animation";
    private int sdk = 1;
    private @Nullable
    Trees trees;
    private boolean debuggable;
    private Types typeUtils;
    private Filer filer;
    protected ProcessingEnvironment processingEnv;
    @Override
    public synchronized void init(ProcessingEnvironment env) {
        String sdk = env.getOptions().get(OPTION_SDK_INT);
        this.processingEnv = env;
        if (sdk != null) {
            try {
                this.sdk = Integer.parseInt(sdk);
            } catch (NumberFormatException e) {
                env.getMessager()
                        .printMessage(Diagnostic.Kind.WARNING, "Unable to parse supplied minSdk option '"
                                + sdk
                                + "'. Falling back to API 1 support.");
            }
        }

        debuggable = !"false".equals(env.getOptions().get(OPTION_DEBUGGABLE));

        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
        try {
            trees = Trees.instance(processingEnv);
        } catch (IllegalArgumentException ignored) {
            try {
                // Get original ProcessingEnvironment from Gradle-wrapped one or KAPT-wrapped one.
                for (Field field : processingEnv.getClass().getDeclaredFields()) {
                    if (field.getName().equals("delegate") || field.getName().equals("processingEnv")) {
                        field.setAccessible(true);
                        ProcessingEnvironment javacEnv = (ProcessingEnvironment) field.get(processingEnv);
                        trees = Trees.instance(javacEnv);
                        break;
                    }
                }
            } catch (Throwable ignored2) {
            }
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        /*processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,"xxxx22222");
        Set< Element> elements = (Set<Element>) roundEnvironment.getElementsAnnotatedWith(OnNeverAskAgain.class);
           for (Element element:elements){
               //获取方法名methodname logSomeThings
               processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,"methodname"+element.getSimpleName());
              //获取方法参数(java.lang.String[])void
               processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,"xxxx"+element.asType().toString());
               //获取类名 classnamecom.hfc.apttest.MainActivity
               processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,"classname"+element.getEnclosingElement().toString());
               //获取annotation的名称
               processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,"classname"+element.getAnnotationMirrors().toString());
               // com.sun.tools.javac.code.Symbol$MethodSymbol
               processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,"classname"+element.getClass().toString());

           }*/
        Map<TypeElement, BindingSet> bindingMap = findAndParseTargets(roundEnvironment);

        for (Map.Entry<TypeElement, BindingSet> entry : bindingMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            BindingSet binding = entry.getValue();
//            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,"xxxx22222");
            JavaFile javaFile = binding.brewJava(sdk, debuggable);
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                error(typeElement, "Unable to write binding for type %s: %s", typeElement, e.getMessage());
            }
        }

        return false;
    }

    private Map<TypeElement, BindingSet> findAndParseTargets(RoundEnvironment env) {
        Map<TypeElement, BindingSet.Builder> builderMap = new LinkedHashMap<>();
        Set<TypeElement> erasedTargetNames = new LinkedHashSet<>();

        // Process each @BindAnim element.
        for (Element element : env.getElementsAnnotatedWith(NeedsPermission.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                parseNeedsPermission(element, builderMap, erasedTargetNames);
            } catch (Exception e) {
                logParsingError(element, NeedsPermission.class, e);
            }
        }

        // Process each @BindArray element.
        for (Element element : env.getElementsAnnotatedWith(OnNeverAskAgain.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                parseOnNeverAskAgainPermission(element, builderMap, erasedTargetNames);
            } catch (Exception e) {
                logParsingError(element, NeedsPermission.class, e);
            }
        }

        // Process each @BindBitmap element.
        for (Element element : env.getElementsAnnotatedWith(OnPermissionDenied.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                parseDeniedPermission(element, builderMap, erasedTargetNames);
            } catch (Exception e) {
                logParsingError(element, NeedsPermission.class, e);
            }
        }
        // Process each @BindBitmap element.
        for (Element element : env.getElementsAnnotatedWith(OnShowRationale.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            try {
                parseOnShowRationalePermission(element, builderMap, erasedTargetNames);
            } catch (Exception e) {
                logParsingError(element, NeedsPermission.class, e);
            }
        }
// which starts at the roots (superclasses) and walks to the leafs (subclasses).
        Deque<Map.Entry<TypeElement, BindingSet.Builder>> entries =
                new ArrayDeque<>(builderMap.entrySet());
        Map<TypeElement, BindingSet> bindingMap = new LinkedHashMap<>();
        while (!entries.isEmpty()) {
            Map.Entry<TypeElement, BindingSet.Builder> entry = entries.removeFirst();

            TypeElement type = entry.getKey();
            BindingSet.Builder builder = entry.getValue();

            bindingMap.put(type, builder.build());

        }
        return bindingMap;
    }

    @Override
    public Set<String> getSupportedOptions() {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        builder.add(OPTION_SDK_INT, OPTION_DEBUGGABLE);

        if (trees != null) {
            builder.add(IncrementalAnnotationProcessorType.ISOLATING.getProcessorOption());
        }
        return builder.build();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }
    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(NeedsPermission.class);
        annotations.add(OnNeverAskAgain.class);
        annotations.add(OnPermissionDenied.class);
        annotations.add(OnShowRationale.class);
        return annotations;
    }
    //打印日志
    private void error(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, element, message, args);
    }

    private void note(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.NOTE, element, message, args);
    }

    private void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        processingEnv.getMessager().printMessage(kind, message, element);
    }
    private void logParsingError(Element element, Class<? extends Annotation> annotation,
                                 Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        error(element, "Unable to parse @%s binding.\n\n%s", annotation.getSimpleName(), stackTrace);
    }

    private BindingSet.Builder getOrCreateBindingBuilder(
            Map<TypeElement, BindingSet.Builder> builderMap, TypeElement enclosingElement) {
        BindingSet.Builder builder = builderMap.get(enclosingElement);
        if (builder == null) {
            builder = BindingSet.newBuilder(enclosingElement);
            builderMap.put(enclosingElement, builder);
        }
        return builder;
    }
    private void parseNeedsPermission(Element element,
                                        Map<TypeElement, BindingSet.Builder> builderMap, Set<TypeElement> erasedTargetNames) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();


        // Verify common generated code restrictions.
        hasError |= isInaccessibleViaGeneratedCode(NeedsPermission.class, "fields", element);
        hasError |= isBindingInWrongPackage(NeedsPermission.class, element);

        if (hasError) {
            return;
        }

        // Assemble information on the field.
        String name = element.getSimpleName().toString();
        String[] permissions = element.getAnnotation(NeedsPermission.class).value();
        int flag = element.getAnnotation(NeedsPermission.class).flag();

        BindingSet.Builder builder = getOrCreateBindingBuilder(builderMap, enclosingElement);
        builder.addNeedsElement(String.join(",",permissions)+flag,element);

        erasedTargetNames.add(enclosingElement);
    }
    private void parseDeniedPermission(Element element,
                                      Map<TypeElement, BindingSet.Builder> builderMap, Set<TypeElement> erasedTargetNames) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();


        // Verify common generated code restrictions.
        hasError |= isInaccessibleViaGeneratedCode(OnPermissionDenied.class, "fields", element);
        hasError |= isBindingInWrongPackage(OnPermissionDenied.class, element);

        if (hasError) {
            return;
        }

        // Assemble information on the field.
        String name = element.getSimpleName().toString();
        String[] permissions = element.getAnnotation(OnPermissionDenied.class).value();
        int flag = element.getAnnotation(OnPermissionDenied.class).flag();

        BindingSet.Builder builder = getOrCreateBindingBuilder(builderMap, enclosingElement);
        builder.addDeniedElement(String.join(",",permissions)+flag,element);

        erasedTargetNames.add(enclosingElement);
    }

    private void parseOnShowRationalePermission(Element element,
                                       Map<TypeElement, BindingSet.Builder> builderMap, Set<TypeElement> erasedTargetNames) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();


        // Verify common generated code restrictions.
        hasError |= isInaccessibleViaGeneratedCode(OnShowRationale.class, "fields", element);
        hasError |= isBindingInWrongPackage(OnShowRationale.class, element);

        if (hasError) {
            return;
        }

        // Assemble information on the field.
        String name = element.getSimpleName().toString();
        String[] permissions = element.getAnnotation(OnShowRationale.class).value();
        int flag = element.getAnnotation(OnShowRationale.class).flag();

        BindingSet.Builder builder = getOrCreateBindingBuilder(builderMap, enclosingElement);
        builder.addShowrationaleElement(String.join(",",permissions)+flag,element);

        erasedTargetNames.add(enclosingElement);
    }
    private void parseOnNeverAskAgainPermission(Element element,
                                                Map<TypeElement, BindingSet.Builder> builderMap, Set<TypeElement> erasedTargetNames) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();


        // Verify common generated code restrictions.
        hasError |= isInaccessibleViaGeneratedCode(OnNeverAskAgain.class, "fields", element);
        hasError |= isBindingInWrongPackage(OnNeverAskAgain.class, element);

        if (hasError) {
            return;
        }

        // Assemble information on the field.
        String name = element.getSimpleName().toString();
        String[] permissions = element.getAnnotation(OnNeverAskAgain.class).value();
        int flag = element.getAnnotation(OnNeverAskAgain.class).flag();

        BindingSet.Builder builder = getOrCreateBindingBuilder(builderMap, enclosingElement);
        builder.addNeveraskedElement(String.join(",",permissions)+flag,element);

        erasedTargetNames.add(enclosingElement);
    }
    private boolean isInaccessibleViaGeneratedCode(Class<? extends Annotation> annotationClass,
                                                   String targetThing, Element element) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify field or method modifiers.
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
            error(element, "@%s %s must not be private or static. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        // Verify containing type.
        if (enclosingElement.getKind() != CLASS) {
            error(enclosingElement, "@%s %s may only be contained in classes. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        // Verify containing class visibility is not private.
        if (enclosingElement.getModifiers().contains(PRIVATE)) {
            error(enclosingElement, "@%s %s may not be contained in private classes. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        return hasError;
    }

    private boolean isBindingInWrongPackage(Class<? extends Annotation> annotationClass,
                                            Element element) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        String qualifiedName = enclosingElement.getQualifiedName().toString();

        if (qualifiedName.startsWith("android.")) {
            error(element, "@%s-annotated class incorrectly in Android framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return true;
        }
        if (qualifiedName.startsWith("java.")) {
            error(element, "@%s-annotated class incorrectly in Java framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return true;
        }

        return false;
    }
}
