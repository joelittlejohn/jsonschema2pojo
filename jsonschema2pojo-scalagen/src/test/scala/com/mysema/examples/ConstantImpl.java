package com.mysema.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysema.commons.lang.Assert;
import com.mysema.query.types.Constant;
import com.mysema.query.types.ExpressionBase;
import com.mysema.query.types.Visitor;

/**
 * ConstantImpl is the default implementation of the Constant interface
 * 
 * @author tiwe
 */
public class ConstantImpl<T> extends ExpressionBase<T> implements Constant<T> {

    private static final long serialVersionUID = -3898138057967814118L;
    
    private static final int CACHE_SIZE = 256;
    
    private static final Constant<Byte>[] BYTES = new Constant[CACHE_SIZE];

    private static final Constant<Integer>[] INTEGERS = new Constant[CACHE_SIZE];
    
    private static final Constant<Long>[] LONGS = new Constant[CACHE_SIZE];

    private static final Constant<Short>[] SHORTS = new Constant[CACHE_SIZE];

    private static final Map<String,Constant<String>> STRINGS;
    
    private static final Constant<Boolean> FALSE = new ConstantImpl<Boolean>(Boolean.FALSE);

    private static final Constant<Boolean> TRUE = new ConstantImpl<Boolean>(Boolean.TRUE);

    static {
        List<String> strs = new ArrayList<String>(Arrays.asList("", ".", ".*", "%"));
        for (int i = 0; i < CACHE_SIZE; i++) {
            strs.add(String.valueOf(i));
        }

        STRINGS = new HashMap<String,Constant<String>>(strs.size());
        for (String str : strs) {
            STRINGS.put(str, new ConstantImpl<String>(str));
        }
        
        for (int i = 0; i < CACHE_SIZE; i++) {
            INTEGERS[i] = new ConstantImpl<Integer>(Integer.class, Integer.valueOf(i));
            SHORTS[i] = new ConstantImpl<Short>(Short.class, Short.valueOf((short)i));
            BYTES[i] = new ConstantImpl<Byte>(Byte.class, Byte.valueOf((byte)i));
            LONGS[i] = new ConstantImpl<Long>(Long.class, Long.valueOf(i));
        }
    }

    public static Constant<Boolean> create(boolean b) {
        return b ? TRUE : FALSE;
    }
    
    public static Constant<Byte> create(byte i) {
        if (i >= 0 && i < CACHE_SIZE) {
            return BYTES[i];
        } else {
            return new ConstantImpl<Byte>(Byte.class, Byte.valueOf(i));
        }
    }

    public static Constant<Integer> create(int i) {
        if (i >= 0 && i < CACHE_SIZE) {
            return INTEGERS[i];
        } else {
            return new ConstantImpl<Integer>(Integer.class, Integer.valueOf(i));
        }
    }

    public static Constant<Long> create(long i) {
        if (i >= 0 && i < CACHE_SIZE) {
            return LONGS[(int)i];
        } else {
            return new ConstantImpl<Long>(Long.class, Long.valueOf(i));
        }
    }

    public static Constant<Short> create(short i) {
        if (i >= 0 && i < CACHE_SIZE) {
            return SHORTS[i];
        } else {
            return new ConstantImpl<Short>(Short.class, Short.valueOf(i));
        }
    }
    
    public static Constant<String> create(String str) {
        return create(str, false);
    }

    public static Constant<String> create(String str, boolean populateCache) {
        if (STRINGS.containsKey(str)) {
            return STRINGS.get(str);
        } else {
            Constant<String> rv = new ConstantImpl<String>(Assert.notNull(str,"str"));
            if (populateCache) {
                STRINGS.put(str, rv);
            }
            return rv;
        }
    }
    
    public static <T> Constant<Class<T>> create(Class<T> constant) {
        return new ConstantImpl<Class<T>>(constant);
    }

    private final T constant;
    
    public ConstantImpl(T constant) {
        this((Class<T>)constant.getClass(), constant);
    }
    
    public ConstantImpl(Class<T> type, T constant) {
        super(type);
        this.constant = constant;
    }
    
    @Override
    public <R, C> R accept(Visitor<R, C> v, C context) {
        return v.visit(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Constant<?>) {
            return ((Constant<?>)o).getConstant().equals(constant);
        } else {
            return false;
        }
    }
    
    @Override
    public T getConstant() {
        return constant;
    }
    
    @Override
    public int hashCode() {
        return constant.hashCode();
    }

}
