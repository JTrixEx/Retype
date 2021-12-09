package ru.retype.script;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;

public class Scripting {

    public static <T> T callObjectScript(String action, Class<T> returnType, Object[] args) {
        return (T)eval0(action, args);
    }

    public static void callVoidScript(String action, Object[] args) {
        eval0(action, args);
    }

    private static Object eval0(String action, Object[] args) {
        try {
            ScriptEngineManager factory = new ScriptEngineManager();
            ScriptEngine engine = factory.getEngineByName("nashorn");
            engine.eval(new FileReader(new File("script.js")));
            Invocable invocable = (Invocable) engine;
            return invocable.invokeFunction(action, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
