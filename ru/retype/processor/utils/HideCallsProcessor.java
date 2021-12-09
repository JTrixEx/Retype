package ru.retype.processor.utils;

import ru.retype.ObfuscationContext;
import ru.retype.processor.IProcessor;
import ru.retype.utils.StackAnalyzer;

import java.util.Arrays;

public class HideCallsProcessor implements IProcessor {

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isHideCallsEnabled())
            return;
        context.getClassNodes().forEach(classNode -> {
            classNode.methods.forEach(methodNode -> {
                Arrays.stream(methodNode.instructions.toArray()).forEach(insn -> {
                    StackAnalyzer stackAnalyzer = new StackAnalyzer(methodNode.instructions);
                });
            });
        });
    }
}
