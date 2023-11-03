package in.humbhionline.certbot;

import in.succinct.json.JSONObjectWrapper;
import org.json.simple.JSONArray;

import java.util.List;
import java.util.Map;

public class Response extends JSONObjectWrapper {
    public int getStatus(){
        return getInteger("status");
    }
    public void setStatus(int status){
        set("status",status);
    }


    public Headers getHeaders(){
        return get(Headers.class, "headers");
    }
    public void setHeaders(Headers headers){
        set("headers",headers);
    }


    @SuppressWarnings("ALL")
    public void setHeaders(Map<String, List<String>> map) {
        Headers headers = getHeaders();
        map.forEach((k,v)->{
            JSONArray array = new JSONArray();
            array.addAll(v);
            headers.set(k, array);
        });
    }
}
