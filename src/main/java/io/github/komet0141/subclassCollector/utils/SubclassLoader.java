package io.github.komet0141.subclassCollector.utils;

import io.github.komet0141.subclassCollector.CollectSubclass;
import io.github.komet0141.subclassCollector.CollectionProcessor;

public class SubclassLoader {
    public static void load(String subpackageName) throws ClassNotFoundException {
        Class.forName(fullClassName(subpackageName));
    }
    public static void load(Class<?> clazz) throws ClassNotFoundException {
        load(clazz.getAnnotation(CollectSubclass.OutputPackage.class).value());
    }
    public static String fullClassName(String subpackageName) {
        return fullPackageName(subpackageName)+"."+SubclassLoader.class.getSimpleName();
    }
    
    public static String fullPackageName(String subpackageName) {
        return CollectionProcessor.class.getPackage().getName()+".loader."+subpackageName;
    }
}
