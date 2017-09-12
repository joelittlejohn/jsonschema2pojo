/*
 * Copyright 2011, Mysema Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mysema.examples;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public final class ClassPathUtils {
    
    private static final Pattern JAR_URL_SEPARATOR = Pattern.compile("!");
    
    public static Set<Class<?>> scanPackage(ClassLoader classLoader, Package pkg) throws IOException {
        return scanPackage(classLoader, pkg.getName());
    }
    
    public static Set<Class<?>> scanPackage(ClassLoader classLoader, String pkg) throws IOException {
        String packagePath = pkg.replace('.', '/');
        Enumeration<URL> urls = classLoader.getResources(packagePath);
        Set<Class<?>> classes = new HashSet<Class<?>>();
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url.getProtocol().equals("jar")) {
                scanJar(classes, url, packagePath);

            } else if (url.getProtocol().equals("file")) {
                scanDirectory(pkg, classes, url, pkg);

            } else {
                throw new IllegalArgumentException("Illegal url : " + url);
            }
        }
        return classes;
    }

    private static void scanDirectory(String pkg, Set<Class<?>> classes, URL url, String packageName) throws IOException {
        Deque<File> files = new ArrayDeque<File>();
        String packagePath = null;
        try {
            File packageAsFile = new File(url.toURI());
            packagePath = packageAsFile.getPath();
            files.add(packageAsFile);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        while (!files.isEmpty()) {
            File file = files.pop();
            for (File child : file.listFiles()) {
                if (child.getName().endsWith(".class")) {
                    String fileName = child.getPath().substring(packagePath.length()+1).replace(File.separatorChar, '.');
                    String className = pkg + "." + fileName.substring(0, fileName.length()-6);
                    if (className.startsWith(packageName)) {
                        Class<?> cl = safeClassForName(className);
                        if (cl != null) {
                            classes.add(cl);
                        }    
                    }                    
                } else if (child.isDirectory()) {
                    files.add(child);
                }
            }
        }
    }

    private static void scanJar(Set<Class<?>> classes, URL url, String packagePath) throws IOException {
        String[] fileAndPath = JAR_URL_SEPARATOR.split(url.getFile().substring(5));
        JarFile jarFile = new JarFile(fileAndPath[0]);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();            
            if (entry.getName().endsWith(".class") && entry.getName().startsWith(packagePath) && entry.getName().startsWith(fileAndPath[1].substring(1))) {
                String className = entry.getName().substring(0, entry.getName().length()-6).replace('/', '.');
                Class<?> cl = safeClassForName(className);
                if (cl != null) {
                    classes.add(cl);
                }
            }
        }
    }
    
    public static Class<?> safeClassForName(String className){
        try {
            if (className.startsWith("com.sun")) {
                return null;
            } else {
                return Class.forName(className);
            }
        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoClassDefFoundError e) {
            return null;
        }
    }

    private ClassPathUtils() {}
    
}
