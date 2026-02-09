/**
 * Copyright © 2010-2020 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JTypeVar;

/**
 * Represents a type with type-use annotations (Java 8+, JSR 308).
 * <p>
 * This class wraps a {@link JClass} and adds type-use annotations that are rendered
 * directly before the type name. This enables generating code like:
 * <pre>
 * List&lt;@Valid Item&gt;
 * Map&lt;String, @Nullable Object&gt;
 * </pre>
 * <p>
 * <b>Type-use annotations vs declaration annotations:</b>
 * <ul>
 * <li>{@link com.sun.codemodel.JAnnotatable#annotate(Class)} adds annotations to <em>declarations</em> (the field,
 * method, or class itself). Example: {@code @NotNull private List<String> items;}</li>
 * <li>{@link JAnnotatedClass#annotated(Class)} creates a new <em>type</em> with embedded annotations.
 * Example: {@code private List<@NotNull String> items;}</li>
 * </ul>
 * This implementation uses reflection to access {@code JFormatter}'s private {@code importedClasses} field,
 * which is needed to determine correct annotation placement per JLS §9.7.4. This is unavoidable since
 * codemodel 2.6 provides no public API for querying import state.
 */
public class JAnnotatedClass extends JClass {

    /** Matches codemodel's package-private JFormatter.CLOSE_TYPE_ARGS */
    private static final char CLOSE_TYPE_ARGS = '\uFFFF';

