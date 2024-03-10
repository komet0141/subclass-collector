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
        load(clazz.getAnnotation(CollectSubclass.OutputPackage.class).value());
    }
    public static String fullClassName(String subpackageName) {
        return fullPackageName(subpackageName)+"."+SubclassLoader.class.getSimpleName();
    }
    
    public static String fullPackageName(String subpackageName) {
        return CollectionProcessor.class.getPackage().getName()+".loader."+subpackageName;
    }
}
