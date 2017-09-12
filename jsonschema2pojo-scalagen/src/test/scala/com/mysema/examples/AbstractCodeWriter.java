/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.examples;

import static com.mysema.codegen.Symbols.NEWLINE;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

/**
 * @author tiwe
 *
 * @param <T>
 */
public abstract class AbstractCodeWriter<T extends AbstractCodeWriter<T>> implements Appendable {

    private final Appendable appendable;

    private final int spaces;

    private final String spacesString;

    private String indent = "";

    @SuppressWarnings("unchecked")
    private final T self = (T)this;

    public AbstractCodeWriter(Appendable appendable, int spaces){
        if (appendable == null){
            throw new IllegalArgumentException("appendable is null");
        }
        this.appendable = appendable;
        this.spaces = spaces;
        this.spacesString = StringUtils.leftPad("", spaces);
    }

    @Override
    public T append(char c) throws IOException {
        appendable.append(c);
        return self;
    }

    @Override
    public T append(CharSequence csq) throws IOException {
        appendable.append(csq);
        return self;
    }

    @Override
    public T append(CharSequence csq, int start, int end) throws IOException {
        appendable.append(csq, start, end);
        return self;
    }

    //@Override
    public T beginLine(String... segments) throws IOException {
        append(indent);
        for (String segment : segments){
            append(segment);
        }
        return self;
    }

    protected T goIn(){
        indent += spacesString;
        return self;
    }


    protected T goOut(){
        if (indent.length() >= spaces){
            indent = indent.substring(0, indent.length() - spaces);
        }
        return self;
    }

    //@Override
    public T line(String... segments) throws IOException{
        append(indent);
        for (String segment : segments){
            append(segment);
        }
        return nl();
    }

    //@Override
    public T nl() throws IOException {
        return append(NEWLINE);
    }

}
