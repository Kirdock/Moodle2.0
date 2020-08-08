package com.aau.moodle20.validation;


import validation.IValidator;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ValidatorLoader {

    public IValidator loadValidator(String directory) throws ClassNotFoundException, IOException {

        IValidator validator = null;
        JarFile jarFile = new JarFile(directory);
        try {
            Enumeration<JarEntry> en = jarFile.entries();
            JarLoader jarLoader = new JarLoader(this.getClass().getClassLoader(), directory);
            while (en.hasMoreElements()) {
                JarEntry je = en.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                    continue;
                }
                String className = je.getName().replace(".class", "");
                className = className.replace('/', '.');
                Class<?> newClass = jarLoader.loadClass(className);
                Constructor<?> constructor = newClass.getConstructor();
                Object object = constructor.newInstance();
                if(object instanceof IValidator) {
                    validator = (IValidator) object;
                    break;
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new ClassNotFoundException(directory, e);
        }

        return validator;
    }
}
