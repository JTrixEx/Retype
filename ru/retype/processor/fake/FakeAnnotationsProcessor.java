package ru.retype.processor.fake;

import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
import ru.retype.ObfuscationContext;
import ru.retype.processor.IProcessor;

import java.util.ArrayList;

public class FakeAnnotationsProcessor implements IProcessor {

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isFakeAnnotationsEnabled())
            return;
        context.getClassNodes().forEach(classNode -> {
            if (classNode.visibleAnnotations == null)
                classNode.visibleAnnotations = new ArrayList<>();
            if (classNode.invisibleAnnotations == null)
                classNode.invisibleAnnotations = new ArrayList<>();

            classNode.visibleAnnotations.add(new AnnotationNode("@"));
            classNode.invisibleAnnotations.add(new AnnotationNode("@"));

            classNode.fields.forEach(fieldNode -> {
                if (fieldNode.visibleAnnotations == null)
                    fieldNode.visibleAnnotations = new ArrayList<>();
                if (fieldNode.invisibleAnnotations == null)
                    fieldNode.invisibleAnnotations = new ArrayList<>();

                fieldNode.visibleAnnotations.add(new AnnotationNode("@"));
                fieldNode.invisibleAnnotations.add(new AnnotationNode("@"));
            });

            classNode.methods.forEach(methodNode -> {
                if (methodNode.visibleAnnotations == null)
                    methodNode.visibleAnnotations = new ArrayList<>();
                if (methodNode.invisibleAnnotations == null)
                    methodNode.invisibleAnnotations = new ArrayList<>();

                methodNode.visibleAnnotations.add(new AnnotationNode("@"));
                methodNode.invisibleAnnotations.add(new AnnotationNode("@"));
            });
        });
    }
}
