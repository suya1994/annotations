package com.example.processor;

import com.example.annotations.Factory;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class FactoryProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Elements mElementUtils;
    private Types mTypeUtils;
    private Filer mFiler;
    private FactoryGroup mFactoryGroup;


    /**
     * 初始化处理器，一般在这里获取我们需要的工具类
     * @param processingEnvironment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mMessager = processingEnvironment.getMessager();
        mElementUtils = processingEnvironment.getElementUtils();
        mTypeUtils = processingEnvironment.getTypeUtils();
        mFiler = processingEnvironment.getFiler();
        mFactoryGroup = new FactoryGroup();
    }

    /**
     * 指定注解处理器是注册给哪个注解的，返回指定支持的注解类集合
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Factory.class.getCanonicalName());
        return annotations;
    }

    /**
     * 指定java版本,一般返回最新版本即可
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 处理器实际处理逻辑入口
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("---process---");
        try {

            //遍历所有注解了@Factory的元素
            for (Element element : roundEnvironment.getElementsAnnotatedWith(Factory.class)){

                //1.只有类可以被@Factory注解，因为接口或者抽象类并不能用new操作实例化；
                if (ElementKind.CLASS != element.getKind()){
                    throw new ProcessorException(element, "Only classes can be annotated with @%s", Factory.class.getSimpleName());
                }
                //检查是否是抽象类
                checkIsAbstract(element);

                //强转
                TypeElement typeElement = (TypeElement) element;
                FactoryAnnotationClass currentClass = new FactoryAnnotationClass(typeElement);

                //2.被@Factory注解的类必须直接或者间接的继承于type()指定的类型
                checkType(currentClass);

                //符合条件的加入到group中
                mFactoryGroup.add(currentClass);
            }

            mFactoryGroup.createFactory(mElementUtils,mFiler);
            mFactoryGroup.clear();

        }catch (ProcessorException e){
            showError(e);
        } catch (IOException e) {
            showError(new ProcessorException(null,e.getMessage()));
        }
        return true;
    }

    private void checkIsAbstract(Element element) throws ProcessorException {
        Set<Modifier> modifierSet = element.getModifiers();
        for (Modifier modifier : modifierSet){
            if (Modifier.ABSTRACT.toString().equals(modifier.toString()) ){
                throw new ProcessorException(element, "abstract classes can't be annotated with @%s", Factory.class.getSimpleName());
            }
        }
    }


    private void checkType(FactoryAnnotationClass annotationClass) throws ProcessorException {
        String superTypeName = annotationClass.getSuperTypeName();
        TypeElement typeElement = annotationClass.getElement();

        //根据type的class名来获取TypeElement
        TypeElement requiredElement = mElementUtils.getTypeElement(superTypeName);

        //若type类是一个接口
        if (ElementKind.INTERFACE == requiredElement.getKind()){
            // 检查接口是否实现了
            if (!typeElement.getInterfaces().contains(requiredElement.asType())) {
                throw new ProcessorException(typeElement, "The class %s annotated with @%s must implement the interface %s",
                        typeElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                        superTypeName);
            }
        }else {
            // 检查子类
            TypeElement currentClass = typeElement;
            while (true) {

                TypeMirror superClassType = currentClass.getSuperclass();

                if (superClassType.getKind() == TypeKind.NONE) {
                    // 到达了基本类型(java.lang.Object), 所以退出
                    throw new ProcessorException(typeElement, "The class %s annotated with @%s must inherit from %s",
                            typeElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                            superTypeName);
                }

                if (superClassType.toString().equals(superTypeName)) {
                    // 找到了要求的父类
                    break;
                }

                // 在继承树上继续向上搜寻
                currentClass = (TypeElement) mTypeUtils.asElement(superClassType);
            }
        }
    }

    private void showError(ProcessorException e){
        mMessager.printMessage(Diagnostic.Kind.ERROR,e.getMessage(),e.getElement());
    }
}
