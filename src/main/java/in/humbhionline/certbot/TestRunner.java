package in.humbhionline.certbot;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.Defaults;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.venky.core.string.StringUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public class TestRunner {

    public static void main(String[] args) throws ParseException, IOException {
        SimpleJsonProvider.init();
        Options options = new Options();
        {
            Option g = new Option("g", "global", true, "Global variables");
            g.setRequired(true);
            options.addOption(g);

            Option t = new Option("t","testcase",true, "Test Case");
            t.setRequired(true);
            options.addOption(t);
        }

        DefaultParser parser= new DefaultParser();
        CommandLine commandLine = parser.parse(options,args);


        File g = new File(commandLine.getOptionValue("g"));
        File[] tests = Arrays.stream(commandLine.getOptionValues("t")).map(File::new).toList().toArray(new File[]{});
        String initialVariables = StringUtil.read(new FileReader(g));


        for (File t : tests){
            TestCase testCase = new TestCase(StringUtil.read(new FileReader(t)));
            testCase.setVariables(new Variables(initialVariables));
            executeTest(testCase);
        }

    }

    private static void executeTest(TestCase testCase) {
        try {
            testCase.execute();
        }catch (Exception ex){
            Logger.log(ex);
        }
    }

}
