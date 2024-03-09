package io.github.komet0141.subclassCollector;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Inherited
public @interface CollectSubclass {
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.CLASS)
    @Inherited
    @interface Abstract {}
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    @interface Initializer {}
    
    @Retention(RetentionPolicy.SOURCE)
    @interface OutputPackage{
        String value();
    }
}