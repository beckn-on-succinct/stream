package in.humbhionline.certbot;

import com.venky.core.string.StringUtil;
import in.succinct.json.JSONObjectWrapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.Locale;

public class Script extends JSONObjectWrapper implements Condition {
    public Script(){
        super(new JSONObject());
    }

    private void append(StringBuilder builder, String possiblyReturnStatement){
        if (possiblyReturnStatement.trim().matches("return[ (]+.*")){
            builder.append(possiblyReturnStatement);
        }else {
            builder.append(String.format(" return %s;",possiblyReturnStatement));
        }
    }
    @Override
    @SuppressWarnings("ALL")
    public boolean eval(TestCase testCase) {
        Object eval = getEval();
        StringBuilder builder = new StringBuilder();
        if (eval instanceof String) {
            append(builder, String.valueOf(eval));
        }else {
            JSONArray array = (JSONArray) eval;
            for (Iterator<Object> i = array.iterator();i.hasNext();){
                Object o = i.next();
                builder.append("\n\t");
                if (i.hasNext()){
                    builder.append(o);
                }else {
                    append(builder,String.valueOf(o));
                }
            }
        }
        Object ret = JavaScriptEvaluator.getInstance().eval(testCase,builder.toString());
        return Boolean.parseBoolean(StringUtil.valueOf(ret));
    }

    public Object getEval(){
        return get("eval");
    }

    public String getMessage(){
        return get("message");
    }


    @Override
    public String toString() {
        return Condition.toString(this);
    }
}