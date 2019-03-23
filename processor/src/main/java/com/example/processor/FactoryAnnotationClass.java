package com.example.processor;

import com.example.annotations.Factory;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

public class FactoryAnnotationClass {
    /**
     * 被Factory注解的元素
     */
    private TypeElement element;
    /**
     * {@link Factory#id()} 注解时设置的id
     */
    private String id;
    /**
     * {@link Factory#type()} 注解时设置的type类型的合法全名；
     */
    private String superTypeName;

    public FactoryAnnotationClass(TypeElement element) {
        this.element = element;
        Factory factory = element.getAnnotation(Factory.class);
        id = factory.id();
        superTypeName = getRequiredTypeClassName(element);
    }

    private String getRequiredTypeClassName(TypeElement currentElement){
        Factory factory = currentElement.getAnnotation(Factory.class);
        String requiredTypeClassName;
        try {
            //这种情况是针对第三方jar包
            Class clazz = factory.type();
            requiredTypeClassName = clazz.getCanonicalName();
        }catch (MirroredTypeException e){
            //平常在源码上注解时，都会走到这里
            DeclaredType declaredType = (DeclaredType) e.getTypeMirror();
            TypeElement element = (TypeElement) declaredType.asElement();
            requiredTypeClassName = element.getQualifiedName().toString();
        }
        return requiredTypeClassName;
    }

    public TypeElement getElement() {
        return element;
    }

    public String getId() {
        return id;
    }

    public String getSuperTypeName() {
        return superTypeName;
    }


}
