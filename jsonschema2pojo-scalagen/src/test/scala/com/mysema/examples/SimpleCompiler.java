/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.examples;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;
import javax.lang.model.SourceVersion;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * SimpleCompiler provides a convenience wrapper of the JavaCompiler interface with automatic
 * classpath generation
 * 
 * @author tiwe
 *
 */
public class SimpleCompiler implements JavaCompiler{
        
    public static String getClassPath(URLClassLoader classLoader) {
        try{
            StringBuilder path = new StringBuilder();
            for (URL url : ((URLClassLoader)classLoader).getURLs()){
                if (path.length() > 0){
                    path.append(File.pathSeparator);
                }
                String decodedPath = URLDecoder.decode(url.getPath(),"UTF-8");
                path.append(new File(decodedPath).getAbsolutePath());
            }
            return  path.toString();    
        }catch(UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }        
    }
    
    private final ClassLoader classLoader;
    
    @Nullable
    private String classPath;    
    
    private final JavaCompiler compiler;
    
    public SimpleCompiler(){
        this(ToolProvider.getSystemJavaCompiler(), Thread.currentThread().getContextClassLoader());
    }
    
    public SimpleCompiler(JavaCompiler compiler, ClassLoader classLoader){
        this.compiler = compiler;
        this.classLoader = classLoader;
    }
    
    private String getClasspath(){        
        if (classPath == null){                
            if (classLoader instanceof URLClassLoader){
                classPath = getClassPath((URLClassLoader)classLoader);
            }else{
                throw new IllegalArgumentException("Unsupported ClassLoader " + classLoader);
            }                                
        }
        return classPath;          
    }

    @Override
    public Set<SourceVersion> getSourceVersions() {
        return compiler.getSourceVersions();
    }

    @Override
    public StandardJavaFileManager getStandardFileManager(
            DiagnosticListener<? super JavaFileObject> diagnosticListener,
            Locale locale, Charset charset) {
        return compiler.getStandardFileManager(diagnosticListener, locale, charset);
    }

    @Override
    public JavaCompiler.CompilationTask getTask(Writer out, JavaFileManager fileManager,
            DiagnosticListener<? super JavaFileObject> diagnosticListener,
            Iterable<String> options, Iterable<String> classes,
            Iterable<? extends JavaFileObject> compilationUnits) {
        return compiler.getTask(out, fileManager, diagnosticListener, options, classes, compilationUnits);
    }

    @Override
    public int isSupportedOption(String option) {
        return compiler.isSupportedOption(option);
    }

    @Override
    public int run(InputStream in, OutputStream out, OutputStream err, String... arguments) {
        for (String a : arguments){
            if (a.equals("-classpath")){
                // FIXME varargs
                //return compiler.run(in, out, err, arguments);
            }
        }
        
        // no classpath given
        List<String> args = new ArrayList<String>(arguments.length + 2);
        args.add("-classpath");
        args.add(getClasspath());
        for (String arg : arguments){
            args.add(arg);
        }        
        
        // FIXME varargs
        //return compiler.run(in, out, err, args.toArray(new String[args.size()]));
        return 0;
    }

}
