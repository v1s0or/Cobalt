package common;

import java.io.File;
import java.util.HashSet;
import java.util.Stack;

public class CommandParser {

    protected StringStack parse;

    protected String command;

    protected Stack args = new Stack();

    protected String error = null;

    protected String text;

    protected boolean missing = false;

    public CommandParser(String string) {
        this.text = string;
        reset();
    }

    public boolean is(String string) {
        return this.command.equals(string);
    }

    public String getCommand() {
        return this.command;
    }

    public String getArguments() {
        return this.parse.toString();
    }

    public String error() {
        return this.command + " error: " + this.error;
    }

    public boolean isMissingArguments() {
        boolean bool = this.missing;
        if (this.missing)
            reset();
        return bool;
    }

    public void error(String string) {
        this.error = string;
    }

    public boolean empty() {
        return this.parse.isEmpty();
    }

    public boolean hasError() {
        return (this.error != null);
    }

    public boolean reset() {
        this.parse = new StringStack(this.text);
        this.command = this.parse.shift();
        this.error = null;
        this.args = new Stack();
        this.missing = false;
        return false;
    }

    public boolean verify(String string) {
        char[] arrc = string.toCharArray();
        for (int b = 0; b < arrc.length; b++) {
            if (this.parse.isEmpty()) {
                this.error = "not enough arguments";
                this.missing = true;
                return false;
            }
            if (arrc[b] == 'A') {
                this.args.push(this.parse.shift());
            } else if (arrc[b] == 'C') {
                String str = this.parse.shift();
                if (str.equals("dns") || str.equals("dns-txt") || str.equals("dns6")) {
                    this.args.push(str);
                } else {
                    this.error = "argument '" + str + "' is not 'dns', 'dns6', or 'dns-txt'";
                    return false;
                }
            } else if (arrc[b] == 'D') {
                String str = this.parse.shift();
                if (str.equals("icmp") || str.equals("arp") || str.equals("none")) {
                    this.args.push(str);
                } else {
                    this.error = "argument '" + str + "' is not 'arp', 'icmp', or 'none'";
                    return false;
                }
            } else if (arrc[b] == 'g') {
                String str = this.parse.shift();
                if (str.equals("query") || str.equals("queryv")) {
                    this.args.push(str);
                } else {
                    this.error = "argument '" + str + "' is not 'query or queryv'";
                    return false;
                }
            } else if (arrc[b] == 'H') {
                String str = this.parse.shift();
                if (str.length() == 65 && str.charAt(32) == ':')
                    str = str.substring(33);
                if (str.length() != 32) {
                    this.error = "argument '" + str + "' is not an NTLM hash";
                    return false;
                }
                this.args.push(str);
            } else if (arrc[b] == 'I') {
                String str = this.parse.shift();
                try {
                    int i = Integer.parseInt(str);
                    this.args.push(new Integer(i));
                } catch (Exception exception) {
                    this.error = "'" + str + "' is not a number";
                    return false;
                }
            } else if (arrc[b] == 'f') {
                File file = new File(this.parse.toString());
                if (!file.exists()) {
                    this.error = "'" + file.getAbsolutePath() + "' does not exist";
                    return false;
                }
                if (!file.canRead()) {
                    this.error = "'" + file.getAbsolutePath() + "' is not readable";
                    return false;
                }
                if (file.isDirectory()) {
                    this.error = "'" + file.getAbsolutePath() + "' is a directory";
                    return false;
                }
                this.args.push(file.getAbsolutePath());
            } else if (arrc[b] == 'F') {
                File file = new File(this.parse.toString());
                if (!file.exists()) {
                    this.error = "'" + file.getAbsolutePath() + "' does not exist";
                    return false;
                }
                if (!file.canRead()) {
                    this.error = "'" + file.getAbsolutePath() + "' is not readable";
                    return false;
                }
                if (file.isDirectory()) {
                    this.error = "'" + file.getAbsolutePath() + "' is a directory";
                    return false;
                }
                if (file.length() > 1048576L) {
                    this.error = "max upload size is 1MB";
                    return false;
                }
                this.args.push(file.getAbsolutePath());
            } else if (arrc[b] == 'L') {
                String str = this.parse.toString();
                if (!ListenerUtils.isListener(str)) {
                    this.error = "Listener '" + str + "' does not exist";
                    return false;
                }
                this.args.push(str);
            } else if (arrc[b] == 'p') {
                File file = new File(this.parse.shift());
                if (!file.exists()) {
                    this.error = "'" + file.getAbsolutePath() + "' does not exist";
                    return false;
                }
                if (!file.canRead()) {
                    this.error = "'" + file.getAbsolutePath() + "' is not readable";
                    return false;
                }
                if (file.isDirectory()) {
                    this.error = "'" + file.getAbsolutePath() + "' is a directory";
                    return false;
                }
                this.args.push(file.getAbsolutePath());
            } else if (arrc[b] == 'Q') {
                String str = this.parse.shift();
                if (str.equals("high") || str.equals("low")) {
                    this.args.push(str);
                } else {
                    this.error = "argument '" + str + "' is not 'high' or 'low'";
                    return false;
                }
            } else if (arrc[b] == 'R') {
                String str = this.parse.shift();
                PortFlipper portFlipper = new PortFlipper(str);
                portFlipper.parse();
                if (portFlipper.hasError()) {
                    this.error = portFlipper.getError();
                    return false;
                }
                this.args.push(str);
            } else if (arrc[b] == 'T') {
                String str = this.parse.shift();
                AddressList addressList = new AddressList(str);
                if (addressList.hasError()) {
                    this.error = addressList.getError();
                    return false;
                }
                this.args.push(str);
            } else if (arrc[b] == 'U') {
                String str = this.parse.shift();
                if (str.startsWith("\\\\")) {
                    this.args.push(str.substring(2));
                } else {
                    this.error = "argument '" + str + "' is not a \\\\target";
                    return false;
                }
            } else if (arrc[b] == 'V') {
                String str = this.parse.shift();
                HashSet hashSet = new HashSet(CommonUtils.getNetCommands());
                if (hashSet.contains(str)) {
                    this.args.push(str);
                } else {
                    this.error = "argument '" + str + "' is not a net command";
                    return false;
                }
            } else if (arrc[b] == 'X') {
                String str = this.parse.shift();
                if (str.equals("x86") || str.equals("x64")) {
                    this.args.push(str);
                } else {
                    this.error = "argument '" + str + "' is not 'x86' or 'x64'";
                    return false;
                }
            } else if (arrc[b] == 'Z') {
                this.args.push(this.parse.toString());
            } else if (arrc[b] == '%') {
                String str = this.parse.shift();
                try {
                    int i = Integer.parseInt(str);
                    if (i < 0 || i > 99) {
                        this.error = "argument " + i + " is not a value 0-99";
                        return false;
                    }
                    this.args.push(new Integer(i));
                } catch (Exception exception) {
                    this.error = "'" + str + "' is not a number";
                    return false;
                }
            } else if (arrc[b] == '?') {
                String str = this.parse.shift();
                if (str.equals("start") || str.equals("on") || str.equals("true")) {
                    this.args.push(Boolean.TRUE);
                } else if (str.equals("stop") || str.equals("off") || str.equals("false")) {
                    this.args.push(Boolean.FALSE);
                } else {
                    this.error = "'" + str + "' is not a boolean value";
                    return false;
                }
            }
        }
        return true;
    }

    public int popInt() {
        Integer integer = (Integer) this.args.pop();
        return integer.intValue();
    }

    public String popString() {
        return this.args.pop() + "";
    }

    public boolean popBoolean() {
        Boolean bool = (Boolean) this.args.pop();
        return bool.booleanValue();
    }
}
