package in.humbhionline.certbot;

import com.jayway.jsonpath.JsonPath;
import com.venky.core.io.ByteArrayInputStream;
import com.venky.core.string.StringUtil;
import com.venky.core.util.ObjectUtil;
import in.humbhionline.certbot.Request.HttpMethod;
import in.succinct.json.JSONObjectWrapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class Step extends JSONObjectWrapper {

    public Request getRequest(){
        return get(Request.class, "request");
    }
    public void setRequest(Request request){
        set("request",request);
    }

    static HttpClient client = null;
    private HttpClient getHttpClient(){
        if (client == null ){
            client = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();
        }
        return client;
    }

    @SuppressWarnings("ALL")
    public void execute(TestCase testCase) {
        finalizeAttribute("request",testCase);
        StringBuilder url = new StringBuilder();
        url.append(getRequest().getUrl());
        if (!ObjectUtil.isVoid(getRequest().getApi())){
            url.append(getRequest().getApi());
        }
        Builder builder = HttpRequest.newBuilder(URI.create(url.toString()));

        if (getRequest().getHeaders() == null) {
            getRequest().setHeaders(new Headers());
        }
        JSONObject o = getRequest().getHeaders().getInner();
        o.forEach((k, v) -> {
            builder.header(StringUtil.valueOf(k), StringUtil.valueOf(v));
        });
        builder.timeout(Duration.ofSeconds(getRequest().getTimeout()));
        if (getRequest().getHttpMethod() == HttpMethod.post) {
            builder.POST(BodyPublishers.ofString(getRequest().getBody().toString()));
        }else {
            builder.GET();
        }
        builder.setHeader("Accept-Encoding", "gzip");
        builder.version(Version.HTTP_2);

        HttpRequest request = builder.build();
        try {
            Response r = getResponse();
            if (r == null){
                HttpResponse<InputStream> response = getHttpClient().send(request, BodyHandlers.ofInputStream());
                r = new Response();
                r.setStatus(response.statusCode());
                r.setHeaders(new Headers());
                r.setHeaders(response.headers().map());

                InputStream is = null;
                if (response.headers().firstValue("Content-Encoding").isPresent()){
                    is = new GZIPInputStream(response.body());
                }else {
                    is = response.body();
                }
                is = new ByteArrayInputStream(StringUtil.readBytes(is,true));

                if (r.getStatus() >= 200 && r.getStatus() < 299){
                    r.set("data", (JSONAware) JSONValue.parseWithException(StringUtil.read(is,true)));
                    r.set("error", new JSONObject());
                }else {
                    r.set("error",(JSONAware) JSONValue.parseWithException(StringUtil.read(is,true)));
                    r.set("data",new JSONObject());
                }
                setResponse(r);
            }


            testCase.getVariables().set(getName(),getExportedVariables());
            finalizeAttribute("assertion",testCase);
            getAssertion().assertTrue();
        }catch (Exception ex){
            throw  new RuntimeException(ex);
        }


    }
    public void finalizeAttribute(String attributeName, TestCase testCase){
        Variables variables = testCase.getVariables();
        JSONObject o = get(attributeName);
        resolve(variables, o);
        set(attributeName, o);
    }



    @SuppressWarnings("ALL")
    private void resolve(Variables variables, JSONAware jsonAware) {
        if (jsonAware instanceof JSONObject jsonObject) {
            Set<Object> keys = new HashSet<Object>(jsonObject.keySet());
            for (Object key : keys) {
                String k  = (String)key;
                Object v  = jsonObject.get(k);
                boolean isKeyTransformationRequired =  k.charAt(0) == '$' ;
                boolean isValueTransformationRequired = false;

                k = isKeyTransformationRequired ? JsonPath.read(variables.getInner(), k) : k;

                if (v instanceof JSONAware){
                    resolve(variables,(JSONAware)v);
                }else if (v instanceof String v1) {
                    isValueTransformationRequired = v1.charAt(0) == '$';
                    v = isValueTransformationRequired ? JsonPath.read(variables.getInner(), v1) : v1;
                }
                if (isKeyTransformationRequired || isValueTransformationRequired){
                    jsonObject.remove(key);
                    jsonObject.put(k,v);
                }
            }
        }else if (jsonAware instanceof JSONArray jsonArray){
            JSONArray transformedArray = new JSONArray();
            for (Object e : jsonArray){
                if (e instanceof JSONAware o){
                    resolve(variables,(JSONAware) e);
                    transformedArray.add(e);
                }else if (e instanceof String s){
                    boolean isElementTransformationRequired = s.charAt(0) == '$' ;
                    s = isElementTransformationRequired ? JsonPath.read(variables.getInner(),s) : s ;
                    transformedArray.add(s);
                }else {
                    transformedArray.add(e);
                }
            }
            jsonArray.clear();
            jsonArray.addAll(transformedArray);
        }

    }



    @SuppressWarnings("ALL")
    public JSONObject getExportedVariables(){
        JSONObject exported = new JSONObject();
        exported.put("response",getResponse().getInner());
        exported.put("request",getRequest().getInner());
        return exported;
    }


    public Response getResponse(){
        return get(Response.class, "response");
    }
    public void setResponse(Response response){
        set("response",response);
    }
    public String getName(){
        return get("name");
    }
    public void setName(String name){
        set("name",name);
    }


    public Assertion getAssertion(){
        return get(Assertion.class, "assertion");
    }
    public void setAssertion(Assertion assertion){
        set("assertion",assertion);
    }




}
