package com.aau.moodle20.validation;


import com.aau.moodle20.constants.ApiErrorResponseCodes;
import com.aau.moodle20.constants.FileConstants;
import com.aau.moodle20.exception.ServiceException;
import org.springframework.web.multipart.MultipartFile;
import validation.IValidator;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ValidatorHandler {

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
        finally {
            jarFile.close();
        }

        return validator;
    }


    public void checkValidatorFile(MultipartFile file,Long exampleId) throws IOException, ClassNotFoundException {

        String filePath = FileConstants.VALIDATOR_TEST_DIR +"/"+file.getOriginalFilename() ;
        File fileValidatorTest = new File(filePath);
        if(fileValidatorTest.exists())
            fileValidatorTest.delete();
        try {
            saveFile(filePath,file);
            IValidator validator = loadValidator(filePath);
            if (validator == null) {
                throw new ServiceException("Error: no validator found in Jar File", ApiErrorResponseCodes.NO_VALID_VALIDATOR_FOUND_IN_JAR);
            }

        }
        catch (ClassNotFoundException e)
        {
            throw new ServiceException("Error: classNotFoundException",e, ApiErrorResponseCodes.NO_VALID_VALIDATOR_FOUND_IN_JAR);
        }
    }

    protected void saveFile(String filePath, MultipartFile file) throws IOException
    {
        OutputStream outStream = null;
        try {
            InputStream initialStream = file.getInputStream();
            byte[] buffer = new byte[initialStream.available()];
            int readBytes = initialStream.read(buffer);
            File targetFile = new File(filePath);
            outStream = new FileOutputStream(targetFile);
            outStream.write(buffer);
            outStream.close();
        }finally {
            if(outStream!=null)
                outStream.close();
        }
    }

}
