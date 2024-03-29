package in.humbhionline.certbot;

import com.venky.core.date.DateUtils;
import com.venky.core.string.StringUtil;
import in.succinct.json.JSONObjectWrapper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TimeZone;

public class TestRunner {

    public static void main(String[] args) throws IOException {
        Options options = getOptions();

        DefaultParser parser= new DefaultParser();
        CommandLine commandLine =null ;
        try {
            commandLine = parser.parse(options,args);
        }catch (Exception ex){
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(TestRunner.class.getName(),options);
            Runtime.getRuntime().exit(-1);
        }


        if (commandLine == null) throw new AssertionError( "Incorrect usage!");

        String logDir = commandLine.getOptionValue("d");
        Logger.getInstance().setLogDirectory(logDir);

        File g = new File(commandLine.getOptionValue("g"));
        File[] tests = Arrays.stream(commandLine.getOptionValues("t")).map(File::new).toList().toArray(new File[]{});
        String initialVariables = StringUtil.read(new FileReader(g));

        try {

            for (File t : tests){
                TestCase testCase = new TestCase(t);
                Variables variables = new Variables((JSONObject) JSONValue.parseWithException(initialVariables));
                loadEnv(variables);
                if (testCase.getVariables() != null){
                    throw new RuntimeException("variables must be passed via -g option only");
                }
                testCase.setVariables(variables);

                executeTest(testCase);
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }


    }

    public static void loadEnv(Variables variables) {
        Env env = variables.getEnv();
        if (env == null ) {
            env = new Env();
            variables.setEnv(env);
            for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
                Object k = entry.getKey();
                Object v = entry.getValue();
                env.set((String) k, (String) v);
            }
        }
        env.setToday( new Date());
        env.setNow(new Date());

    }
    public static class Env extends JSONObjectWrapper {
        public Date getNow(){
            return getTimestamp("now");
        }
        public void setNow(Date now){
            set("now",now,TIMESTAMP_FORMAT);
        }
        public Date getToday(){
            return getDate("today");
        }
        public void setToday(Date today){
            set("today",today, DATE_FORMAT);
        }


    }
    public static class DateUtil {
        public static String addMinutes(String date, int minutes, String tz, String dateFormat){
            SimpleDateFormat fmt = new SimpleDateFormat(dateFormat);
            fmt.setTimeZone(TimeZone.getTimeZone(tz));
            try {
                Date aDate = fmt.parse(date);
                return fmt.format(DateUtils.addMinutes(aDate,minutes));
            }catch (Exception ex){
                return null;
            }
        }
        public static String now(String tz,String dateFormat){
            SimpleDateFormat fmt = new SimpleDateFormat(dateFormat);
            fmt.setTimeZone(TimeZone.getTimeZone(tz));
            try {
                return fmt.format(new Date());
            }catch (Exception ex){
                return null;
            }
        }

    }

    private static Options getOptions() {
        Options options = new Options();

        Option g = new Option("g", "global", true, "Global context file");
        g.setRequired(true);
        g.setArgs(1);
        options.addOption(g);

        Option t = new Option("t","testcase",true, "One Testcase file per -t option");
        t.setRequired(true);
        t.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(t);

        Option d = new Option("d","log-directory",true, "Directory to log payloads");
        d.setRequired(false);
        d.setArgs(1);
        options.addOption(d);

        return options;
    }

    private static void executeTest(TestCase testCase) {
        try {
            testCase.execute();
        }catch (Exception ex){
            Logger.getInstance().log(ex.getMessage());
        }
    }

}
