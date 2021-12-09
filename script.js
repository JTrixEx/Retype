function excludeClassName(className) {
    return className.equals("ru/retype/Example")
}

function excludeFieldName(fieldName) {
    return fieldName.equals("exampleField")
}

function excludeMethodName(methodName, methodDesc) {
    return methodName.equals("exampleMethod") && methodDesc.equals("(Lexample;)V")
}

function needPackage() {
    return false
}

function getKeywords(){
    var keywords = ["public","class", "static", "private", "protected", "final", "double", "int", "float", "boolean", "try", "catch", "finally"]
    var arrayType = Java.type("java.lang.String[]")
    var res = Java.to(keywords, arrayType)
    return res
}

function isClassRemapEnabled() {
    return true
}

function isFieldRemapEnabled() {
    return true
}

function isMethodRemapEnabled() {
    return true
}

function isLineNumberRemoveEnabled() {
    return true
}

function isLocalVariableRenameEnabled() {
    return true
}

function isFakeAnnotationsEnabled() {
    return true
}

function isFakeAttributesEnabled() {
    return false
}

function isFakeFieldsEnabled() {
    return true
}

function isFakeMethodsEnabled() {
    return true
}

function isDecompilerCrasherEnabled() {
    return true
}

function isXorStringEnabled() {
    return true
}

function isStringHideEnabled() {
    return true
}

function isMemberShuffleEnabled() {
    return true
}

function isMemberHideEnabled() {
    return true
}

function isHideCallsEnabled() {
    return false
}

function isLightFlowEnabled() {
    return true
}