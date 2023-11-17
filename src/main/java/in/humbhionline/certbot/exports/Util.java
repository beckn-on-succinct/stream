package in.humbhionline.certbot.exports;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Util {
    public static String btoa(String b){
        return Base64.getEncoder().encodeToString(b.getBytes(StandardCharsets.UTF_8));
    }
    public static String atob(String b){
        return new String(Base64.getDecoder().decode(b));
    }

    public static void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //
        }
    }
}
