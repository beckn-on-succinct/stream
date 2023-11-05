package in.humbhionline.certbot;

import com.venky.core.io.ByteArrayInputStream;
import com.venky.core.string.StringUtil;
import com.venky.core.util.ObjectUtil;
import in.humbhionline.certbot.Request.HttpMethod;
import in.succinct.json.JSONObjectWrapper;
import in.succinct.json.ObjectWrappers;
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

                String dataOnThewire = StringUtil.read(is, true);

                String mainAttribute = "data";
                String otherAttribute = "error";

                if (r.getStatus() < 200 || r.getStatus() >= 300){
                    mainAttribute  = "error";
                    otherAttribute = "data";
                }

                JSONArray content_type = r.getHeaders().get("CONTENT-TYPE");
                if (content_type != null && ((String)content_type.get(0)).contains("application/json")) {
                    r.set(mainAttribute, (JSONAware) JSONValue.parseWithException(dataOnThewire));
                }else {
                    r.set(mainAttribute,dataOnThewire);
                }
                r.set(otherAttribute, new JSONObject());

                setResponse(r);
            }


            testCase.getVariables().set(getName(),getExportedVariables());
            Logger.getInstance().logPayloads(testCase,this);

            if (getAssertion() != null) {
                finalizeAttribute("assertion", testCase);
                getAssertion().assertTrue(testCase);
            }

        }catch (Exception ex){
            throw  new RuntimeException(ex);
        }


    }
    public Logs getLogs(){
        return get(Logs.class, "logs");
    }
    public void setLogs(Logs logs){
        set("logs",logs);
    }

    public static class Logs extends ObjectWrappers<Log> {

    }
    public static class Log extends JSONObjectWrapper {
        public String getName(){
            return get("name");
        }

        public void setName(String name){
            set("name",name);
        }

        public String getValue(){
            return get("value");
        }
        public void setValue(String value){
            set("value",value);
        }
    }
    @SuppressWarnings("unchecked")
    public void finalizeAttribute(String attributeName, TestCase testCase){
        JSONAware o = get(attributeName);
        Object script = get(attributeName +"_finalizer");
        StringBuilder attributeFinalizer = new StringBuilder();
        if (script != null) {
            if (script instanceof String) {
                attributeFinalizer.append(String.format("\n\t%s", script));
            } else if (script instanceof JSONArray) {
                ((JSONArray) script).forEach(v -> {
                    attributeFinalizer.append(String.format("\n\t%s", v));
                });
            }
        }



        String builder = String.format("\n\t%s = %s", attributeName, o.toString()) +
                String.format("\n\t %s",attributeFinalizer) +
                String.format("\n\treturn JSON.stringify(%s); ", attributeName);

        String s = (String)JavaScriptEvaluator.getInstance().eval(testCase,builder);

        set(attributeName, (JSONAware) parse(s));
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
