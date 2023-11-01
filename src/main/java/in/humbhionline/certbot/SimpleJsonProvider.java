package in.humbhionline.certbot;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.Defaults;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.AbstractJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.venky.core.string.StringUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.Set;

public class SimpleJsonProvider extends AbstractJsonProvider {
    public static void init(){
        Configuration.setDefaults(new Defaults() {

            final JsonProvider jsonProvider = new SimpleJsonProvider();
            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }

            @Override
            public MappingProvider mappingProvider() {
                return new MappingProvider() {
                    @Override
                    public <T> T map(Object source, Class<T> targetType, Configuration configuration) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public <T> T map(Object source, TypeRef<T> targetType, Configuration configuration) {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        });
    }
    @Override
    public Object parse(String json) throws InvalidJsonException {
        try {
            return JSONValue.parseWithException(json);
        } catch (ParseException e) {
            throw new InvalidJsonException(e);
        }
    }

    @Override
    public Object parse(InputStream jsonStream, String charset) throws InvalidJsonException {
        try {
            return JSONValue.parseWithException(new InputStreamReader(jsonStream,charset));
        } catch (Exception e) {
            throw new InvalidJsonException(e);
        }
    }

    @Override
    public String toJson(Object obj) {
        return StringUtil.valueOf(obj);
    }

    @Override
    public Object createArray() {
        return new JSONArray();
    }

    @Override
    public Object createMap() {
        return new JSONObject();
    }
}
