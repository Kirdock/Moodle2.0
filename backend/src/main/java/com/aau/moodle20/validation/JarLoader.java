package com.aau.moodle20.validation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarLoader extends ClassLoader {

    private String jarDirectory = null;

    public JarLoader(ClassLoader parent, String jarDirectory) {
        super(parent);
        this.jarDirectory = jarDirectory;
    }


    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException ex) {
            try {
                return getClassFromJar(name);
            } catch (IOException e) {
                throw new ClassNotFoundException(name, ex);
            }
        }
    }

    private Class<?> getClassFromJar(String name) throws ClassNotFoundException, IOException {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(this.jarDirectory);
            Class<?> newClass = null;
            Enumeration<JarEntry> en = jarFile.entries();
            while (en.hasMoreElements()) {
                JarEntry je = en.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                    continue;
                }
                String className = je.getName().replace(".class", "");
                if (!name.equals(className))
                    continue;
                className = className.replace('/', '.');
                byte[] data = getBytes(jarFile.getInputStream(je));
                if (data != null) {
                    newClass = defineClass(null, data, 0, data.length);
                }
            }
            if (newClass == null)
                throw new ClassNotFoundException(name);

            return newClass;
        } catch (IOException ex) {
            throw new ClassNotFoundException(name, ex);
        } finally {
            if (jarFile != null)
                jarFile.close();
        }
    }


    private static byte[] getBytes(InputStream is) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
            byte[] buffer = new byte[0xFFFF];
            for (int len; (len = is.read(buffer)) != -1; )
                os.write(buffer, 0, len);
            os.flush();
            return os.toByteArray();
        }
    }
}
