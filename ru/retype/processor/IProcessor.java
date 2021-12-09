package ru.retype.processor;

import ru.retype.ObfuscationContext;

public interface IProcessor {

    public void obfuscate(ObfuscationContext context);

    default String getName() {
        return this.getClass().getName();
    }
}
