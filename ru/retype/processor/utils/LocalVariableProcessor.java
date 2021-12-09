package ru.retype.processor.utils;

import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.LineNumberNode;
import jdk.internal.org.objectweb.asm.tree.LocalVariableNode;
import ru.retype.ObfuscationContext;
import ru.retype.processor.IProcessor;

public class LocalVariableProcessor implements IProcessor {

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isLocalVariableRenameEnabled())
            return;
        context.getClassNodes().forEach(classNode -> {
            classNode.methods.forEach(methodNode -> {
                methodNode.localVariables.forEach(localVariableNode -> localVariableNode.name = "this");
            });
        });
    }
}
