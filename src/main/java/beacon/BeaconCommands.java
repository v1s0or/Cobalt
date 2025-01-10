package beacon;

import common.CommandParser;
import common.CommonUtils;
import common.MudgeSanity;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BeaconCommands {
    public Map descriptions = new HashMap();

    public Map details = new HashMap();

    public BeaconCommands() {
        loadCommands();
        loadDetails();
    }

    public void register(String string1, String string2, String string3) {
        this.descriptions.put(string1, string2);
        this.details.put(string1, string3);
    }

    public String getCommandFile() {
        return "resources/bhelp.txt";
    }

    public String getDetailFile() {
        return "resources/bdetails.txt";
    }

    protected void loadCommands() {
        try {
            InputStream inputStream = CommonUtils.resource(getCommandFile());
            byte[] arrby = CommonUtils.readAll(inputStream);
            inputStream.close();
            String[] arrstring = CommonUtils.bString(arrby).split("\n");
            for (byte b = 0; b < arrstring.length; b++) {
                String[] strs1 = arrstring[b].split("\t+");
                if (strs1.length == 2) {
                    this.descriptions.put(strs1[0], strs1[1]);
                } else {
                    CommonUtils.print_error("bhelp, line: " + b + " '" + arrstring[b] + "'");
                }
            }
        } catch (IOException iOException) {
            MudgeSanity.logException("Load Commands", iOException, false);
        }
    }

    protected void loadDetails() {
        try {
            InputStream inputStream = CommonUtils.resource(getDetailFile());
            byte[] arrby = CommonUtils.readAll(inputStream);
            inputStream.close();
            String[] arrstring = CommonUtils.bString(arrby).split("\n");
            String str = null;
            StringBuffer stringBuffer = new StringBuffer();
            for (byte b = 0; b < arrstring.length; b++) {
                CommandParser commandParser = new CommandParser(arrstring[b]);
                if (commandParser.is("beacon>")) {
                    if (commandParser.verify("AZ")) {
                        if (str != null)
                            this.details.put(str, stringBuffer.toString().trim());
                        str = commandParser.popString();
                        stringBuffer = new StringBuffer();
                    }
                } else {
                    stringBuffer.append(arrstring[b] + "\n");
                }
            }
            this.details.put(str, stringBuffer.toString().trim());
        } catch (IOException iOException) {
            MudgeSanity.logException("Load Details", iOException, false);
        }
    }

    public List commands() {
        synchronized (this) {
            return new LinkedList(this.descriptions.keySet());
        }
    }

    public String getDetails(String string) {
        synchronized (this) {
            return this.details.get(string) + "";
        }
    }

    public String getDescription(String string) {
        synchronized (this) {
            return this.descriptions.get(string) + "";
        }
    }

    public boolean isHelpAvailable(String string) {
        synchronized (this) {
            return this.details.containsKey(string);
        }
    }
}
