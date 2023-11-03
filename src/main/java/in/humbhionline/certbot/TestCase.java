package in.humbhionline.certbot;

import in.succinct.json.JSONObjectWrapper;
import org.json.simple.JSONObject;



public class TestCase extends JSONObjectWrapper {
    public TestCase(JSONObject value) {
        super(value);
    }

    public TestCase(String payload) {
        super(payload);
    }

    public String getName(){
        return get("name");
    }
    public void setName(String name){
        set("name",name);
    }
    
    public Variables getVariables(){
        return get(Variables.class, "variables");
    }
    public void setVariables(Variables variables){
        set("variables",variables);
    }


    public Steps getSteps(){
        return get(Steps.class, "steps");
    }
    public void setSteps(Steps steps){
        set("steps",steps);
    }


    public void execute(){
        try {
            Logger.getInstance().log(String.format("Test Case : %s Started", getName()));
            for (Step step : getSteps()) {
                try {
                    Logger.getInstance().log(String.format("\t Step : %s Started",step.getName()));
                    step.execute(this);
                    Logger.getInstance().log(String.format("\t Step : %s Successful",step.getName()));
                }catch (Exception ex){
                    Logger.getInstance().log(String.format("\t Step : %s Failed",step.getName()));
                    throw ex;
                }
            }
            Logger.getInstance().log(String.format("Test Case : %s Passed", getName()));
        }catch (Exception ex) {
            Logger.getInstance().log(String.format("Test Case : %s Failed", getName()));
            throw ex;
        }
    }

}
