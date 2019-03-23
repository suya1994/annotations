package com.example.processor;

import com.example.annotations.Factory;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class FactoryGroup {

    /**
     * key:Factory type 类型的合法全名
     */
    private Map<String,Set<FactoryAnnotationClass>> mGroupMap;

    FactoryGroup() {
        mGroupMap = new HashMap<>();
    }


     void add(FactoryAnnotationClass annotationClass) throws ProcessorException {
        Set<FactoryAnnotationClass> set = mGroupMap.get(annotationClass.getSuperTypeName());

        if (null == set){
            set = new HashSet<>();
            set.add(annotationClass);
            mGroupMap.put(annotationClass.getSuperTypeName(),set);
        }else {
            for (FactoryAnnotationClass factoryAnnotationClass : set){
                //3.id只能是String类型，并且在同一个type组中必须唯一;
                if (annotationClass.getId().equals(factoryAnnotationClass.getId())){
                    throw new ProcessorException(annotationClass.getElement(),
                            "Conflict: The class %s annotated with @%s with id ='%s' but %s already uses the same id",
                            annotationClass.getElement().getQualifiedName().toString(),
                            Factory.class.getSimpleName(),
                            annotationClass.getId(),
                            factoryAnnotationClass.getElement().getQualifiedName().toString());
                }
            }
            set.add(annotationClass);
        }
    }

    /**
     * 具有相同的type的注解类，将被聚合在一起生成一个工厂类。
     * 这个生成的类使用*Factory*后缀，
     * 例如type = ChineseFood.class，将生成ChineseFoodFactory工厂类；
     * @param elementUtils
     * @param filer
     * @throws IOException
     */
    void createFactory(Elements elementUtils, Filer filer) throws IOException {

        if (null != mGroupMap){
            for (String typeName : mGroupMap.keySet()){

                TypeElement superTypeElement = elementUtils.getTypeElement(typeName);
                PackageElement pkg = elementUtils.getPackageOf(superTypeElement);
                //isUnnamed;如果此包是一个未命名的包，则返回 true，否则返回 false
                String packageName = pkg.isUnnamed() ? null : pkg.getQualifiedName().toString();

                //创建一个create方法
                MethodSpec.Builder method = MethodSpec.methodBuilder("create")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String.class, "id")
                        .returns(TypeName.get(superTypeElement.asType()));

                // 判断id是否为空
                method.beginControlFlow("if (id == null)")
                        .addStatement("throw new IllegalArgumentException($S)", "id is null!")
                        .endControlFlow();

                Set<FactoryAnnotationClass> classSet = mGroupMap.get(typeName);
                // 根据id去返回各自的实现类
                for (FactoryAnnotationClass item : classSet) {
                    method.beginControlFlow("if ($S.equals(id))", item.getId())
                            .addStatement("return new $L()", item.getElement().getQualifiedName().toString())
                            .endControlFlow();
                }

                method.addStatement("throw new IllegalArgumentException($S + id)", "Unknown id = ");

                TypeSpec typeSpec = TypeSpec.classBuilder(superTypeElement.getSimpleName().toString() + Factory.class.getSimpleName()).addMethod(method.build()).build();

                JavaFile.builder(packageName, typeSpec).build().writeTo(filer);
            }
        }


    }

    void clear(){
        mGroupMap.clear();
    }

     Map<String, Set<FactoryAnnotationClass>> getGroupMap() {
        return mGroupMap;
    }
}
