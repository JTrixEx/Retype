package ru.retype.utils;

import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;
import jdk.internal.org.objectweb.asm.tree.VarInsnNode;

import java.util.*;

public class StackAnalyzer {

    private InsnList insnList;
    private int stackVarID = -1;
    private Map<Integer, Integer> stackVars = new HashMap<>();

    public StackAnalyzer(InsnList insnList) {
        this.insnList = insnList;
        this.analyze();
    }

    private void analyze() {
        Arrays.stream(insnList.toArray()).forEach(insn -> {
            if(insn instanceof VarInsnNode) {
                VarInsnNode varInsnNode = (VarInsnNode)insn;
                stackVarID = varInsnNode.var;

                stackVars.put(varInsnNode.getOpcode(), varInsnNode.var);
            }
            if(insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsnNode = (MethodInsnNode)insn;
                Arrays.stream(insnList.toArray()).forEach(prevInsn -> {

                });
            }
        });
    }

    public Map<Integer, Integer> getStackVars() {
        return stackVars;
    }

    public int getStackVarID() {
        return stackVarID;
    }

    public static class MethodCallStack {

        private String owner;
        private String name;
        private String desc;
        private Map<Integer, Integer> stackVars = new HashMap<>();

        public String getOwner() {
            return owner;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }
    }
}
