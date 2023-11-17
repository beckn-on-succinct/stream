package in.humbhionline.certbot;

import com.venky.core.io.ByteArrayInputStream;
import com.venky.core.string.StringUtil;
import com.venky.core.util.ObjectUtil;
import in.humbhionline.certbot.Request.HttpMethod;
import in.succinct.json.JSONAwareWrapper;
import in.succinct.json.JSONObjectWrapper;
import in.succinct.json.ObjectWrappers;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.FileReader;
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

    public String getFlow(){
        return get("flow");
    }
    public void setFlow(String flow){
        set("flow",flow);
    }

    public void execute(TestCase testCase){
        String flow = getFlow();
        TestRunner.loadEnv(testCase.getVariables());
        if (flow != null) {
            finalizeAttribute("flow",testCase);
            executeSubFlow(testCase,getFlow());
        }else {
            executeRequest(testCase);
        }
    }

    private void executeSubFlow(TestCase testCase,String subFlowName) {
        try {
            TestCase subFlow = new TestCase(StringUtil.read(new FileReader(subFlowName)));
            Variables state = new Variables(testCase.getVariables().getInner());
            subFlow.setVariables(state);
            subFlow.execute();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("ALL")
    public void executeRequest(TestCase testCase) {
        StringBuilder fakeCurl = new StringBuilder("\ncurl " );
        finalizeAttribute("request",testCase);
        StringBuilder url = new StringBuilder();
        url.append(getRequest().getUrl());
        Builder builder = HttpRequest.newBuilder(URI.create(url.toString()));


        if (getRequest().getHeaders() == null) {
            getRequest().setHeaders(new Headers());
        }
        JSONObject o = getRequest().getHeaders().getInner();
        o.forEach((k, v) -> {
            fakeCurl.append(String.format(" -H '%s:%s'",StringUtil.valueOf(k), StringUtil.valueOf(v)));
            builder.header(StringUtil.valueOf(k), StringUtil.valueOf(v));
        });
        builder.timeout(Duration.ofSeconds(getRequest().getTimeout()));
        builder.setHeader("Accept-Encoding", "gzip");
        builder.setHeader("Accept", "application/json");
        fakeCurl.append(" -H 'Accept-Encoding:gzip' ");
        fakeCurl.append(" -H 'Accept:application/json' ");


        fakeCurl.append(" '").append(url).append("' ");
        if (getRequest().getHttpMethod() == HttpMethod.get) {
            builder.GET();
        }else {
            fakeCurl.append(" -d '").append(getRequest().getBody().toString()).append("'");
            builder.method(getRequest().getHttpMethod().toString().toUpperCase(),BodyPublishers.ofString(getRequest().getBody().toString()));
        }


        builder.version(Version.HTTP_2);
        fakeCurl.append(" --http2 ");

        HttpRequest request = builder.build();
        try {
            Logger.getInstance().log(fakeCurl.toString());
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
                if (content_type != null && ((String)content_type.get(0)).contains("application/json") && !ObjectUtil.isVoid(dataOnThewire)) {
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
        Object o = get(attributeName);
        Object script = get(attributeName +"_finalizer");
        if (o == null && script == null){
            return;
        }
        boolean isJson = false;
        if (o == null){
            try {
                Class<?> type = getClass().getMethod("get" + StringUtil.camelize(attributeName)).getReturnType();

                isJson = JSONAware.class.isAssignableFrom(type) || JSONAwareWrapper.class.isAssignableFrom(type);
            }catch (Exception ignored){
            }
        }else {
            isJson = o instanceof JSONAware;
        }

        StringBuilder attributeFinalizer = new StringBuilder();
        if (script != null) {
            if (script instanceof String) {
                attributeFinalizer.append(String.format("\n\t%s", script));
            } else if (script instanceof JSONArray) {
                ((JSONArray) script).forEach(v -> attributeFinalizer.append(String.format("\n\t%s", v)));
            }
        }

        StringBuilder builder = new StringBuilder();
        if (isJson) {
            builder.append(String.format("\n\t%s = %s", attributeName, o));
        }else {
            builder.append(String.format("\n\t%s = \"%s\"", attributeName, o));
        }
        builder.append(String.format("\n\t %s", attributeFinalizer));
        if (isJson) {
            builder.append(String.format("\n\treturn JSON.stringify(%s); ", attributeName));
        }else {
            builder.append(String.format("\n\treturn %s; ", attributeName));
        }

        String s = (String)JavaScriptEvaluator.getInstance().eval(testCase, builder.toString());

        if (isJson) {
            set(attributeName, (JSONAware) parse(s));
        }else {
            set(attributeName, s);
        }
    }

    public static class SubFlowException extends RuntimeException{
        public SubFlowException(String message) {
            super(message);
        }

        public SubFlowException(String message, Throwable cause) {
            super(message, cause);
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
