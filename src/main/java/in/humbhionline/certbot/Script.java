package in.humbhionline.certbot;

import com.venky.core.string.StringUtil;
import in.succinct.json.JSONObjectWrapper;
import org.json.simple.JSONObject;

public class Script extends JSONObjectWrapper implements Condition {
    public Script(){
        super(new JSONObject());
    }

    @Override
    public boolean eval(TestCase testCase) {
        String builder = String.format(" return %s ;", getEval());
        return JavaScriptEvaluator.getInstance().evalBoolean(testCase, builder);
    }

    public String getEval(){
        return get("eval");
    }
    public void setEval(String eval){
        set("eval",eval);
    }

    public String getMessage(){
        return get("message");
    }
    public void setMessage(String message){
        set("message",message);
    }


    @Override
    public String toString() {
        return Condition.toString(this);
    }
}