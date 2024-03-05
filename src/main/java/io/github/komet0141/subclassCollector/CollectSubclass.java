package io.github.komet0141.subclassCollector;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CollectSubclass {
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    @interface Initializer {}
}