package in.humbhionline.certbot;

import in.humbhionline.certbot.TestRunner.Env;
import in.succinct.json.JSONObjectWrapper;
import org.json.simple.JSONObject;

public class Variables extends JSONObjectWrapper {

    public Variables(JSONObject value) {
        super(value);
    }

    public Variables(String payload) {
        super(payload);
    }


    public Env getEnv(){
        return get(Env.class, "env");
    }
    public void setEnv(Env env){
        set("env",env);
    }

}
