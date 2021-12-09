package ru.retype.processor.name;

import jdk.internal.org.objectweb.asm.commons.SimpleRemapper;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import ru.retype.ObfuscationContext;
import ru.retype.Retype;
import ru.retype.processor.IProcessor;
import ru.retype.processor.name.remapper.ClassRemapper;
import ru.retype.processor.name.remapper.Remapper;

public class RemapProcessor implements IProcessor {

    @Override
    public void obfuscate(ObfuscationContext context) {
        SimpleRemapper simpleRemapper = new Remapper(context.getMappings());
        context.getClassNodes().forEach(classNode -> {
            ClassNode renamedClassNode = new ClassNode();
            ClassRemapper classAdapter = new ClassRemapper(renamedClassNode, simpleRemapper);
            classNode.accept(classAdapter);
            Retype.getInstance().swapClassNode(classNode, renamedClassNode);
        });
    }
}
