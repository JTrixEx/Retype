package ru.retype.processor.name.remapper;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.commons.Remapper;

public class AnnotationRemapper extends AnnotationVisitor {

    protected final Remapper remapper;

    public AnnotationRemapper(final AnnotationVisitor av, final Remapper remapper) {
        this(Opcodes.ASM5, av, remapper);
    }

    protected AnnotationRemapper(final int api, final AnnotationVisitor av, final Remapper remapper) {
        super(api, av);
        this.remapper = remapper;
    }

    @Override
    public void visit(String name, Object value) {
        av.visit(name, remapper.mapValue(value));
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
        av.visitEnum(name, remapper.mapDesc(desc), value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
        AnnotationVisitor v = av.visitAnnotation(name, remapper.mapDesc(desc));
        return v == null ? null : (v == av ? this : new AnnotationRemapper(api, v, remapper));
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        AnnotationVisitor v = av.visitArray(name);
        return v == null ? null : (v == av ? this : new AnnotationRemapper(api, v, remapper));
    }
}