    private static final Field IMPORTED_CLASSES_FIELD;
    private static final Constructor<JAnnotationUse> ANNOTATION_USE_CONSTRUCTOR;
    static {
        try {
            IMPORTED_CLASSES_FIELD = JFormatter.class.getDeclaredField("importedClasses");
            IMPORTED_CLASSES_FIELD.setAccessible(true);
            ANNOTATION_USE_CONSTRUCTOR = JAnnotationUse.class.getDeclaredConstructor(JClass.class);
            ANNOTATION_USE_CONSTRUCTOR.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final JClass basis;
    private final List<JAnnotationUse> annotations;

    /**
     * Creates a new annotated class wrapper for the given type.
     *
     * @param basis
     *        The class to annotate. May not be <code>null</code>.
     * @return A new JAnnotatedClass wrapping the basis type.
     */
    public static JAnnotatedClass of(JClass basis) {
        if (basis == null) throw new IllegalArgumentException("basis for annotated class cannot be null");
        return new JAnnotatedClass(basis, Collections.emptyList());
    }

    private JAnnotatedClass(JClass basis, List<JAnnotationUse> annotations) {
        super(basis.owner());
        this.basis = basis;
        this.annotations = new ArrayList<>(annotations);
    }

    /**
     * Creates a JAnnotationUse instance using reflection (since the constructor is package-private).
     */
    private static JAnnotationUse createAnnotationUse(JClass annotationClass) {
        try {
            return ANNOTATION_USE_CONSTRUCTOR.newInstance(annotationClass);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException("Failed to create JAnnotationUse", e);
        }
    }

    /**
     * @return The underlying class without annotations.
     */
    public JClass basis() {
        return basis;
    }

    /**
     * @return An unmodifiable list of annotations on this type.
     */
    public List<JAnnotationUse> annotations() {
        return Collections.unmodifiableList(annotations);
    }

    /**
     * Returns a new JAnnotatedClass with the given annotation added.
     *
     * @param clazz the annotation class to add
     * @return a new JAnnotatedClass with the annotation applied
     */
    public JAnnotatedClass annotated(Class<? extends Annotation> clazz) {
        return annotated(owner().ref(clazz));
    }

    /**
     * Returns a new JAnnotatedClass with the given annotation added.
     *
     * @param clazz the annotation class reference to add
     * @return a new JAnnotatedClass with the annotation applied
     */
    public JAnnotatedClass annotated(JClass clazz) {
        List<JAnnotationUse> newAnnotations = new ArrayList<>(annotations);
        newAnnotations.add(createAnnotationUse(clazz));
        return new JAnnotatedClass(basis, newAnnotations);
    }

    @Override
    public String name() {
        return basis.name();
    }

    @Override
    public String fullName() {
        return basis.fullName();
    }

    @Override
    public String binaryName() {
        return basis.binaryName();
    }

    @Override
    public JPackage _package() {
        return basis._package();
    }

    @Override
    public JClass _extends() {
        return basis._extends();
    }

    @Override
    public Iterator<JClass> _implements() {
        return basis._implements();
    }

    @Override
    public boolean isInterface() {
        return basis.isInterface();
    }

    @Override
    public boolean isAbstract() {
        return basis.isAbstract();
    }

    @Override
    public boolean isArray() {
        return basis.isArray();
    }

    @Override
    public JPrimitiveType getPrimitiveType() {
        return basis.getPrimitiveType();
    }

    @Override
    public JClass erasure() {
        return basis.erasure();
    }

    @Override
    public List<JClass> getTypeParameters() {
        return basis.getTypeParameters();
    }

    @Override
    public JTypeVar[] typeParams() {
        return basis.typeParams();
    }

    @Override
    public JClass outer() {
        return basis.outer();
    }

    @Override
    protected JClass substituteParams(JTypeVar[] variables, List<JClass> bindings) {
        // JAnnotatedClass only wraps concrete (non-generic) types for type-use annotations,
        // so type variable substitution is always a no-op.
        return this;
    }

    @Override
    public void generate(JFormatter f) {
        if (annotations.isEmpty()) {
            f.t(basis);
            return;
        }

        if (!f.isPrinting()) {
            // Collecting mode: use g() not t() so that narrowed type arguments are visited
            f.g(basis);
            for (JAnnotationUse annotation : annotations) {
                f.g(annotation);
            }
            return;
        }

        // Printing mode: place annotations per JLS §9.7.4.
        // When imported:     @Annotation SimpleName
        // When not imported: pkg.@Annotation SimpleName
        // Use erasure (raw type) for prefix — narrowed types like List<String> have
        // different name/fullName lengths due to type arguments.
        JClass rawType = basis.erasure();
        String rawFullName = rawType.fullName();
        String rawSimpleName = rawType.name();
        String prefix = rawFullName.substring(0, rawFullName.length() - rawSimpleName.length());

        if (!isImported(f, rawType) && !prefix.isEmpty()) {
            f.p(prefix);
        }
        printAnnotations(f);
        f.p(rawSimpleName);

        // For narrowed types (e.g., List<String>), print type arguments
        List<JClass> typeArgs = basis.getTypeParameters();
        if (!typeArgs.isEmpty()) {
            f.p('<').g(typeArgs).p(CLOSE_TYPE_ARGS);
        }
    }

    private void printAnnotations(JFormatter f) {
        for (JAnnotationUse annotation : annotations) {
            f.g(annotation).p(' ');
        }
    }

    private static boolean isImported(JFormatter f, JClass type) {
        try {
            return ((Collection<?>) IMPORTED_CLASSES_FIELD.get(f)).contains(type);
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        JAnnotatedClass that = (JAnnotatedClass) obj;
        return basis.equals(that.basis) && annotationsEqual(this.annotations, that.annotations);
    }

    // Compares annotations by class name only, not parameter values.
    // JAnnotationUse does not implement equals(), so we use the annotation class
    // full name as a proxy. JAnnotationUse param values are unfortunately not accessible.
    private static boolean annotationsEqual(List<JAnnotationUse> a, List<JAnnotationUse> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).getAnnotationClass().fullName().equals(b.get(i).getAnnotationClass().fullName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = basis.hashCode();
        for (JAnnotationUse annotation : annotations) {
            h = h * 37 + annotation.getAnnotationClass().fullName().hashCode();
        }
        return h;
    }
}
