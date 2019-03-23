package com.example.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Factory {
    /**
     * 所属的type
     * @return
     */
    Class type();

    /**
     * 唯一的id
     * @return
     */
    String id();
}
