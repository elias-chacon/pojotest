package io.github.eliaschacon.test;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.Set;

public abstract class AbstractProcessor {
    protected ProcessingEnvironment processingEnv;

    protected AbstractProcessor(final ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public abstract boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);
}
