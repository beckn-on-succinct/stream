package in.humbhionline.certbot;

import in.succinct.json.JSONObjectWrapper;
import org.json.simple.JSONObject;

public class Script extends JSONObjectWrapper implements Condition {
    public Script(){
        super(new JSONObject());
    }

    @Override
    public boolean eval(TestCase testCase) {
        String builder = String.format(" return (%s);", getEval());
        return JavaScriptEvaluator.getInstance().evalBoolean(testCase, builder);
    }

    public String getEval(){
        return get("eval");
    }
    public void setEval(String eval){
        set("eval",eval);
    }

    @Override
    public String toString(){
        return String.format("{\"%s\": \"%s\" }",
                Condition.getType(this), getInner().toString());
    }

}