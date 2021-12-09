package ru.retype;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import ru.retype.script.Scripting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObfuscationContext {

    private List<ClassNode> classNodes = new ArrayList<>();
    private boolean classRenameEnabled = false;
    private boolean fieldRenameEnabled = false;
    private boolean methodRenameEnabled = false;
    private boolean lineNumberRemoveEnabled = false;
    private boolean isLocalVariableRenameEnabled = false;
    private boolean isFakeAnnotationsEnabled = false;
    private boolean isFakeAttributesEnabled = false;
    private boolean isDecompilerCrasherEnabled = false;
    private boolean isXorStringEnabled = false;
    private boolean isStringHideEnabled = false;
    private boolean isMemberShuffleEnabled = false;
    private boolean isMemberHideEnabled = false;
    private boolean isHideCallsEnabled = false;
    private boolean isFakeFieldsEnabled = false;
    private boolean isFakeMethodsEnabled = false;
    private boolean isLightFlowEnabled = false;

    private Map<String, String> mappings = new HashMap<>();

    public ObfuscationContext(List<ClassNode> classNodes){
        this.classNodes = classNodes;

        classRenameEnabled = Scripting.callObjectScript("isClassRemapEnabled", boolean.class, new Object[0]);
        fieldRenameEnabled = Scripting.callObjectScript("isFieldRemapEnabled", boolean.class, new Object[0]);
        methodRenameEnabled = Scripting.callObjectScript("isMethodRemapEnabled", boolean.class, new Object[0]);
        lineNumberRemoveEnabled = Scripting.callObjectScript("isLineNumberRemoveEnabled", boolean.class, new Object[0]);
        isLocalVariableRenameEnabled = Scripting.callObjectScript("isLocalVariableRenameEnabled", boolean.class, new Object[0]);
        isFakeAnnotationsEnabled = Scripting.callObjectScript("isFakeAnnotationsEnabled", boolean.class, new Object[0]);
        isFakeAttributesEnabled = Scripting.callObjectScript("isFakeAttributesEnabled", boolean.class, new Object[0]);
        isDecompilerCrasherEnabled = Scripting.callObjectScript("isDecompilerCrasherEnabled", boolean.class, new Object[0]);
        isXorStringEnabled = Scripting.callObjectScript("isXorStringEnabled", boolean.class, new Object[0]);
        isStringHideEnabled = Scripting.callObjectScript("isStringHideEnabled", boolean.class, new Object[0]);
        isMemberShuffleEnabled = Scripting.callObjectScript("isMemberShuffleEnabled", boolean.class, new Object[0]);
        isMemberHideEnabled = Scripting.callObjectScript("isMemberHideEnabled", boolean.class, new Object[0]);
        isFakeFieldsEnabled = Scripting.callObjectScript("isFakeFieldsEnabled", boolean.class, new Object[0]);
        isFakeMethodsEnabled = Scripting.callObjectScript("isFakeMethodsEnabled", boolean.class, new Object[0]);
        isLightFlowEnabled = Scripting.callObjectScript("isLightFlowEnabled", boolean.class, new Object[0]);
        isHideCallsEnabled =  Scripting.callObjectScript("isHideCallsEnabled", boolean.class, new Object[0]);
    }

    public Map<String, String> getMappings() {
        return mappings;
    }

    public boolean isHideCallsEnabled() {
        return isHideCallsEnabled;
    }

    public boolean isStringHideEnabled() {
        return isStringHideEnabled;
    }

    public boolean isLightFlowEnabled() {
        return isLightFlowEnabled;
    }

    public boolean isFakeMethodsEnabled() {
        return isFakeMethodsEnabled;
    }

    public boolean isXorStringEnabled() {
        return isXorStringEnabled;
    }

    public boolean isMemberShuffleEnabled() {
        return isMemberShuffleEnabled;
    }

    public boolean isFakeFieldsEnabled() {
        return isFakeFieldsEnabled;
    }

    public boolean isMemberHideEnabled() {
        return isMemberHideEnabled;
    }

    public boolean isDecompilerCrasherEnabled() {
        return isDecompilerCrasherEnabled;
    }

    public boolean isFakeAttributesEnabled() {
        return isFakeAttributesEnabled;
    }

    public boolean isFakeAnnotationsEnabled() {
        return isFakeAnnotationsEnabled;
    }

    public boolean isLocalVariableRenameEnabled() {
        return isLocalVariableRenameEnabled;
    }

    public boolean isLineNumberRemoveEnabled() {
        return lineNumberRemoveEnabled;
    }

    public boolean isClassRenameEnabled() {
        return classRenameEnabled;
    }

    public boolean isFieldRenameEnabled() {
        return fieldRenameEnabled;
    }

    public boolean isMethodRenameEnabled() {
        return methodRenameEnabled;
    }

    public List<ClassNode> getClassNodes() {
        return classNodes;
    }
}
