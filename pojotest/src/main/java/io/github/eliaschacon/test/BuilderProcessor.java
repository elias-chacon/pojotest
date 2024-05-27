package io.github.eliaschacon.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BuilderProcessor extends AbstractProcessor {

    private static final Logger logger = LoggerFactory.getLogger(BuilderProcessor.class);

    public BuilderProcessor(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "## Processing builders");

        roundEnv.getElementsAnnotatedWith(BuilderGenerator.class)
                .stream()
                .filter(element -> element.getKind() != ElementKind.ANNOTATION_TYPE)
                .forEach(element -> processBuilderGenerator((TypeElement) element));

        roundEnv.getElementsAnnotatedWith(DTO.class)
                .stream()
                .filter(element -> element.getKind() != ElementKind.ANNOTATION_TYPE)
                .forEach(element -> processDTO((TypeElement) element));

        return true;
    }

    protected void processDTO(TypeElement element) {
        final var builderGenerator = element.getAnnotation(DTO.class);
        final var skipProperties = builderGenerator.buildSkip();
        final var packageLocation = ElementUtils.getPackage(element, builderGenerator.buildLocation());

        process(element, skipProperties, packageLocation);
    }

    protected void processBuilderGenerator(TypeElement element) {
        final var builderGenerator = element.getAnnotation(BuilderGenerator.class);
        final var skipProperties = builderGenerator.skip();
        final var packageLocation = ElementUtils.getPackage(element, builderGenerator.location());

        process(element, skipProperties, packageLocation);
    }

    private void process(TypeElement element, String[] skipProperties, String packageLocation) {
        final var elementProperties = ElementUtils.getElementProperties(element);
        generateClass(element, elementProperties, skipProperties, packageLocation);
    }

    protected void generateClass(TypeElement element, List<String> entityProperties,
                                 String[] skipProperties, String packageName) {
        final var properties = entityProperties.stream()
                .filter(property -> !Arrays.asList(skipProperties).contains(property))
                .collect(Collectors.toList());
        final var className = element.getSimpleName() + "Builder";
        final var refClass = element.getSimpleName();
        final var sourceCode = generateSourceCode(element, className, refClass, properties, packageName);

        try {
            final var sourceFile = processingEnv.getFiler().createSourceFile(packageName + "." + className);
            if (Paths.get(sourceFile.toUri()).toAbsolutePath().toFile().exists()) {
                return;
            }
            try (final var writer = sourceFile.openWriter()) {
                writer.write(sourceCode);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "## Error writing " + e.getMessage());
        }
    }

    private String generateSourceCode(TypeElement element, String className, Name refClass, List<String> properties, String packageLocation) {
        final var propertiesCode = properties.stream()
                .map(property -> String.format("    private %s _%s;\n", ElementUtils.getPropertyType(element, property), property))
                .collect(Collectors.joining());
        final var gettersSettersCode = properties.stream()
                .map(property -> {
                    String propertyType = ElementUtils.getPropertyType(element, property);
                    return String.format(
                            "    public %s %s(final %s %s) {\n" +
                                    "        _%s = %s;\n" +
                                    "        return this;\n    }\n\n",
                            className, property, propertyType,
                            property, property, property);
                })
                .collect(Collectors.joining());
        final var buildCode = String.format(
                "    public %s build() {\n        final var o = new %s();\n", refClass, refClass) +
                properties.stream().map(property -> String.format("        o.set%s(_%s);\n", (property.substring(0, 1).toUpperCase() + property.substring(1)), property)).collect(Collectors.joining()) + "\n" +
                "        return o;    \n}\n";
        final var builderCode = String.format("    public static final %s builder() {\n        return new %s();\n    }\n", className, className);
        return String.format("%s\n\n%s\npublic class %s {\n%s\n%s\n%s\n%s}\n",
                String.format("package %s;\n", packageLocation),
                String.format("/**\n * Builder Class autogenerated by POJO TEST for POJOS. \n * Do not modify please!\n */"),
                className,
                propertiesCode, gettersSettersCode, buildCode, builderCode);
    }
}
