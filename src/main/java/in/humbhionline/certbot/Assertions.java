package in.humbhionline.certbot;

import in.succinct.json.ObjectWrappers;
import org.json.simple.JSONArray;

public class Assertions extends ObjectWrappers<Assertion> {
    public Assertions() {
    }

    public Assertions(JSONArray value) {
        super(value);
    }

    public Assertions(String payload) {
        super(payload);
    }
    public static class And extends Assertions implements Condition {
        public And(){
            super(new JSONArray());
        }

        @Override
        public boolean eval(TestCase testCase) {
            for (Assertion assertion : this){
                if (!assertion.eval(testCase)){
                    return false;
                }
            }
            return true;
        }
        @Override
        public void assertTrue(TestCase testCase) {
            for (Assertion assertion : this){
                assertion.assertTrue(testCase);
            }
        }

        @Override
        public JSONArray getInner() {
            return super.getInner();
        }
    }

    public static class Or extends Assertions implements Condition {

        @Override
        public boolean eval(TestCase testCase) {
            for (Assertion assertion : this){
                if (assertion.eval(testCase)){
                    return true;
                }
            }
            return false;
        }

    }
}
