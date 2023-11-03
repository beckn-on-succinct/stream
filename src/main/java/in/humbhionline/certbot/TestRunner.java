package in.humbhionline.certbot;

import com.esotericsoftware.minlog.Log;
import com.venky.core.string.StringUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class TestRunner {

    public static void main(String[] args) throws ParseException, IOException {
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


        for (File t : tests){
            TestCase testCase = new TestCase(StringUtil.read(new FileReader(t)));
            testCase.setVariables(new Variables(initialVariables));
            executeTest(testCase);
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
            Logger.getInstance().log(ex);
        }
    }

}
