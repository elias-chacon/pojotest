package io.github.eliaschacon.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.core.annotation.AliasFor;

@BuilderGenerator
@TestGenerator
@JsonInclude(JsonInclude.Include.NON_NULL)
public @interface DTO {
    @AliasFor(annotation = BuilderGenerator.class, attribute = "skip")
    String[] buildSkip() default {};

    @AliasFor(annotation = BuilderGenerator.class, attribute = "location")
    String buildLocation() default "";

    @AliasFor(annotation = TestGenerator.class, attribute = "skip")
    String[] testSkip() default {};
}
