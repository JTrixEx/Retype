package ru.retype.processor.utils;

import ru.retype.ObfuscationContext;
import ru.retype.processor.IProcessor;

import java.util.Collections;

public class MemberShufflerProcessor implements IProcessor {

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isMemberShuffleEnabled())
            return;
        context.getClassNodes().forEach(classNode -> {
            Collections.shuffle(classNode.methods);
            Collections.shuffle(classNode.fields);
        });
    }
}
