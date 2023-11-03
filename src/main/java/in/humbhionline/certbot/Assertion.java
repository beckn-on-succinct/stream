package in.humbhionline.certbot;

import in.humbhionline.certbot.Assertions.And;
import in.humbhionline.certbot.Assertions.Or;
import in.succinct.json.JSONObjectWrapper;
import org.json.simple.JSONObject;

@SuppressWarnings("ALL")
public class Assertion extends JSONObjectWrapper implements Condition {

    public Assertion(){
        super();
    }
    public Assertion(JSONObject o){
        super(o);
    }

    public Assertion(String payload) {
        super(payload);
    }

    public And getAnd(){
        return get(And.class, "and");
    }

    public Or getOr(){
        return get(Or.class, "or");
    }

    public Script getScript(){
        return get(Script.class, "script");
    }

    public Condition getCondition(){
        Condition condition = null;

        for (Condition c : new Condition[]{getAnd(), getOr(), getScript()}){
            if (condition == null){
                condition = c;
            }else if (c != null){
                throw new RuntimeException("Multiple conditions to assert!");
            }
        }
        return condition;
    }

    @Override
    public boolean eval(TestCase testCase){
        return getCondition().eval(testCase);
    }

    public String toString(){
        return String.format(getInner().toString());
    }

}
