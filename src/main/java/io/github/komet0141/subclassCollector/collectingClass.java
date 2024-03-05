package io.github.komet0141.subclassCollector;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

final class collectingClass {
    public final TypeElement type;
    public final ExecutableElement initializer;
    public collectingClass(TypeElement type, ExecutableElement initializer) {
        this.initializer = initializer;
        this.type = type;
    }
}
