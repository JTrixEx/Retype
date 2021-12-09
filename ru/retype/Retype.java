package ru.retype;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;
import ru.retype.processor.IProcessor;
import ru.retype.processor.crasher.DecompilerCrasherProcessor;
import ru.retype.processor.fake.FakeAnnotationsProcessor;
import ru.retype.processor.fake.FakeAttributesProcessor;
import ru.retype.processor.fake.FakeFieldsProcessor;
import ru.retype.processor.fake.FakeMethodProcessor;
import ru.retype.processor.flow.LightFlowProcessor;
import ru.retype.processor.name.ClassNameProcessor;
import ru.retype.processor.name.FieldNameProcessor;
import ru.retype.processor.name.MethodNameProcessor;
import ru.retype.processor.name.RemapProcessor;
import ru.retype.processor.string.StringHideProcessor;
import ru.retype.processor.string.XorStringProcessor;
import ru.retype.processor.utils.*;
import ru.retype.utils.Logger;

import java.awt.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Retype {

    private static Retype instance;
    private File obfuscattionFile;

    private List<IProcessor> processors = new ArrayList<>();
    private List<ClassNode> classNodes = new ArrayList<>();

    public Retype(){
        instance = this;

        processors.add(new FakeAnnotationsProcessor());
        processors.add(new FakeAttributesProcessor());
        processors.add(new FakeFieldsProcessor());
        processors.add(new FakeMethodProcessor());

        processors.add(new DecompilerCrasherProcessor());

        processors.add(new XorStringProcessor());
        processors.add(new StringHideProcessor());

        processors.add(new LightFlowProcessor());

        processors.add(new LineNumberProcessor());
        processors.add(new LocalVariableProcessor());
        processors.add(new MemberShufflerProcessor());
        processors.add(new HideProcessor());

        processors.add(new HideCallsProcessor());

        processors.add(new ClassNameProcessor());
        processors.add(new FieldNameProcessor());
        processors.add(new MethodNameProcessor());
        processors.add(new RemapProcessor());
    }

    public void launch(String[] args) {
        if(args == null || args.length == 0) {
            obfuscattionFile = new File("E:\\Dev\\RetypeTests\\main.jar"); // for tests
        } else {
            obfuscattionFile = new File(args[0]);
            if(!obfuscattionFile.exists()) {
                Logger.log("Input file doesn\'t exists!");
                return;
            }
        }

        loadFile(obfuscattionFile);
        startObfuscation();
        saveFile(obfuscattionFile);
    }

    private void startObfuscation(){
        ObfuscationContext obfuscationContext = new ObfuscationContext(this.classNodes);

        processors.forEach(processor -> {
            processor.obfuscate(obfuscationContext);
        });
    }

    public void swapClassNode(ClassNode src, ClassNode dest) {
        int i = classNodes.indexOf(src);
        if(i >= 0){
            classNodes.set(i, dest);
        }
    }

    public void saveFile(File f) {
        try {
            File obfPath = new File(f.getParent(), f.getName().replace(".jar", "_obf.jar"));
            if(obfPath.exists())
                obfPath.delete();
            obfPath.createNewFile();
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(obfPath));

            classNodes.forEach(classNode -> {
                try {
                    classNode.methods.forEach(methodNode -> {
                        methodNode.maxStack += 10;
                        methodNode.maxLocals += 10;
                    });
                    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                    classNode.accept(classWriter);

                    zipOutputStream.putNextEntry(new ZipEntry(classNode.name + ".class"));
                    zipOutputStream.write(classWriter.toByteArray());
                    zipOutputStream.closeEntry();
                } catch (Exception e){
                    e.printStackTrace();
                }
            });

            zipOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFile(File f) {
        try {
            ZipFile zipFile = new ZipFile(f);
            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();

            while(enumeration.hasMoreElements()) {
                ZipEntry entry = enumeration.nextElement();
                if (!entry.isDirectory()) {
                    if (entry.getName().endsWith(".class")) {
                        InputStream inputStream = zipFile.getInputStream(entry);

                        DataInputStream dataInputStream = new DataInputStream(inputStream);

                        byte[] classData = new byte[dataInputStream.available()];
                        for(int i = 0; i < classData.length; i++){
                            classData[i] = dataInputStream.readByte();
                        }

                        dataInputStream.close();
                        inputStream.close();

                        ClassReader classReader = new ClassReader(classData);
                        ClassNode classNode = new ClassNode();
                        classReader.accept(classNode, 0);

                        classNodes.add(classNode);
                    }
                }
            }

            zipFile.close();
            Logger.log(String.format("Loaded %d classes", classNodes.size()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Retype getInstance() {
        return instance;
    }
}
