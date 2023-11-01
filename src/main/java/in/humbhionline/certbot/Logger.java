package in.humbhionline.certbot;

import com.venky.core.util.ExceptionUtil;

public class Logger {

    public static void log(String message){
        System.out.println(message);
    }

    public static void log(Exception ex) {
        ex.printStackTrace(System.out);
    }
}
