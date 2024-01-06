package in.humbhionline.certbot;

import com.venky.core.string.StringUtil;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;

public class JavaScriptEvaluator {
    private static volatile JavaScriptEvaluator sSoleInstance;

    //private constructor.
    private JavaScriptEvaluator() {
        //Prevent form the reflection api.
        if (sSoleInstance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        engine = factory.getScriptEngine("-scripting");

    }

    public static JavaScriptEvaluator getInstance() {
        if (sSoleInstance == null) { //if there is no instance available... create new one
            synchronized (JavaScriptEvaluator.class) {
                if (sSoleInstance == null) sSoleInstance = new JavaScriptEvaluator();
            }
        }

        return sSoleInstance;
    }

    //Make singleton from serialize and deserialize operation.
    protected JavaScriptEvaluator readResolve() {
        return getInstance();
    }

    private final ScriptEngine engine ;


    public Object eval(TestCase testCase, String statements){
        String function = "function(){}";
        try {
            /*
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.putAll(testCase.getVariables().getInner());
            */
            StringBuilder init = new StringBuilder();
            JSONObject var = testCase.getVariables().getInner();

            for (Object k : var.keySet()){
                Object v = var.get(k);
                if (v instanceof String) {
                    init.append(String.format("%s = \"%s\" ; \n", k, v));
                }else {
                    init.append(String.format("%s = %s ; \n", k, v));
                }
            }
            function = "function evaluate() { \n" +
                    init +
                    statements +
                    "\n}";

            engine.eval(function);
            return ((Invocable)engine).invokeFunction("evaluate");
        }catch (Exception ex){
            Logger.getInstance().log("Failed Calling Function\n--" );
            Logger.getInstance().log(function);
            Logger.getInstance().log("\n--" );

            throw new AssertionException(ex);
        }
    }
}
