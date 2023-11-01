package in.humbhionline.certbot;

import in.succinct.json.JSONObjectWrapper;
import org.json.simple.JSONObject;

import java.util.Set;

@SuppressWarnings("ALL")
public class Assertion extends JSONObjectWrapper{


    public Assertion() {
    }

    public Assertion(JSONObject value) {
        super(value);
    }

    public Assertion(String payload) {
        super(payload);
    }

    public Assertable getAssertable(){
        Set keySet = getInner().keySet();
        if (keySet.size() != 1 ){
            throw new RuntimeException("Unknown type of assertion!");
        }
        String type = (String)keySet.iterator().next();

        return get(Assertable.getClass(type), type);
    }
    public void setAssertable(Assertable assertable){
        set(assertable.getType(assertable),assertable);
    }

    public boolean eval(){
        return getAssertable().eval();
    }

    public void assertTrue(){
        Assertable assertable = getAssertable();

        try {
            assertable.assertTrue();
            Logger.log(String.format("\t\t Asserted %s %s",Assertable.getType(assertable),assertable.toString()));
        }catch (Exception ex){
            Logger.log(String.format("\t\t Could not assert %s %s",Assertable.getType(assertable), assertable.toString()));
            throw ex;
        }
    }

}
