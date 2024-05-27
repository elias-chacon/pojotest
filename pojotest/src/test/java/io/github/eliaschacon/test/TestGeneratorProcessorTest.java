package io.github.eliaschacon.test;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.Set;
//import com.sun.tools.javac.processing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

class TestGeneratorProcessorTest {

    @Test
    public void shouldGenerateClass() {
        final var element = Mockito.mock(TypeElement.class);
        final var roundEnv = Mockito.mock(RoundEnvironment.class);
        final var annotations = new HashSet<TypeElement>();

//        JavacProcessingEnvironment

        doReturn(ElementKind.CLASS).when(element).getKind();
        doReturn(new NameImpl("Pojo")).when(element).getSimpleName();
        doReturn(new NameImpl("io.github.eliaschacon.domain.Pojo")).when(element).getQualifiedName();
//        doReturn("io.github.eliaschacon.domain.Pojo").when((TypeElement) element).get();

        doReturn(Set.of(element)).when(roundEnv).getElementsAnnotatedWith(TestGenerator.class);

        new TestGeneratorProcessor().process(annotations, roundEnv);
    }

    static class NameImpl implements Name {

        CharSequence cs = "";

        public NameImpl(CharSequence cs) {
            this.cs = cs;
        }

        @Override
        public boolean contentEquals(CharSequence cs) {
            return cs.equals(cs);
        }

        @Override
        public int length() {
            return cs.length();
        }

        @Override
        public char charAt(int index) {
            return cs.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return cs.subSequence(start, end);
        }

        @Override
        public String toString() {
            return cs.toString();
        }
    }
}