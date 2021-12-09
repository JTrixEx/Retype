package ru.retype.processor.string;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;
import ru.retype.ObfuscationContext;
import ru.retype.processor.IProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class StringHideProcessor implements IProcessor {

    private ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isStringHideEnabled())
            return;
        context.getClassNodes().forEach(classNode -> {
            List<String> classStrings = new ArrayList<>();

            AtomicInteger atomicInteger = new AtomicInteger(0);
            classNode.methods.forEach(methodNode -> {
                for(int i = 0; i < methodNode.instructions.size(); i++) {
                    AbstractInsnNode abstractInsnNode = methodNode.instructions.get(i);
                    if(abstractInsnNode instanceof LdcInsnNode) {
                        LdcInsnNode ldcInsnNode = (LdcInsnNode) abstractInsnNode;
                        if(ldcInsnNode.cst instanceof String) {
                            String s = (String)ldcInsnNode.cst;
                            classStrings.add(s);

                            InsnList getInsnList = new InsnList();
                            getInsnList.add(new TypeInsnNode(Opcodes.NEW, "java/lang/String"));
                            getInsnList.add(new InsnNode(Opcodes.DUP));
                            getInsnList.add(new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, "b", "[[B"));

                            int firstKey = threadLocalRandom.nextInt(25565 * 2);
                            int secondKey = threadLocalRandom.nextInt(25565 * 2);
                            int thirdKey = threadLocalRandom.nextInt(25565 * 2);
                            int res = (atomicInteger.getAndIncrement() ^ firstKey ^ thirdKey) + secondKey;

                            pushInt(getInsnList, res);
                            pushInt(getInsnList, secondKey);
                            getInsnList.add(new InsnNode(Opcodes.ISUB));
                            pushInt(getInsnList, firstKey);
                            getInsnList.add(new InsnNode(Opcodes.IXOR));
                            pushInt(getInsnList, thirdKey);
                            getInsnList.add(new InsnNode(Opcodes.IXOR));

                            getInsnList.add(new InsnNode(Opcodes.AALOAD));
                            getInsnList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"));
                            methodNode.instructions.insertBefore(abstractInsnNode, getInsnList);
                            methodNode.instructions.remove(abstractInsnNode);
                            methodNode.instructions.add(new InsnNode(Opcodes.POP));
                        }
                    }
                }
            });
            if(classStrings.size() > 0) {
                MethodNode initializerMethod = getClassInitializerMethod(classNode);
                FieldNode fieldNode = new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "b", "[[B", null, null);
                classNode.fields.add(fieldNode);

                InsnList decryptionInsn = createDecryptionInsn(classNode, classStrings);
                if (initializerMethod.instructions == null || initializerMethod.instructions.size() == 0) {
                    initializerMethod.instructions = decryptionInsn;
                    initializerMethod.instructions.add(new InsnNode(Opcodes.RETURN));
                } else {
                    initializerMethod.instructions.insertBefore(initializerMethod.instructions.get(0), decryptionInsn);
                }
            }
        });
    }

    private InsnList createDecryptionInsn(ClassNode classNode, List<String> strings) {
        int size = strings.size();

        InsnList insnList = new InsnList();
        pushInt(insnList, size);
        insnList.add(new TypeInsnNode(Opcodes.ANEWARRAY, "[B"));
        insnList.add(new FieldInsnNode(Opcodes.PUTSTATIC, classNode.name, "b", "[[B"));

        for(int i = 0; i < strings.size(); i++) {
            insnList.add(new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, "b", "[[B"));

            int posKey0 = threadLocalRandom.nextInt(25565 * 2);
            int posKey1 = threadLocalRandom.nextInt(25565 * 2);
            int pos = (i ^ posKey0) + posKey1;
            pushInt(insnList, pos);
            pushInt(insnList, posKey1);
            insnList.add(new InsnNode(Opcodes.ISUB));
            pushInt(insnList, posKey0);
            insnList.add(new InsnNode(Opcodes.IXOR));

            byte[] bytes = strings.get(i).getBytes();

            posKey0 = threadLocalRandom.nextInt(25565 * 2);
            posKey1 = threadLocalRandom.nextInt(25565 * 2);
            pos = (bytes.length ^ posKey0) + posKey1;
            pushInt(insnList, pos);
            pushInt(insnList, posKey1);
            insnList.add(new InsnNode(Opcodes.ISUB));
            pushInt(insnList, posKey0);
            insnList.add(new InsnNode(Opcodes.IXOR));

            insnList.add(new IntInsnNode(Opcodes.NEWARRAY, 8));
            insnList.add(new InsnNode(Opcodes.AASTORE));

            for(int x = 0; x < bytes.length; x++) {
                insnList.add(new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, "b", "[[B"));

                posKey0 = threadLocalRandom.nextInt(25565 * 2);
                posKey1 = threadLocalRandom.nextInt(25565 * 2);
                pos = (i ^ posKey0) + posKey1;
                pushInt(insnList, pos);
                pushInt(insnList, posKey1);
                insnList.add(new InsnNode(Opcodes.ISUB));
                pushInt(insnList, posKey0);
                insnList.add(new InsnNode(Opcodes.IXOR));

                insnList.add(new InsnNode(Opcodes.AALOAD));

                posKey0 = threadLocalRandom.nextInt(25565 * 2);
                posKey1 = threadLocalRandom.nextInt(25565 * 2);
                pos = (x ^ posKey0) + posKey1;
                pushInt(insnList, pos);
                pushInt(insnList, posKey1);
                insnList.add(new InsnNode(Opcodes.ISUB));
                pushInt(insnList, posKey0);
                insnList.add(new InsnNode(Opcodes.IXOR));

                byte cbyte = bytes[x];
                int firstKey = threadLocalRandom.nextInt(25565 * 2);
                int secondKey = threadLocalRandom.nextInt(25565 * 2);
                int thirdKey = threadLocalRandom.nextInt(25565 * 2);
                int resByte = (cbyte ^ firstKey ^ thirdKey) + secondKey;

                pushInt(insnList, resByte);
                pushInt(insnList, secondKey);
                insnList.add(new InsnNode(Opcodes.ISUB));
                pushInt(insnList, firstKey);
                insnList.add(new InsnNode(Opcodes.IXOR));
                pushInt(insnList, thirdKey);
                insnList.add(new InsnNode(Opcodes.IXOR));
                insnList.add(new InsnNode(Opcodes.BASTORE));
            }
        }

        return insnList;
    }

    private void pushInt(InsnList insnList, int i) {
        if(i <= 5) {
            switch (i) {
                case 0: {
                    insnList.add(new InsnNode(Opcodes.ICONST_0));
                    break;
                }
                case 1: {
                    insnList.add(new InsnNode(Opcodes.ICONST_1));
                    break;
                }
                case 2: {
                    insnList.add(new InsnNode(Opcodes.ICONST_2));
                    break;
                }
                case 3: {
                    insnList.add(new InsnNode(Opcodes.ICONST_3));
                    break;
                }
                case 4: {
                    insnList.add(new InsnNode(Opcodes.ICONST_4));
                    break;
                }
                case 5: {
                    insnList.add(new InsnNode(Opcodes.ICONST_5));
                    break;
                }
            }
        }
        else if(i >= -128 && i <= 127)
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
