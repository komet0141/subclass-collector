package io.github.komet0141.subclassCollector.utils;

import io.github.komet0141.subclassCollector.CollectSubclass;
import io.github.komet0141.subclassCollector.CollectionProcessor;

public class SubclassLoader {
    public static void load(String subpackageName) {
        try {
            Class.forName(fullClassName(subpackageName));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static void load(Class<?> clazz) {
        String subpackageName;
        try {
            subpackageName = clazz.getAnnotation(CollectSubclass.OutputPackage.class).value();
        } catch (Exception e) {throw new RuntimeException("could not find "+ CollectSubclass.OutputPackage.class+"in given class");}
        load(subpackageName);
    }
    public static String fullClassName(String subpackageName) {
        return fullPackageName(subpackageName)+"."+SubclassLoader.class.getSimpleName();
    }
    
    public static String fullPackageName(String subpackageName) {
        return CollectionProcessor.class.getPackage().getName()+".loader."+subpackageName;
    }
}
