package in.humbhionline.certbot;

import in.succinct.json.ObjectWrappers;
import org.json.simple.JSONArray;

public class Steps extends ObjectWrappers<Step> {
    public Steps() {
    }

    public Steps(JSONArray value) {
        super(value);
    }

    public Steps(String payload) {
        super(payload);
    }
}
