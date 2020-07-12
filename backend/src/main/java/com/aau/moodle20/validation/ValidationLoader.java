package com.aau.moodle20.validation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ValidationLoader {

    public IValidator loadValidator(String directory, String classpath, Class<IValidator> parentClass) throws ClassNotFoundException, IOException {
        File jarDir = new File(directory);
        JarFile jarFile = new JarFile(directory);
        //JarFile jarFile = new JarFile(jarDir);

        try {
           /* URLClassLoader loader = URLClassLoader.newInstance(
                    new URL[]{jarDir.toURI().toURL()},
                    getClass().getClassLoader()
            );
            Class classToLoad = Class.forName(classpath, true, loader);
            //Class<? extends IValidator> newClass = classToLoad.asSubclass(parentClass);
            // Apparently its bad to use Class.newInstance, so we use
            // newClass.getConstructor() instead
            Object instance = classToLoad.newInstance();
            //Constructor<? extends IValidator> constructor = newClass.getConstructor();
            return (IValidator) instance;*/

            Enumeration<JarEntry> en = jarFile.entries();
            URLClassLoader loader = URLClassLoader.newInstance(
                    new URL[]{jarDir.toURI().toURL()},
                    getClass().getClassLoader());

            while (en.hasMoreElements()) {
                try {
                    JarEntry je = (JarEntry) en.nextElement();
                    if (je.isDirectory() || !je.getName().endsWith(".class")) {
                        continue;
                    }
                    String className = je.getName().replace(".class", "");
                    className = className.replace('/', '.');
                    je.get
                    Class<?> newClass = loader.loadClass(className);
                    Constructor<?> constructor = newClass.getConstructor();
                    Object instance = constructor.newInstance();

                    ClasspathHacke

                    return (IValidator) instance;
                } catch (ClassNotFoundException | NoSuchMethodException ex) {
                    ex.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }


        } catch (MalformedURLException e) {
            // There might be multiple JARs in the directory,
            // so keep looking
            e.printStackTrace();
        } /*| NoSuchMethodException | InvocationTargetException */

        throw new ClassNotFoundException("Class " + classpath
                + " wasn't found in directory " + System.getProperty("user.dir") + directory);
    }

    


   /* for (File jar : jarDir.listFiles()) {
        try {
            URLClassLoader loader = URLClassLoader.newInstance(
                    new URL[] { jar.toURI().toURL() },
                    getClass().getClassLoader()
            );
            Class classToLoad = Class.forName(classpath, true, loader);
            //Class<? extends IValidator> newClass = classToLoad.asSubclass(parentClass);
            // Apparently its bad to use Class.newInstance, so we use
            // newClass.getConstructor() instead
            Object instance = classToLoad.newInstance();
            //Constructor<? extends IValidator> constructor = newClass.getConstructor();
            return (IValidator) instance;

        } catch (ClassNotFoundException e) {
            // There might be multiple JARs in the directory,
            // so keep looking
            continue;
        } catch (MalformedURLException /*| NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }*/
}
