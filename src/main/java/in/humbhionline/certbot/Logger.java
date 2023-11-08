package in.humbhionline.certbot;

import com.venky.core.util.ObjectUtil;
import in.humbhionline.certbot.Step.Log;
import in.humbhionline.certbot.Step.Logs;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {
    private static volatile Logger sSoleInstance;

    //private constructor.
    private Logger() {
        //Prevent form the reflection api.
        if (sSoleInstance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static Logger getInstance() {
        if (sSoleInstance == null) { //if there is no instance available... create new one
            synchronized (Logger.class) {
                if (sSoleInstance == null) sSoleInstance = new Logger();
            }
        }

        return sSoleInstance;
    }

    //Make singleton from serialize and deserialize operation.
    protected Logger readResolve() {
        return getInstance();
    }

    public void logPayloads(TestCase testCase, Step step){
        if (getLogDirectory() == null){
            return;
        }
        if (step.getLogs() == null){
            step.setLogs(new Logs());
        }
        String dir = testCase.getName();
        File subDirectory = new File(getLogDirectory(),dir);
        subDirectory.mkdirs();

        step.finalizeAttribute("logs",testCase);


        for (Log log : step.getLogs()){
            String file = log.getName();
            String content = log.getValue();
            File logFile = new File(subDirectory,file);
            try (FileWriter os = new FileWriter(logFile); BufferedWriter bw = new BufferedWriter(os); ){
                bw.write(content);
                bw.newLine();
            }catch (IOException ex){
                throw new RuntimeException(ex);
            }
        }


    }
    public void log(String message){
        System.out.println(message);
    }

    public void log(Exception ex) {
        ex.printStackTrace(System.out);
    }

    File logDirectory = null;
    public File getLogDirectory(){
        return logDirectory;
    }
    public void setLogDirectory(String dir){
        if (ObjectUtil.isVoid(dir)){
            return;
        }
        logDirectory = new File(dir);
        if (logDirectory.exists()){
            throw new RuntimeException("Directory/File already exists");
        }else {
            logDirectory.mkdirs();
        }
    }
}
