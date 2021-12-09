package ru.retype.processor.utils;

import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.LineNumberNode;
import jdk.internal.org.objectweb.asm.tree.ParameterNode;
import ru.retype.ObfuscationContext;
import ru.retype.processor.IProcessor;
import ru.retype.utils.NameUtils;

import javax.naming.Name;

public class LineNumberProcessor implements IProcessor {

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isLineNumberRemoveEnabled())
            return;
        context.getClassNodes().forEach(classNode -> {
            classNode.methods.forEach(methodNode -> {
                for(int i = 0; i < methodNode.instructions.size(); i++) {
                    AbstractInsnNode abstractInsnNode = methodNode.instructions.get(i);
                    if(abstractInsnNode instanceof LineNumberNode) {
                        LineNumberNode lineNumberNode = (LineNumberNode)abstractInsnNode;
                        methodNode.instructions.remove(lineNumberNode);
                    }
                }
                if (methodNode.parameters != null) {
                    for (ParameterNode parameter : methodNode.parameters) {
                        parameter.name = NameUtils.getRandomString(10);
                    }
                }
            });
            classNode.sourceFile = null;
            classNode.sourceDebug = null;
        });
    }
}
