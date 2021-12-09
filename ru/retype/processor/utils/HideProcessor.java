package ru.retype.processor.utils;

import jdk.internal.org.objectweb.asm.Opcodes;
import ru.retype.ObfuscationContext;
import ru.retype.processor.IProcessor;

import java.util.Collections;

public class HideProcessor implements IProcessor {

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isMemberHideEnabled())
            return;

        context.getClassNodes().forEach(classNode -> {
            classNode.methods.forEach(methodNode -> {
                if (methodNode.name.startsWith("<"))
                    return;
                if ((methodNode.access & Opcodes.ACC_NATIVE) == 0) {
                    return;
                }
                methodNode.access = methodNode.access | Opcodes.ACC_BRIDGE;
                methodNode.access = methodNode.access | Opcodes.ACC_SYNTHETIC;
            });
            classNode.fields.forEach(fieldNode -> {
                fieldNode.access = fieldNode.access | Opcodes.ACC_SYNTHETIC;
            });
        });
    }
}
