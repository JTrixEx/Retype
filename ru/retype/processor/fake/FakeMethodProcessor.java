package ru.retype.processor.fake;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;
import ru.retype.ObfuscationContext;
import ru.retype.processor.IProcessor;
import ru.retype.utils.NameUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class FakeMethodProcessor implements IProcessor {

    private AtomicInteger atomicInteger = new AtomicInteger();
    private ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isFakeMethodsEnabled())
            return;
        context.getClassNodes().forEach(classNode -> {
            int fakeMethodAccess = Opcodes.ACC_PUBLIC;
            fakeMethodAccess |= Opcodes.ACC_STATIC;
            fakeMethodAccess |= Opcodes.ACC_SYNTHETIC;
            fakeMethodAccess |= Opcodes.ACC_VARARGS;
            fakeMethodAccess |= Opcodes.ACC_FINAL;
            fakeMethodAccess |= Opcodes.ACC_BRIDGE;
            fakeMethodAccess |= Opcodes.ACC_STRICT;

            InsnList callInsn = new InsnList();
            for(int i = 0; i < 6; i++) {
                MethodNode methodNode = new MethodNode(fakeMethodAccess, NameUtils.generateString(atomicInteger.incrementAndGet()), "()V", null, null);

                Collections.shuffle(classNode.fields);
                List<FieldNode> prevFieldNodes = new ArrayList<>();
                for(FieldNode fieldNode : classNode.fields) {
                    if(fieldNode.name.startsWith("retype->")) {
                        methodNode.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, fieldNode.name, "I"));
                        methodNode.instructions.add(new VarInsnNode(Opcodes.ISTORE, 0));
                        methodNode.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
                        if(prevFieldNodes.size() > 0) {
                            for(int v = 0; v < prevFieldNodes.size(); v++) {
                                methodNode.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, prevFieldNodes.get(v).name, "I"));
                                methodNode.instructions.add(new VarInsnNode(Opcodes.ISTORE, v + 1));
                                methodNode.instructions.add(new VarInsnNode(Opcodes.ILOAD, v + 1));
                                for(int l = 0; l < (2 + threadLocalRandom.nextInt(6)); l++) {
                                    int rndID = threadLocalRandom.nextInt(prevFieldNodes.size());
                                    pushRandomInsn(methodNode.instructions);
                                    methodNode.instructions.add(new VarInsnNode(Opcodes.ISTORE, rndID));
                                    methodNode.instructions.add(new VarInsnNode(Opcodes.ILOAD, rndID));

                                    if(l % 3 == 0) {
                                        pushRandomCheck(methodNode.instructions, prevFieldNodes);
                                    }
                                }
                            }
                        }

                        methodNode.instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, classNode.name, fieldNode.name, "I"));
                        prevFieldNodes.add(fieldNode);
                    }
                }
                methodNode.instructions.add(new InsnNode(Opcodes.RETURN));
                classNode.methods.add(methodNode);

                callInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, classNode.name, methodNode.name, "()V"));
            }

            MethodNode initializerMethod = getClassInitializerMethod(classNode);
            if (initializerMethod.instructions == null || initializerMethod.instructions.size() == 0) {
                initializerMethod.instructions = callInsn;
                initializerMethod.instructions.add(new InsnNode(Opcodes.RETURN));
            } else {
                initializerMethod.instructions.insertBefore(initializerMethod.instructions.get(0), callInsn);
            }
        });
    }

    private void pushRandomCheck(InsnList insnList, List<FieldNode> nodes) {
        insnList.add(new VarInsnNode(Opcodes.ILOAD, threadLocalRandom.nextInt(nodes.size())));
        pushRandomInsn(insnList);
        insnList.add(new VarInsnNode(Opcodes.ILOAD, threadLocalRandom.nextInt(nodes.size())));

        LabelNode firstLabel = new LabelNode();
        insnList.add(new JumpInsnNode(Opcodes.IF_ICMPLE, firstLabel));

        for(int l = 0; l < threadLocalRandom.nextInt(6); l++) {
            int rndID = threadLocalRandom.nextInt(nodes.size());
            pushRandomInsn(insnList);
            insnList.add(new VarInsnNode(Opcodes.ISTORE, rndID));
            insnList.add(new VarInsnNode(Opcodes.ILOAD, rndID));
        }
        /*for(int i = 0; i < 2; i++) {
            int rndID = threadLocalRandom.nextInt(nodes.size());
            pushRandomInsn(insnList);
            insnList.add(new VarInsnNode(Opcodes.ILOAD, rndID));
        }*/
        insnList.add(firstLabel);
        insnList.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
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
