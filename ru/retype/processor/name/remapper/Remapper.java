package ru.retype.processor.name.remapper;

import jdk.internal.org.objectweb.asm.commons.SimpleRemapper;

import java.util.Map;

public class Remapper extends SimpleRemapper {

    public Remapper(final Map<String, String> mappings) {
        super(mappings);
    }

    @Override
    public String mapFieldName(String owner, String name, String desc) {
        String remappedName = map(owner + '.' + name + '.' + desc);
        return (remappedName != null) ? remappedName : name;
    }
}

