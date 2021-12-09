package ru.retype.processor.flow;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.*;
import ru.retype.ObfuscationContext;
import ru.retype.processor.IProcessor;
import ru.retype.utils.Logger;
import ru.retype.utils.StackAnalyzer;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LightFlowProcessor implements IProcessor {

    private ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isLightFlowEnabled())
            return;
        context.getClassNodes().forEach(classNode -> {
            classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "x", "I", null, 0));
            classNode.methods.forEach(methodNode -> {
                StackAnalyzer stackAnalyzer = new StackAnalyzer(methodNode.instructions);
                Arrays.stream(methodNode.instructions.toArray())
                .filter(insn -> insn.getOpcode() == Opcodes.NEW
                        || (insn.getOpcode() >= Opcodes.PUTSTATIC && insn.getOpcode() <= Opcodes.PUTFIELD)
                        || (insn.getOpcode() >= Opcodes.INVOKEVIRTUAL && insn.getOpcode() < Opcodes.INVOKEDYNAMIC))
                .forEach(insn -> {
                    int stackVarID = stackAnalyzer.getStackVarID();
                    InsnList flowInsn = new InsnList();
                    IntData intData = pushRandomInsn(flowInsn);

                    int ifOperand = -1;
                    switch (intData.getOperand()) {
                        case Opcodes.IXOR: {
                            ifOperand = (intData.getFirstValue() ^ intData.getSecondValue()) <= intData.getResult() ? Opcodes.IF_ICMPGT : Opcodes.IF_ICMPLE;
                            break;
                        }
                        case Opcodes.IADD: {
                            ifOperand = (intData.getFirstValue() + intData.getSecondValue()) <= intData.getResult() ? Opcodes.IF_ICMPGT : Opcodes.IF_ICMPLE;
                            break;
                        }
                        case Opcodes.ISUB: {
                            ifOperand = (intData.getFirstValue() - intData.getSecondValue()) <= intData.getResult() ? Opcodes.IF_ICMPGT : Opcodes.IF_ICMPLE;
                            break;
                        }
                    }
                    LabelNode fakeLabel = new LabelNode();
                    LabelNode labelNode = new LabelNode();
                    flowInsn.add(new JumpInsnNode(ifOperand, fakeLabel));
                    pushFakeBlock(classNode, flowInsn);
                    flowInsn.add(fakeLabel);

                    for(int x = 0; x < 4; x++) {
                        if(x % 2 == 0) {
                            pushRandomInsn(flowInsn);
                        } else {
                            pushRandomInsnWithCall(flowInsn);
                        }
                        int d = x % 5;
                        flowInsn.add(new JumpInsnNode(d == 0 ? Opcodes.IF_ICMPGT : d == 1 ? Opcodes.IF_ICMPLE : d == 2 ? Opcodes.IF_ICMPLT : d == 3 ? Opcodes.IF_ICMPGE : Opcodes.IF_ICMPEQ, labelNode));
                        pushFakeBlock(classNode, flowInsn);
                    }

                    flowInsn.add(labelNode);
                    methodNode.instructions.insertBefore(insn, flowInsn);
                });
            });
        });
    }

    private void pushFakeBlock(ClassNode classNode, InsnList flowInsn) {
        flowInsn.add(new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, "x", "I"));
        pushInt(flowInsn, threadLocalRandom.nextInt(25565));
        int mode = threadLocalRandom.nextInt(3);
        int operand = mode == 0 ? Opcodes.IXOR : mode == 1 ? Opcodes.IADD : Opcodes.ISUB;
        flowInsn.add(new InsnNode(operand));

        pushInt(flowInsn, threadLocalRandom.nextInt(25565));
        mode = threadLocalRandom.nextInt(3);
        operand = mode == 0 ? Opcodes.IXOR : mode == 1 ? Opcodes.IADD : Opcodes.ISUB;
        flowInsn.add(new InsnNode(operand));

        flowInsn.add(new LdcInsnNode(Type.getType("L" + classNode.name + ";")));
        flowInsn.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;"));
        flowInsn.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I"));
        mode = threadLocalRandom.nextInt(3);
        operand = mode == 0 ? Opcodes.IXOR : mode == 1 ? Opcodes.IADD : Opcodes.ISUB;
        flowInsn.add(new InsnNode(operand));
        pushInt(flowInsn, threadLocalRandom.nextInt(25565));
        mode = threadLocalRandom.nextInt(3);
        operand = mode == 0 ? Opcodes.IXOR : mode == 1 ? Opcodes.IADD : Opcodes.ISUB;
        flowInsn.add(new InsnNode(operand));
        flowInsn.add(new FieldInsnNode(Opcodes.PUTSTATIC, classNode.name, "x", "I"));
    }

    private IntData pushRandomInsnWithCall(InsnList insnList) {
        int mode = threadLocalRandom.nextInt(3);
        int x = pushInt(insnList, threadLocalRandom.nextInt(25565));
        int y = pushInt(insnList, threadLocalRandom.nextInt(25565));
        int z = pushInt(insnList, threadLocalRandom.nextInt(25565));
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, mode == 0 ? "java/lang/Math" : "java/lang/Integer", mode == 0 ? "abs" : mode == 1 ? "bitCount" : "numberOfTrailingZeros", "(I)I"));
        int operand = mode == 0 ? Opcodes.IXOR : mode == 1 ? Opcodes.IADD : Opcodes.ISUB;
        insnList.add(new InsnNode(operand));
        return new IntData(z, y, x, operand);
    }

    private IntData pushRandomInsn(InsnList insnList) {
        int mode = threadLocalRandom.nextInt(3);
        int x = pushInt(insnList, threadLocalRandom.nextInt(25565));
        int y = pushInt(insnList, threadLocalRandom.nextInt(25565));
        int z = pushInt(insnList, threadLocalRandom.nextInt(25565));
        int operand = mode == 0 ? Opcodes.IXOR : mode == 1 ? Opcodes.IADD : Opcodes.ISUB;
        insnList.add(new InsnNode(operand));
        return new IntData(z, y, x, operand);
    }

    private int pushInt(InsnList insnList, int i) {
        if(i >= -128 && i <= 127)
            insnList.add(new IntInsnNode(Opcodes.BIPUSH, i));
        else if(i >= -32768 && i <= 32767)
            insnList.add(new IntInsnNode(Opcodes.SIPUSH, i));
        else
            insnList.add(new LdcInsnNode(i));
        return i;
    }

    public static class IntData {

        private int firstValue;
        private int secondValue;
        private int result;
        private int operand;

        public IntData(int firstValue, int secondValue, int result, int operand) {
            this.firstValue = firstValue;
            this.secondValue = secondValue;
            this.result = result;
            this.operand = operand;
        }

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }

        public int getFirstValue() {
            return firstValue;
        }

        public void setFirstValue(int firstValue) {
            this.firstValue = firstValue;
        }

        public int getSecondValue() {
            return secondValue;
        }

        public void setSecondValue(int secondValue) {
            this.secondValue = secondValue;
        }

        public int getOperand() {
            return operand;
        }

        public void setOperand(int operand) {
            this.operand = operand;
        }
    }
}
