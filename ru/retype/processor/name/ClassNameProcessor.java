package ru.retype.processor.name;

import jdk.internal.org.objectweb.asm.commons.Remapper;
import jdk.internal.org.objectweb.asm.commons.SimpleRemapper;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import ru.retype.ObfuscationContext;
import ru.retype.Retype;
import ru.retype.processor.IProcessor;
import ru.retype.processor.name.remapper.ClassRemapper;
import ru.retype.script.Scripting;
import ru.retype.utils.Logger;
import ru.retype.utils.NameUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassNameProcessor implements IProcessor {

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isClassRenameEnabled())
            return;
        Map<String, String> map = context.getMappings();
        context.getClassNodes().forEach(classNode -> {
            String className = classNode.name;
            if(!Scripting.callObjectScript("excludeClassName", boolean.class, new Object[] {className})) {
                boolean needPackage = Scripting.callObjectScript("needPackage", boolean.class, new Object[0]);
                String newName = needPackage ? NameUtils.generateString(atomicInteger.incrementAndGet()) + "/" + NameUtils.generateString(atomicInteger.incrementAndGet()) : NameUtils.generateString(atomicInteger.incrementAndGet());
                Logger.log(String.format("Class %s renamed to %s", className, newName));
                map.put(className, newName);
            }
        });
    }
}
