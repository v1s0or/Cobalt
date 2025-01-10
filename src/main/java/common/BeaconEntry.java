package common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import sleep.runtime.SleepUtils;

public class BeaconEntry implements Serializable, Loggable {

    public static final int LINK_NONE = 0;

    public static final int LINK_GOOD = 1;

    public static final int LINK_BROKEN = 2;

    protected String id = "";

    protected String pid = "";

    protected String ver = "";

    protected String intz = "";

    protected String comp = "";

    protected String user = "";

    protected String is64 = "0";

    protected String ext = "";

    protected long last = System.currentTimeMillis();

    protected long diff = 0L;

    protected int state = 0;

    protected int hint = 0;

    protected String pbid = "";

    protected String note = "";

    protected String barch = "";

    protected boolean alive = true;

    protected String port = "";

    protected boolean sane = false;

    protected String chst = null;

    protected String proc = "";

    protected String accent = "";

    protected String lname = "";

    public static final int METADATA_FLAG_NOTHING = 1;

    public static final int METADATA_FLAG_X64_AGENT = 2;

    public static final int METADATA_FLAG_X64_SYSTEM = 4;

    public static final int METADATA_FLAG_ADMIN = 8;

    public String getId() {
        return this.id;
    }

    public boolean sane() {
        return this.sane;
    }

    public String getListenerName() {
        return this.lname;
    }

    public String getPort() {
        return this.port;
    }

