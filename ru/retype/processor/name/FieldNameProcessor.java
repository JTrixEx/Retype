package ru.retype.processor.name;

import ru.retype.ObfuscationContext;
import ru.retype.processor.IProcessor;
import ru.retype.script.Scripting;
import ru.retype.utils.Logger;
import ru.retype.utils.NameUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FieldNameProcessor implements IProcessor {

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isFieldRenameEnabled())
            return;

        AtomicInteger atomicInteger = new AtomicInteger(0);
        Map<String, String> map = context.getMappings();
        context.getClassNodes().forEach(classNode -> {
            classNode.fields.forEach(fieldNode -> {
                String fieldName = fieldNode.name;
                if(!Scripting.callObjectScript("excludeFieldName", boolean.class, new Object[] {fieldName})) {
                    String newName = NameUtils.generateString(atomicInteger.incrementAndGet());
                    Logger.log(String.format("Field %s renamed to %s", fieldName, newName));
                    map.put(classNode.name + '.' + fieldName + '.' + fieldNode.desc, newName);
                }
            });
        });
    }
}
