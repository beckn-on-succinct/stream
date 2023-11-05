package in.humbhionline.certbot;

import in.humbhionline.certbot.Assertions.And;
import in.humbhionline.certbot.Assertions.Or;
import org.json.simple.JSONAware;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Conditions evaluate to true of false.
 */
public interface Condition {
    boolean eval(TestCase testCase);
    default void assertTrue(TestCase testCase){
        if (!eval(testCase)){
            throw new AssertionException(toString());
        }
    }


    static String toString(Condition condition){
        return String.format("{\"%s\": \"%s\" }",Condition.getType(condition), condition.getInner().toString());
    }
    JSONAware getInner();

    Map<String,Class<? extends Condition>> CONDITION_MAP = new HashMap<>(){{
        for (Class<? extends Condition> c : Arrays.asList(And.class, Or.class, Script.class)) {
            put(c.getSimpleName().toLowerCase(), c);
        }
    }};
    static Class<? extends Condition> getClass(String type){
        return CONDITION_MAP.get(type);
    }
    static String getType(Condition condition) {
        return condition.getClass().getSimpleName().toLowerCase();
    }

}
