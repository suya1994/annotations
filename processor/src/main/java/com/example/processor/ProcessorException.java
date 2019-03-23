package com.example.processor;

import javax.lang.model.element.Element;

public class ProcessorException  extends Exception{
    private Element element;

    public ProcessorException(Element element, String msg, Object... args) {
        super(String.format(msg, args));
        this.element = element;
    }

    public Element getElement() {
        return element;
    }
}
