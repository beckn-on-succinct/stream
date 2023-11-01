package in.humbhionline.certbot;

import in.succinct.json.JSONObjectWrapper;
import org.json.simple.JSONObject;

public class Variables extends JSONObjectWrapper {

    public Variables(JSONObject value) {
        super(value);
    }

    public Variables(String payload) {
        super(payload);
    }


}
