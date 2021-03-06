package ru.retype.processor.fake;

import jdk.internal.org.objectweb.asm.Attribute;
import ru.retype.ObfuscationContext;
import ru.retype.processor.IProcessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class FakeAttributesProcessor implements IProcessor {

    @Override
    public void obfuscate(ObfuscationContext context) {
        if(!context.isFakeAttributesEnabled())
            return;

        try {
            Attribute runtimeInvisibleParameterAnnotationsAttribute = newAttribute("RuntimeInvisibleParameterAnnotations", getRandomBytes());
            Attribute methodParametersAttributes = newAttribute("MethodParameters", getRandomBytes());
            Attribute enclosingMethodAttribute = newAttribute("EnclosingMethod", getRandomBytes());
            Attribute exceptionAttributes = newAttribute("Exceptions", getRandomBytes());
            Attribute localVarTypeTableAttribute = newAttribute("LocalVariableTypeTable", getRandomBytes());
            Attribute localVarTableAttribute = newAttribute("LocalVariableTable", getRandomBytes());
            Attribute constantValueAttribyte = newAttribute("ConstantValue", getRandomBytes());
            Attribute stackMapAttribute = newAttribute("StackMap", getRandomBytes());
            Attribute moduleMainClassAttribute = newAttribute("ModuleMainClass", getRandomBytes());

            byte[] nestHostBytes = getRandomBytes();
            nestHostBytes[1] = 6;
            Attribute nestHostAttribute = newAttribute("NestHost", nestHostBytes);

            Attribute codeAttribute = newAttribute("Code", getRandomBytes());

            context.getClassNodes().forEach(classNode -> {
                if(classNode.attrs == null)
                    classNode.attrs = new ArrayList<Attribute>();

                classNode.attrs.add(moduleMainClassAttribute);
                classNode.attrs.add(nestHostAttribute);
                classNode.attrs.add(exceptionAttributes);
                classNode.attrs.add(localVarTypeTableAttribute);
                classNode.attrs.add(localVarTableAttribute);

                classNode.methods.forEach(methodNode -> {
                    if(methodNode.attrs == null)
                        methodNode.attrs = new ArrayList<Attribute>();

                    methodNode.attrs.add(constantValueAttribyte);
                    methodNode.attrs.add(stackMapAttribute);
                    methodNode.attrs.add(localVarTypeTableAttribute);
                });

                classNode.fields.forEach(fieldNode -> {
                    if(fieldNode.attrs == null)
                        fieldNode.attrs = new ArrayList<Attribute>();
                    fieldNode.attrs.add(codeAttribute);
                    fieldNode.attrs.add(enclosingMethodAttribute);
                    fieldNode.attrs.add(methodParametersAttributes);
                    fieldNode.attrs.add(runtimeInvisibleParameterAnnotationsAttribute);
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Attribute newAttribute(String name, byte[] attributeData) throws Exception{
        Class<Attribute> attributeClass = Attribute.class;
        Constructor<Attribute> attributeConstructor = attributeClass.getDeclaredConstructor(String.class);
        attributeConstructor.setAccessible(true);
        Attribute attribute = attributeConstructor.newInstance(name);
        Field valueField = attributeClass.getDeclaredField("value");
        valueField.setAccessible(true);
        valueField.set(attribute, attributeData);
        return attribute;
    }

    private byte[] getRandomBytes(){
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        byte[] value = new byte[2 + threadLocalRandom.nextInt(4)];
        threadLocalRandom.nextBytes(value);
        return value;
    }
}
