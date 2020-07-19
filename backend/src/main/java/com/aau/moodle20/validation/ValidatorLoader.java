package com.aau.moodle20.validation;


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

//    public void addValidatorJarToClasspath(String directory) throws MalformedURLException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
//        File f = new File("./backend/attachments/validators/");
//        URL url = f.toURI().toURL();
//
//
//
////        Class urlClass = URLClassLoader.class;
////        Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
////        method.setAccessible(true);
////        method.invoke(urlClassLoader, new Object[]{url});
//    }

}
