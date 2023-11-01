package in.humbhionline.certbot;

import com.venky.core.date.DateUtils;
import com.venky.core.math.DoubleUtils;
import com.venky.core.string.StringUtil;
import com.venky.core.util.ObjectUtil;
import in.succinct.json.JSONObjectWrapper;
import org.json.simple.JSONArray;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Assertable extends JSONObjectWrapper {
    public Assertable() {
    }

    private static final List<Class<? extends Assertable>> classes = Arrays.asList(And.class,Or.class,Eq.class,Lt.class,Le.class, Gt.class, Ge.class,In.class,Not.class,Blank.class, Matches.class);
    private static final Map<String,Class<? extends Assertable>> assertableMap = new HashMap<>(){{
        for (Class<? extends Assertable> c : classes) {
            put(c.getSimpleName().toLowerCase(), c);
        }
    }};
    public static Class<? extends Assertable> getClass(String type){
        return assertableMap.get(type);
    }

    public String getMessage(){
        String message = get("message");
        return  message == null ? String.format("Assertion %s failed!",
                getClass().getSimpleName().toLowerCase()) : message;
    }

    public String toString(Object o ){
        return StringUtil.valueOf(convert(o));
    }
    public Object convert(Object o){
        if (o == null){
            return null;
        }
        String c = get( "comparator");
        if (ObjectUtil.equals(c,"number")){
            if (o instanceof Number){
                return ((Number)o).doubleValue();
            }else {
                return Double.valueOf(StringUtil.valueOf(o));
            }
        }else if (ObjectUtil.equals(c,"date")){
            return DateUtils.getDate(StringUtil.valueOf(o));
        }else if (ObjectUtil.equals(c,"boolean")){
            if ( o  instanceof Boolean){
                return o;
            }else {
                String s = StringUtil.valueOf(o);
                return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("Y") || s.equalsIgnoreCase("1") || s.equalsIgnoreCase("YES");
            }
        }else{
            return StringUtil.valueOf(o);
        }
    }

    public Comparator<Object> getComparator(){
        String c = get( "comparator");

        return (o1, o2) -> {
            if (ObjectUtil.equals(c,"number")){
                return DoubleUtils.compareTo((Double)convert(o1),(Double)convert(o2));
            }else if (ObjectUtil.equals(c,"date")){
               return DateUtils.compareToMinutes((Date)convert(o1),(Date)convert(o2));
            }else if (ObjectUtil.equals(c,"boolean")){
                Boolean b1 = (Boolean) convert(o1);
                Boolean b2 = (Boolean) convert(o2);
                if (ObjectUtil.equals(b1,b2)){
                    return 0;
                }else if (b2){
                    return -1;
                }else {
                    return 1;
                }
            }else {
                return StringUtil.valueOf(o1).compareTo(StringUtil.valueOf(o2));
            }
        };

    }

    public abstract boolean eval();
    public abstract void assertTrue();

    public static String getType(Assertable assertable) {
        return assertable.getClass().getSimpleName().toLowerCase();
    }


    public static class And extends Assertable {

        public Assertions getAssertions(){
            return get(Assertions.class, "assertions");
        }

        @Override
        public boolean eval() {
            for (Assertion assertion : getAssertions()){
                if (!assertion.eval()){
                    return false;
                }
            }
            return true;
        }
        @Override
        public void assertTrue() {
            for (Assertion assertion : getAssertions()){
                assertion.assertTrue();
            }
        }

    }

    public static class Or extends Assertable {


        public Assertions getAssertions(){
            return get(Assertions.class, "assertions");
        }

        @Override
        public boolean eval() {
            for (Assertion assertion : getAssertions()){
                if (assertion.eval()){
                    return true;
                }
            }
            return false;
        }

        @Override
        public void assertTrue() {
            if (!eval()){
                throw new AssertionException(getMessage());
            }
        }
    }

    public static abstract class TwoVariableAssertable extends Assertable {
        public Object getLv(){
            return get("lv");
        }

        public Object getRv(){
            return get("rv");
        }
    }
    public static class Eq extends TwoVariableAssertable {

        @Override
        public boolean eval() {
            return getComparator().compare(getLv(),getRv()) == 0;
        }

        @Override
        public void assertTrue() {
            if (!eval()){
                throw  new RuntimeException(String.format( "%s ( %s != %s )" ,getMessage(), toString(getLv()), toString(getRv()) ));
            }
        }
    }
    public static class Lt extends TwoVariableAssertable {

        @Override
        public boolean eval() {
            return getComparator().compare(getLv(),getRv()) < 0;
        }

        @Override
        public void assertTrue() {
            if (!eval()){
                throw  new RuntimeException(String.format( "%s (%s >= %s) " ,getMessage(), toString(getLv()), toString(getRv()) ));
            }
        }
    }
    public static class Le extends TwoVariableAssertable {

        @Override
        public boolean eval() {
            return getComparator().compare(getLv(),getRv()) <= 0;
        }

        @Override
        public void assertTrue() {
            if (!eval()){
                throw  new RuntimeException(String.format( "%s (%s > %s) " ,getMessage(), toString(getLv()), toString(getRv()) ));
            }
        }
    }
    public static class Gt extends TwoVariableAssertable {

        @Override
        public boolean eval() {
            return getComparator().compare(getLv(),getRv()) > 0;
        }

        @Override
        public void assertTrue() {
            if (!eval()){
                throw  new RuntimeException(String.format( "%s (%s <= %s) " ,getMessage(), toString(getLv()), toString(getRv()) ));
            }
        }
    }
    public static class Ge extends TwoVariableAssertable {

        @Override
        public boolean eval() {
            return getComparator().compare(getLv(),getRv()) >= 0;
        }

        @Override
        public void assertTrue() {
            if (!eval()){
                throw  new RuntimeException(String.format( "%s (%s < %s) " ,getMessage(), toString(getLv()), toString(getRv()) ));
            }
        }
    }

    public static class Matches extends TwoVariableAssertable {

        @Override
        public boolean eval() {
            Pattern pattern = Pattern.compile((String)getRv());
            Matcher matcher = pattern.matcher((String)getLv());
            return matcher.matches();
        }

        @Override
        public void assertTrue() {
            if (!eval()){
                throw  new RuntimeException(String.format( "%s (%s does not match regexp %s) " ,getMessage(), toString(getLv()), toString(getRv()) ));
            }
        }
    }
    public static class In extends TwoVariableAssertable {

        @Override
        public boolean eval() {
            Object lv = convert(getLv());
            JSONArray arr = (JSONArray) getRv();
            for (Object o : arr) {
                if (ObjectUtil.equals(lv,convert(o))){
                    return true;
                }
            }
            return false;
        }

        @Override
        public void assertTrue() {
            if (!eval()){
                throw  new RuntimeException(String.format( "%s (%s not in %s) " ,getMessage(), toString(getLv()), StringUtil.valueOf(getRv()) ));
            }
        }
    }

    public static class Blank extends Assertable {
        public Object getV(){
            return get("v");
        }
        @Override
        public boolean eval() {
            return ObjectUtil.isVoid(getV());
        }

        @Override
        public void assertTrue() {
            if (!eval()){
                throw  new RuntimeException(String.format( "%s (%s is not blank) " ,getMessage(), toString(getV()) ));
            }
        }
    }

    public static class Not extends Assertable {
        public Assertion getAssertion(){
            return get(Assertion.class, "assertion");
        }

        @Override
        public boolean eval() {
            return !getAssertion().eval();
        }

        @Override
        public void assertTrue() {
            getAssertion().assertTrue();
        }
    }



}
