package io.github.komet0141.subclassCollector.utils;

import io.github.komet0141.subclassCollector.CollectionProcessor;

public class SubclassLoader {
    public static void load(){}
    public static String getPackageName(String outputPackageName) {
        return CollectionProcessor.class.getPackage().getName()+".loader."+outputPackageName;
    }
    public static String getFullClassName(String outputPackageName) {
        return getPackageName(outputPackageName)+getClassName();
    }
    public static String getClassName() {
        return "SubclassLoader";
    }
}