    public void die() {
        this.alive = false;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public boolean isActive() {
        if (!isAlive()) {
            return false;
        }
        return !isLinked() || getLinkState() == 1;
    }

    public String getComputer() {
        return this.comp;
    }

    public boolean isEmpty() {
        return this.intz == null || this.intz.length() == 0;
    }

    public String getUser() {
        return this.user;
    }

    public String getInternal() {
        return this.intz;
    }

    public String getExternal() {
        return this.ext;
    }

    public String getPid() {
        if (isSSH()) {
            return "";
        }
        return this.pid;
    }

    public PivotHint getPivotHint() {
        return new PivotHint(this.hint);
    }

    public double getVersion() {
        try {
            if (isSSH() && this.ver.startsWith("ssh-CYGWIN_NT-")) {
                return Double.parseDouble(CommonUtils.strip(this.ver, "ssh-CYGWIN_NT-"));
            }
            if (isBeacon()) {
                return Double.parseDouble(this.ver);
            }
            return 0.0D;
        } catch (Exception exception) {
            return 0.0D;
        }
    }

    public String getNote() {
        return this.note;
    }

    public String getParentId() {
        return this.pbid;
    }

    public boolean isLinked() {
        return this.pbid.length() > 0;
    }

    public int getLinkState() {
        return this.state;
    }

    public String arch() {
        return this.barch;
    }

    public boolean is64() {
        if (!this.is64.equals("1") && !this.is64.equals("0")) {
            CommonUtils.print_warn("is64 is: '" + this.is64 + "'");
        }
        return this.is64.equals("1");
    }

    public boolean isAdmin() {
        return getUser().endsWith(" *");
    }

    public void setExternal(String string) {
        if (checkExt(string)) {
            this.ext = string;
        } else {
            CommonUtils.print_error("Refused to assign: '" + string + "' [was: '"
                    + this.ext + "'] as external address to Beacon: '" + this.id + "'");
        }
    }

    public void setLastCheckin(long l) {
        this.last = l;
    }

    public void setNote(String string) {
        this.note = string;
    }

    public void setAccent(String string) {
        this.accent = string;
    }

    public String getAccent() {
        return this.accent;
    }

    public boolean idle(long l) {
        return (this.diff >= l);
    }

    public String getLastCheckin() {
        String str = "ms";
        long l = this.diff;
        if (l > 1000L) {
            l /= 1000L;
            str = "s";
        } else {
            return l + str;
        }
        if (l > 60L) {
            l /= 60L;
            str = "m";
        }
        if (l > 60L) {
            l /= 60L;
            str = "h";
        }
        return l + str;
    }

    public BeaconEntry(byte[] arrby, String string1, String string2, String string3) {
        boolean bool;
        try {
            DataParser dataParser = new DataParser(arrby);
            dataParser.big();
            dataParser.consume(20);
            this.id = Long.toString(CommonUtils.toUnsignedInt(dataParser.readInt()));
            this.pid = Long.toString(CommonUtils.toUnsignedInt(dataParser.readInt()));
            this.port = Integer.toString(CommonUtils.toUnsignedShort(dataParser.readShort()));
            byte b = dataParser.readByte();
            if (CommonUtils.Flag(b, 1)) {
                this.barch = "";
                this.pid = "";
                this.is64 = "";
            } else if (CommonUtils.Flag(b, 2)) {
                this.barch = "x64";
            } else {
                this.barch = "x86";
            }
            this.is64 = CommonUtils.Flag(b, 4) ? "1" : "0";
            bool = CommonUtils.Flag(b, 8);
        } catch (IOException iOException) {
            MudgeSanity.logException("Could not parse metadata!", iOException, false);
            this.sane = false;
            return;
        }
        String str = CommonUtils.bString(
                Arrays.copyOfRange(arrby, 31, arrby.length), string1);
        String[] arrstring = str.split("\t");
        if (arrstring.length > 0)
            this.ver = arrstring[0];
        if (arrstring.length > 1)
            this.intz = arrstring[1];
        if (arrstring.length > 2)
            this.comp = arrstring[2];
        if (arrstring.length > 3)
            this.user = arrstring[3];
        if (bool)
            this.user += " *";
        if (arrstring.length > 4)
            this.proc = arrstring[4];
        this.ext = string2;
        this.chst = string1;
        this.lname = string3;
        this.sane = sanity();
    }

    public String getCharset() {
        return this.chst;
    }

    public boolean sanity() {
        LinkedList linkedList = new LinkedList();
        try {
            return _sanity(linkedList);
        } catch (Exception exception) {
            this.id = "0";
            this.intz = "";
            MudgeSanity.logException("Validator blew up!", exception, false);
            return false;
        }
    }

    public boolean checkExt(String string) {
        if (string == null)
            return true;
        if ("".equals(string))
            return true;
        String str = string;
        if (string.endsWith(" \u26af \u26af") && string.length() > 5) {
            str = string.substring(0, string.length() - 4);
        } else if (string.endsWith(" \u26af\u26af") && string.length() > 4) {
            str = string.substring(0, string.length() - 3);
        } else {
            str = string;
        }
        return CommonUtils.isIP(str) || CommonUtils.isIPv6(str) || "unknown".equals(str);
    }

    public String getProcess() {
        return this.proc;
    }

    public boolean _sanity(LinkedList linkedList) {
        if (!CommonUtils.isNumber(this.id)) {
            linkedList.add("id '" + this.id + "' is not a number");
            this.id = "0";
        }
        if (!"".equals(intz) && !CommonUtils.isIP(intz)
                && !CommonUtils.isIPv6(intz) && !"unknown".equals(intz)) {
            linkedList.add("internal address '" + intz + "' is not an address");
            intz = "";
        }
        if (!checkExt(this.ext)) {
            linkedList.add("external address '" + this.ext + "' is not an address");
            this.ext = "";
        }
        if (!"".equals(this.pid) && !CommonUtils.isNumber(this.pid)) {
            linkedList.add("pid '" + this.pid + "' is not a number");
            this.pid = "0";
        }
        if (!"".equals(this.port) && !CommonUtils.isNumber(this.port)) {
            linkedList.add("port '" + this.port + "' is not a number");
            this.port = "";
        }
        if (!"".equals(this.is64) && !CommonUtils.isNumber(this.is64)) {
            linkedList.add("is64 '" + this.is64 + "' is not a number");
            this.is64 = "";
        }
        if (this.comp != null && this.comp.length() > 64) {
            linkedList.add("comp '" + this.comp + "' is too long. Truncating");
            this.comp = this.comp.substring(0, 63);
        }
        if (this.user != null && this.user.length() > 64) {
            linkedList.add("user '" + this.user + "' is too long. Truncating");
            this.user = this.user.substring(0, 63);
        }
        if (linkedList.size() > 0) {
            Iterator iterator = linkedList.iterator();
            CommonUtils.print_error("Beacon entry did not validate");
            while (iterator.hasNext()) {
                System.out.println("\t" + iterator.next());
            }
            return false;
        }
        return true;
    }

    public BeaconEntry(String string) {
        this.id = string;
        this.sane = sanity();
    }

    public void touch() {
        this.diff = System.currentTimeMillis() - this.last;
    }

    public BeaconEntry copy() {
        BeaconEntry beaconEntry = new BeaconEntry(this.id);
        beaconEntry.pid = this.pid;
        beaconEntry.ver = this.ver;
        beaconEntry.intz = this.intz;
        beaconEntry.comp = this.comp;
        beaconEntry.user = this.user;
        beaconEntry.is64 = this.is64;
        beaconEntry.ext = this.ext;
        beaconEntry.diff = this.diff;
        beaconEntry.last = this.last;
        beaconEntry.state = this.state;
        beaconEntry.pbid = this.pbid;
        beaconEntry.note = this.note;
        beaconEntry.alive = this.alive;
        beaconEntry.barch = this.barch;
        beaconEntry.port = this.port;
        beaconEntry.chst = this.chst;
        beaconEntry.hint = this.hint;
        beaconEntry.proc = this.proc;
        beaconEntry.accent = this.accent;
        beaconEntry.lname = this.lname;
        return beaconEntry;
    }

    public Map toMap() {
        HashMap<String, String> hashMap = new HashMap();
        hashMap.put("external", this.ext);
        hashMap.put("internal", this.intz);
        hashMap.put("host", this.intz);
        hashMap.put("user", this.user);
        hashMap.put("computer", this.comp);
        hashMap.put("last", this.diff + "");
        hashMap.put("lastf", getLastCheckin());
        hashMap.put("id", this.id);
        hashMap.put("pid", getPid());
        hashMap.put("is64", this.is64);
        hashMap.put("pbid", this.pbid);
        hashMap.put("note", this.note);
        hashMap.put("barch", this.barch);
        hashMap.put("arch", this.barch);
        hashMap.put("port", getPort());
        hashMap.put("charset", getCharset());
        hashMap.put("phint", this.hint + "");
        hashMap.put("process", this.proc);
        hashMap.put("_accent", this.accent);
        hashMap.put("listener", this.lname);
        if (this.alive) {
            hashMap.put("alive", "true");
        } else {
            hashMap.put("alive", "false");
        }
        if (this.state != 0) {
            if (this.state == 1) {
                hashMap.put("state", "good");
            } else if (this.state == 2) {
                hashMap.put("state", "broken");
            }
        }
        hashMap.put("os", getOperatingSystem());
        hashMap.put("ver", Double.toString(getVersion()));
        if (isSSH()) {
            hashMap.put("session", "ssh");
        } else if (isBeacon()) {
            hashMap.put("session", "beacon");
        } else {
            hashMap.put("session", "unknown");
        }
        return hashMap;
    }

    public boolean wantsMetadata() {
        return (this.user.length() == 0);
    }

    public String title() {
        if (isBeacon()) {
            return title("Beacon");
        }
        return "SSH " + this.intz;
    }

    public String title(String string) {
        return string + " " + this.intz + "@" + this.pid;
    }

    public String toString() {
        return getId() + " -> " + title() + ", " + getLastCheckin();
    }

    public Stack eventArguments() {
        Stack stack = new Stack();
        stack.push(SleepUtils.getHashWrapper(toMap()));
        stack.push(SleepUtils.getScalar(this.id));
        return stack;
    }

    public void link(String string, int n) {
        this.pbid = string;
        this.state = 1;
        this.hint = n;
    }

    public void delink() {
        this.state = 2;
        this.lname = "";
    }

    public String getBeaconId() {
        return this.id;
    }

    public String getLogFile() {
        if (isSSH()) {
            return "ssh_" + this.id + ".log";
        }
        return "beacon_" + this.id + ".log";
    }

    public String getLogFolder() {
        return null;
    }

    public boolean isBeacon() {
        return !isSSH();
    }

    public boolean isSSH() {
        return this.ver.startsWith("ssh-");
    }

    public String getOperatingSystem() {
        if (isBeacon()) {
            return "Windows";
        }
        if ("ssh-".equals(this.ver)) {
            return "Unknown";
        }
        if ("ssh-Darwin".equals(this.ver)) {
            return "MacOS X";
        }
        if (this.ver.startsWith("ssh-CYGWIN_NT-")) {
            return "Windows";
        }
        return this.ver.substring(4);
    }

    public void formatEvent(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeBytes(CommonUtils.formatLogDate(System.currentTimeMillis()));
        dataOutputStream.writeBytes(" ");
        dataOutputStream.writeBytes("[metadata] ");
        if (isLinked()) {
            dataOutputStream.writeBytes("beacon_" + getParentId()
                    + " -> " + getInternal() + "; ");
        } else if ("".equals(getExternal())) {
            dataOutputStream.writeBytes("unknown <- " + getInternal() + "; ");
        } else {
            dataOutputStream.writeBytes(getExternal() + " <- " + getInternal() + "; ");
        }
        if (isSSH()) {
            CommonUtils.writeUTF8(dataOutputStream, "computer: " + getComputer() + "; ");
            CommonUtils.writeUTF8(dataOutputStream, "user: " + getUser() + "; ");
            dataOutputStream.writeBytes("os: " + getOperatingSystem() + "; ");
            dataOutputStream.writeBytes("port: " + getPort());
        } else {
            CommonUtils.writeUTF8(dataOutputStream, "computer: " + getComputer() + "; ");
            CommonUtils.writeUTF8(dataOutputStream, "user: " + getUser() + "; ");
            dataOutputStream.writeBytes("process: " + getProcess() + "; ");
            dataOutputStream.writeBytes("pid: " + getPid() + "; ");
            dataOutputStream.writeBytes("os: " + getOperatingSystem() + "; ");
            dataOutputStream.writeBytes("version: " + getVersion() + "; ");
            dataOutputStream.writeBytes("beacon arch: " + this.barch);
            if (is64()) {
                dataOutputStream.writeBytes(" (x64)");
            }
        }
        dataOutputStream.writeBytes("\n");
    }
}
