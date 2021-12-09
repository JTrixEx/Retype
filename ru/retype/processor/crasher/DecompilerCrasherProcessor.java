package ru.retype.processor.crasher;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.*;
import ru.retype.ObfuscationContext;
import ru.retype.processor.IProcessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class DecompilerCrasherProcessor implements IProcessor {

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isDecompilerCrasherEnabled())
            return;

        context.getClassNodes().forEach(classNode -> {
            classNode.methods.forEach(methodNode -> {
                if(!Modifier.isNative(methodNode.access) && !Modifier.isAbstract(methodNode.access)) {
                    InsnList newInsns = new InsnList();
                    newInsns.insert(new InsnNode(Opcodes.POP2));
                    newInsns.insert(new TypeInsnNode(Opcodes.CHECKCAST, "\n\n\n\nWhat are u doinx man???\n\n\n\n"));
                    newInsns.insert(new InsnNode(Opcodes.ACONST_NULL));
                    newInsns.insert(getNullLDC());
                    methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), newInsns);
                }
            });
        });
    }

    private static AbstractInsnNode getNullLDC() {
        try {
            Constructor<Type> typeConstructor = Type.class.getDeclaredConstructor(int.class, char[].class, int.class, int.class);
            typeConstructor.setAccessible(true);
            return new LdcInsnNode(typeConstructor.newInstance(11, "()C".toCharArray(), 0, 3));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
