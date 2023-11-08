package in.humbhionline.certbot;

import in.succinct.json.JSONObjectWrapper;
import org.json.simple.JSONObject;

public class Request extends JSONObjectWrapper {
    public Request() {
    }

    public Request(JSONObject value) {
        super(value);
    }

    public Request(String payload) {
        super(payload);
    }


    public String getUrl(){
        return get("url");
    }
    public void setUrl(String url){
        set("url",url);
    }

    public HttpMethod getHttpMethod(){
        return getEnum(HttpMethod.class, "method");
    }
    public void setHttpMethod(HttpMethod http_method){
        setEnum("method",http_method);
    }

    public enum HttpMethod {
        get,
        post,
        put,
        delete,
    }


    public int getTimeout(){
        return getInteger("timeout",10);
    }
    public void setTimeout(int timeout){
        set("timeout",timeout);
    }

    public Headers getHeaders(){
        return get(Headers.class, "headers");
    }
    public void setHeaders(Headers headers){
        set("headers",headers);
    }

    public Object getBody(){
        return get("body");
    }

}
