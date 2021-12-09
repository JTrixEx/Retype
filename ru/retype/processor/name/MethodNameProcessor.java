package ru.retype.processor.name;

import ru.retype.ObfuscationContext;
import ru.retype.processor.IProcessor;
import ru.retype.script.Scripting;
import ru.retype.utils.Logger;
import ru.retype.utils.NameUtils;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MethodNameProcessor implements IProcessor {

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isMethodRenameEnabled())
            return;

        AtomicInteger atomicInteger = new AtomicInteger(0);
        Map<String, String> map = context.getMappings();
        context.getClassNodes().forEach(classNode -> {
            classNode.methods.forEach(methodNode -> {
                if(Modifier.isNative(methodNode.access))
                    return;
                String methodName = methodNode.name;
                String methodDesc = methodNode.desc;
                if(methodName.equals("<clinit>") || methodName.equals("<init>")
                || methodName.equals("main") || methodName.equals("premain"))
                    return;
                if(!Scripting.callObjectScript("excludeMethodName", boolean.class, new Object[] {methodName, methodDesc})) {
                    String newName = NameUtils.generateString(atomicInteger.incrementAndGet());
                    Logger.log(String.format("Method %s renamed to %s", methodName, newName));
                    map.put(classNode.name + '.' + methodName + methodDesc, newName);
                }
            });
        });
    }
}
