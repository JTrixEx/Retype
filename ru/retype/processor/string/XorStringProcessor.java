package ru.retype.processor.string;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.*;
import ru.retype.ObfuscationContext;
import ru.retype.processor.IProcessor;
import ru.retype.utils.Logger;
import ru.retype.utils.NameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class XorStringProcessor implements IProcessor {

    private ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isXorStringEnabled())
            return;

        context.getClassNodes().forEach(classNode -> {
            List<String> classStrings = new ArrayList<>();

            AtomicInteger atomicInteger = new AtomicInteger(0);
            classNode.methods.forEach(methodNode -> {
                InsnList insnList = methodNode.instructions;
                for(int i = 0; i < insnList.size(); i++) {
                    AbstractInsnNode abstractInsnNode = insnList.get(i);
                    if(abstractInsnNode instanceof LdcInsnNode) {
                        LdcInsnNode ldcInsnNode = (LdcInsnNode) abstractInsnNode;
                        if(ldcInsnNode.cst instanceof String) {
                            String s = (String)ldcInsnNode.cst;
                            classStrings.add(s);

                            InsnList getInsnList = new InsnList();
                            getInsnList.add(new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, "a", "[Ljava/lang/String;"));

                            int v = atomicInteger.getAndIncrement();

                            int firstKey = threadLocalRandom.nextInt(25565 * 2);
                            int secondKey = threadLocalRandom.nextInt(25565 * 2);
                            int thirdKey = threadLocalRandom.nextInt(25565 * 2);
                            int res = (v ^ firstKey ^ thirdKey) + secondKey;

                            pushInt(getInsnList, res);
                            pushInt(getInsnList, secondKey);
                            getInsnList.add(new InsnNode(Opcodes.ISUB));
                            pushInt(getInsnList, firstKey);
                            getInsnList.add(new InsnNode(Opcodes.IXOR));
                            pushInt(getInsnList, thirdKey);
                            getInsnList.add(new InsnNode(Opcodes.IXOR));

                            getInsnList.add(new InsnNode(Opcodes.AALOAD));
                            insnList.insertBefore(abstractInsnNode, getInsnList);
                            insnList.remove(abstractInsnNode);
                        }
                    }
                }
            });
            if(classStrings.size() > 0) {
                MethodNode initializerMethod = getClassInitializerMethod(classNode);
                FieldNode fieldNode = new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "a", "[Ljava/lang/String;", null, null);
                classNode.fields.add(fieldNode);

                InsnList decryptionInsn = createDecryptionInstructions(classNode, classStrings);
                if (initializerMethod.instructions == null || initializerMethod.instructions.size() == 0) {
                    initializerMethod.instructions = decryptionInsn;
                    initializerMethod.instructions.add(new InsnNode(Opcodes.RETURN));
                } else {
                    initializerMethod.instructions.insertBefore(initializerMethod.instructions.get(0), decryptionInsn);
                }

                classNode.methods.add(createDecryptionMethod(classNode));
            }
        });
    }

    private MethodNode createDecryptionMethod(ClassNode classNode) {
        MethodNode methodNode = new MethodNode(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL
                | Opcodes.ACC_PRIVATE, "a", "(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;", null, null);

        LabelNode startLabel = new LabelNode();
        LabelNode endLabel = new LabelNode();

        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C"));
        insnList.add(new VarInsnNode(Opcodes.ASTORE, 3));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C"));
        insnList.add(new VarInsnNode(Opcodes.ASTORE, 4));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 4));
        insnList.add(new InsnNode(Opcodes.ARRAYLENGTH));
        insnList.add(new IntInsnNode(Opcodes.NEWARRAY, 5));
        insnList.add(new VarInsnNode(Opcodes.ASTORE, 5));
        insnList.add(new InsnNode(Opcodes.ICONST_0));

        insnList.add(new VarInsnNode(Opcodes.ISTORE, 6));
        insnList.add(startLabel);

        Object[] localFrame = new Object[7];
        localFrame[0] = "java/lang/String";
        localFrame[1] = "java/lang/String";
        localFrame[2] = 1;
        localFrame[3] = "[C";
        localFrame[4] = "[C";
        localFrame[5] = "[C";
        localFrame[6] = 1;

        insnList.add(new FrameNode(0, localFrame.length, localFrame, 0, new Object[0]));
        insnList.add(new VarInsnNode(Opcodes.ILOAD, 6));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 4));
        insnList.add(new InsnNode(Opcodes.ARRAYLENGTH));

        insnList.add(new JumpInsnNode(Opcodes.IF_ICMPGE, endLabel));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 5));
        insnList.add(new VarInsnNode(Opcodes.ILOAD, 6));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 4));
        insnList.add(new VarInsnNode(Opcodes.ILOAD, 6));
        insnList.add(new InsnNode(Opcodes.CALOAD));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 3));
        insnList.add(new VarInsnNode(Opcodes.ILOAD, 6));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 3));
        insnList.add(new InsnNode(Opcodes.ARRAYLENGTH));
        insnList.add(new InsnNode(Opcodes.IREM));
        insnList.add(new InsnNode(Opcodes.CALOAD));
        insnList.add(new InsnNode(Opcodes.IXOR));
        //insnList.add(new VarInsnNode(Opcodes.ILOAD, 2));
        //insnList.add(new InsnNode(Opcodes.IXOR));
        insnList.add(new InsnNode(Opcodes.I2C));
        insnList.add(new InsnNode(Opcodes.CASTORE));
        insnList.add(new IincInsnNode(6, 1));
        insnList.add(new JumpInsnNode(Opcodes.GOTO, startLabel));
        insnList.add(endLabel);
        insnList.add(new FrameNode(2, 0, null, 0, null));
        insnList.add(new TypeInsnNode(Opcodes.NEW, "java/lang/String"));
        insnList.add(new InsnNode(Opcodes.DUP));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 5));
        insnList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([C)V"));
        insnList.add(new InsnNode(Opcodes.ARETURN));

        methodNode.instructions = insnList;
        return methodNode;
    }

    private InsnList createDecryptionInstructions(ClassNode classNode, List<String> strings) {
        String[] stringArray = strings.toArray(new String[0]);
        int size = strings.size();

        InsnList insnList = new InsnList();
        insnList.add(new LdcInsnNode(Type.getType("L" + classNode.name + ";")));
        insnList.add(new VarInsnNode(Opcodes.ASTORE,0));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;"));
        insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I"));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getModifiers", "()I"));
        insnList.add(new InsnNode(Opcodes.IADD));
        insnList.add(new VarInsnNode(Opcodes.ISTORE, 1));

        insnList.add(new IntInsnNode(size >= -128 && size <= 127 ? Opcodes.BIPUSH : Opcodes.SIPUSH, size));
        insnList.add(new VarInsnNode(Opcodes.ISTORE, 2));
        insnList.add(new VarInsnNode(Opcodes.ILOAD, 2));
        insnList.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/String"));
        insnList.add(new FieldInsnNode(Opcodes.PUTSTATIC, classNode.name, "a", "[Ljava/lang/String;"));

        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

        for(int i = 0; i < size; i++) {
            String xorKey = NameUtils.getRandomString(10);

            insnList.add(new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, "a", "[Ljava/lang/String;"));

            int encryptKey = classNode.name.replace("/", ".").length() + classNode.access;
            int x = threadLocalRandom.nextInt(255);
            int y = threadLocalRandom.nextInt(255);

            int firstKey = threadLocalRandom.nextInt(25565 * 2);
            int secondKey = threadLocalRandom.nextInt(25565 * 2);
            int thirdKey = threadLocalRandom.nextInt(25565 * 2);
            int res = (i ^ firstKey ^ thirdKey) + secondKey;

            pushInt(insnList, res);
            pushInt(insnList, secondKey);
            insnList.add(new InsnNode(Opcodes.ISUB));
            pushInt(insnList, firstKey);
            insnList.add(new InsnNode(Opcodes.IXOR));
            pushInt(insnList, thirdKey);
            insnList.add(new InsnNode(Opcodes.IXOR));

            insnList.add(new LdcInsnNode(xorString(stringArray[i], xorKey, encryptKey ^ x ^ y)));
            insnList.add(new LdcInsnNode(xorKey));
            insnList.add(new VarInsnNode(Opcodes.ILOAD, 1));

            firstKey = threadLocalRandom.nextInt(25565 * 2);
            secondKey = threadLocalRandom.nextInt(25565 * 2);
            thirdKey = threadLocalRandom.nextInt(25565 * 2);
            res = (x ^ firstKey ^ thirdKey) + secondKey;

            pushInt(insnList, res);
            pushInt(insnList, secondKey);
            insnList.add(new InsnNode(Opcodes.ISUB));
            pushInt(insnList, firstKey);
            insnList.add(new InsnNode(Opcodes.IXOR));
            pushInt(insnList, thirdKey);
            insnList.add(new InsnNode(Opcodes.IXOR));

            insnList.add(new InsnNode(Opcodes.IXOR));

            firstKey = threadLocalRandom.nextInt(25565 * 2);
            secondKey = threadLocalRandom.nextInt(25565 * 2);
            thirdKey = threadLocalRandom.nextInt(25565 * 2);
            res = (y ^ firstKey ^ thirdKey) + secondKey;

            pushInt(insnList, res);
            pushInt(insnList, secondKey);
            insnList.add(new InsnNode(Opcodes.ISUB));
            pushInt(insnList, firstKey);
            insnList.add(new InsnNode(Opcodes.IXOR));
            pushInt(insnList, thirdKey);
            insnList.add(new InsnNode(Opcodes.IXOR));

            insnList.add(new InsnNode(Opcodes.IXOR));

            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, classNode.name, "a", "(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;"));
            insnList.add(new InsnNode(Opcodes.AASTORE));
        }

        return insnList;
    }

    private String xorString(String s, String xorKey, int intKey) {
        char[] key = xorKey.toCharArray();
        char[] orig = s.toCharArray();
        char[] res = new char[orig.length];
        for(int i = 0; i < orig.length; i++) {
            res[i] = (char)(orig[i] ^ key[i % key.length]);
        }
        return new String(res);
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
