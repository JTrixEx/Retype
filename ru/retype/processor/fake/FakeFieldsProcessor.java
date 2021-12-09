package ru.retype.processor.fake;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;
import ru.retype.ObfuscationContext;
import ru.retype.processor.IProcessor;
import ru.retype.utils.NameUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class FakeFieldsProcessor implements IProcessor {

    private AtomicInteger atomicInteger = new AtomicInteger(0);
    private ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isFakeFieldsEnabled())
            return;
        context.getClassNodes().forEach(classNode -> {
            int fakeFieldAccess = Opcodes.ACC_PUBLIC;
            fakeFieldAccess |= Opcodes.ACC_STATIC;
            fakeFieldAccess |= Opcodes.ACC_FINAL;
            fakeFieldAccess |= Opcodes.ACC_STRICT;
            fakeFieldAccess |= Opcodes.ACC_SYNTHETIC;
            fakeFieldAccess |= Opcodes.ACC_VARARGS;
            fakeFieldAccess |= Opcodes.ACC_ENUM;
            fakeFieldAccess |= Opcodes.ACC_SYNCHRONIZED;

            InsnList fieldAccessInsn = new InsnList();

            for(int i = 0; i < 5; i++) {
                String fieldName = "retype->" + NameUtils.generateString(atomicInteger.incrementAndGet());
                classNode.fields.add(new FieldNode(fakeFieldAccess, fieldName, "I", null, threadLocalRandom.nextInt(255)));

                fieldAccessInsn.add(new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, fieldName, "I"));
                fieldAccessInsn.add(new VarInsnNode(Opcodes.ISTORE, 0));
                fieldAccessInsn.add(new VarInsnNode(Opcodes.ILOAD, 0));

                for(int l = 0; l < (6 + threadLocalRandom.nextInt(6)); l++) {
                    pushRandomInsn(fieldAccessInsn);
                }

                fieldAccessInsn.add(new FieldInsnNode(Opcodes.PUTSTATIC, classNode.name, fieldName, "I"));
            }

            MethodNode initializerMethod = getClassInitializerMethod(classNode);
            if (initializerMethod.instructions == null || initializerMethod.instructions.size() == 0) {
                initializerMethod.instructions = fieldAccessInsn;
                initializerMethod.instructions.add(new InsnNode(Opcodes.RETURN));
            } else {
                initializerMethod.instructions.insertBefore(initializerMethod.instructions.get(0), fieldAccessInsn);
            }
        });
    }

    private void pushRandomInsn(InsnList insnList) {
        int mode = threadLocalRandom.nextInt(5);
        pushInt(insnList, threadLocalRandom.nextInt(25565));
        insnList.add(new InsnNode(mode == 0 ? Opcodes.IXOR : mode == 1 ? Opcodes.IREM : mode == 2 ? Opcodes.ISUB : mode == 3 ? Opcodes.IDIV : Opcodes.IMUL));
    }

    private void pushInt(InsnList insnList, int i) {
        if(i >= -128 && i <= 127)
            insnList.add(new IntInsnNode(Opcodes.BIPUSH, i));
        else if(i >= -32768 && i <= 32767)
            insnList.add(new IntInsnNode(Opcodes.SIPUSH, i));
        else
            insnList.add(new LdcInsnNode(i));
    }

    private MethodNode getClassInitializerMethod(ClassNode classNode) {
        return classNode.methods.stream().filter(methodNode -> methodNode.name.equals("<clinit>")).findFirst().orElse(new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null));
    }
}
