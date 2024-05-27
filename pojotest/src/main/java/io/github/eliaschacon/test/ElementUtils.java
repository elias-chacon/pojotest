package io.github.eliaschacon.test;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ElementUtils {

    private ElementUtils() {}

    public static String getPackageName(TypeElement element) {
        var qualifiedName = element.getQualifiedName().toString();
        int lastDot = qualifiedName.lastIndexOf('.');
        return (lastDot > 0) ? qualifiedName.substring(0, lastDot) : "";
    }

    public static String getPackage(TypeElement element, String... locations) {
        var elementPackage = new StringBuilder(getPackageName(element));
        for (var location : locations) {
            if (location.matches("^\\.$") || location.isEmpty()) {
                continue;
            }
            if (location.equals("..")) {
                elementPackage = new StringBuilder((elementPackage.lastIndexOf(".") > 0) ? elementPackage.substring(0, elementPackage.lastIndexOf(".")) : "");
            } else {
                elementPackage.append(".").append(location);
            }
        }
        return elementPackage.toString();
    }
    public static String getEnumValue(TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
            var declaredType = (DeclaredType) type;
            var element = declaredType.asElement();
            if (element.getKind() == ElementKind.ENUM) {
                var enumTypeElement = (TypeElement) element;
                List<? extends Element> enumConstants = enumTypeElement.getEnclosedElements();
                if (!enumConstants.isEmpty() && enumConstants.get(0) instanceof ExecutableElement) {
                    Element enumConstantValues = ((ExecutableElement) enumConstants.get(0)).getEnclosingElement()
                            .getEnclosedElements().stream()
                            .filter(e -> e.getKind() == ElementKind.ENUM_CONSTANT)
                            .findFirst().orElse(null);
                    if (Objects.nonNull(enumConstantValues)) {
                        return enumTypeElement.getQualifiedName() + "." + enumConstantValues;
                    }
                }
            }
        }
        return null;
    }

    public static boolean isPrimitive(TypeMirror type) {
        return Stream.of("int", "byte", "short", "long", "float", "double", "boolean", "char")
                .map(stype -> type.toString().contains(stype)).filter(Boolean.TRUE::equals).findFirst().orElse(false);
    }

    public static String getValue(TypeMirror type) {
        final var enumValue = getEnumValue(type);
        if (Objects.nonNull(enumValue)) {
            return enumValue;
        }
        var typeParameterElement = type.toString();
        if (typeParameterElement.contains("[")) {
            while(typeParameterElement.contains("[]")) {
                typeParameterElement = typeParameterElement.replace("[]", "[0]");
            }
            return "new " + typeParameterElement + "";
        }
        if (typeParameterElement.contains("Class")) {
            if (!typeParameterElement.contains("<")) {
                return "Object.class";
            }
            final var realType = typeParameterElement.substring(typeParameterElement.indexOf("<") + 1, typeParameterElement.indexOf(">"));
            if (realType.contains("?")) {
                return "Object.class";
            }
            return realType + ".class";
        }
        switch (typeParameterElement.replaceAll("<.*", "")) {
            case "java.lang.Boolean":
            case "boolean": return "true";

            case "java.util.List": return "new java.util.ArrayList()";
            case "java.util.Set": return "new java.util.HashSet()";
            case "java.util.Map": return String.format("new java.util.HashMap<%s>()", extractType(typeParameterElement));
            case "java.util.Queue": return "new java.util.LinkedList()";
            case "java.util.Deque": return "new java.util.ArrayDeque()";
            case "java.util.UUID": return "java.util.UUID.randomUUID()";
            case "java.util.Calendar": return "java.util.Calendar.getInstance()";
            case "java.util.Collection": return "java.util.Collections.emptyList()";

            case "java.time.LocalDateTime": return "java.time.LocalDateTime.now()";
            case "java.time.LocalDate": return "java.time.LocalDate.now()";
            case "java.time.LocalTime": return "java.time.LocalTime.now()";
            case "java.time.ZonedDateTime": return "java.time.ZonedDateTime.now()";
            case "java.time.Duration": return "java.time.Duration.ofHours(1)";
            case "java.time.Period": return "java.time.Period.ofDays(1)";
            case "java.sql.Date": return "java.sql.Date.valueOf(java.time.LocalDate.now())";
            case "org.springframework.web.multipart.MultipartFile": return "new org.springframework.mock.web.MockMultipartFile(\"file.txt\", \"content\".getBytes())";
            case "org.springframework.core.io.Resource": return "org.mockito.Mockito.mock(org.springframework.core.io.Resource.class)";
            case "java.nio.file.Path": return "org.mockito.Mockito.mock(java.nio.file.Path.class)";
            case "java.time.OffsetDateTime": return "java.time.OffsetDateTime.now()";
            case "net.sf.jasperreports.engine.data.JRBeanCollectionDataSource": return "new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(new java.util.ArrayList())";

            case "java.lang.CharSequence":
            case "java.lang.String": return "\"a string\"";

            case "java.lang.Double":
            case "double": return "1D";

            case "java.lang.Float":
            case "float": return "1F";

            case "java.lang.Long":
            case "long": return "1L";

            case "java.lang.Short": return "(short) 1";
            case "java.lang.Byte": return "(byte) 1";

            case "java.lang.Integer":
            case "java.lang.Character":
            case "short":
            case "byte":
            case "char":
            case "int": return "1";

            default: return "new " + typeParameterElement + "()";
        }
    }

    public static String extractType(final String typeParameterElement) {
        if (typeParameterElement.contains("<") && typeParameterElement.contains(">")) {
            return typeParameterElement.substring(typeParameterElement.indexOf("<") + 1, typeParameterElement.lastIndexOf(">"));
        }
        return typeParameterElement;
    }

    public static String getPropertyType(Element element, String property) {
        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD && enclosedElement.getSimpleName().toString().equals(property)) {
                return enclosedElement.asType().toString();
            }
        }
        return "Object";
    }

    public static boolean hasMethod(Element typeElement, String method) {
        return typeElement.getEnclosedElements().stream()
                .filter(methodElement -> methodElement.getKind() == ElementKind.METHOD)
                .map(Element::getSimpleName)
                .anyMatch(name -> name.contentEquals(method));
    }

    public static boolean hasHashCodeMethod(Element typeElement) {
        return hasMethod(typeElement, "hashCode");
    }

    public static  boolean hasGetterAndSetter(Element typeElement, Element fieldElement) {
        final var fieldName = fieldElement.getSimpleName().toString();
        final var getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        final var setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

        return typeElement.getEnclosedElements().stream()
                .filter(methodElement -> methodElement.getKind() == ElementKind.METHOD)
                .map(Element::getSimpleName)
                .anyMatch(name -> name.contentEquals(getterName) || name.contentEquals(setterName));
    }

    public static List<String> getElementProperties(Element element) {
        List<String> entityProperties = new ArrayList<>();
        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD && ElementUtils.hasGetterAndSetter(element, enclosedElement)) {
                entityProperties.add(enclosedElement.getSimpleName().toString());
            }
        }
        return entityProperties;
    }


}
