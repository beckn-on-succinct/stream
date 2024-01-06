package in.humbhionline.certbot;

import com.venky.core.string.StringUtil;
import in.succinct.json.JSONObjectWrapper;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;


public class TestCase extends JSONObjectWrapper {
    private TestCase(JSONObject value) {
        super(value);
    }

    private TestCase(String payload) {
        super(payload);
    }
    private File file ;
    public TestCase(File file) throws FileNotFoundException {
        this(StringUtil.read(new FileReader(file), true));
        this.file = file;
    }
    public String getDirectory(){
        return file.getParent();
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
        execute(false);
    }
    public void execute(boolean throwOnException){
        try {
            Logger.getInstance().log(String.format("Test Case : %s Started", getName()));
            for (Step step : getSteps()) {
                try {
                    Logger.getInstance().log(String.format("\t Step : %s Started",step.getName()));
                    step.execute(this);
                    Logger.getInstance().log(String.format("\t Step : %s Successful",step.getName()));
                }catch (Exception ex){
                    Logger.getInstance().log(String.format("\t Step : %s Failed with %s",step.getName(),ex.getMessage()));
                    Logger.getInstance().log(ex);
                    throw ex;
                }
            }
            Logger.getInstance().log(String.format("Test Case : %s Passed", getName()));
        }catch (Exception ex) {
            Logger.getInstance().log(String.format("Test Case : %s Failed", getName()));
            if (throwOnException) {
                throw new RuntimeException(ex);
            }
        }
    }

}
