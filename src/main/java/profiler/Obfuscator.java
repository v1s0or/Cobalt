package profiler;

import java.util.UUID;

public class Obfuscator {
    protected String code;

    public static String strrep(String data, String oldstr, String newstr) {
        StringBuffer work = new StringBuffer(data);

        if (oldstr.length() == 0) {
            return data;
        }
        int x = 0;
        int oldlen = oldstr.length();
        int newlen = newstr.length();

        while ((x = work.indexOf(oldstr, x)) > -1) {
            work.replace(x, x + oldlen, newstr);
            x += newstr.length();
        }

        return work.toString();
    }


    public static String garbage(String replaceme) {
        return replaceme.charAt(0) + _garbage(replaceme.substring(1, replaceme.length()));
    }


    public static String _garbage(String replaceme) {
        String garbage = strrep(ID(), "-", "");
        if (replaceme == null) {
            return "";
        }
        if (replaceme.length() > garbage.length()) {
            return garbage + garbage(replaceme.substring(garbage.length()));
        }
        if (replaceme.length() == garbage.length()) {
            return garbage;
        }

        return garbage.substring(0, replaceme.length());
    }


    public static String ID() {
        return UUID.randomUUID().toString();
    }


    public Obfuscator(String code) {
        this.code = code;
    }


    public String obfuscate() {
        this.code = strrep(this.code, "internalAddress", garbage("internalAddress"));
        this.code = strrep(this.code, "applications", garbage("applications"));
        this.code = strrep(this.code, "checkPlugin", garbage("checkPlugin"));
        this.code = strrep(this.code, "extractVersion", garbage("extractVersion"));
        this.code = strrep(this.code, "tokens", garbage("tokens"));
        this.code = strrep(this.code, "fixReaderVersion", garbage("fixReaderVersion"));
        this.code = strrep(this.code, "checkControl", garbage("checkControl"));
        this.code = strrep(this.code, "decloak", garbage("decloak"));
        return this.code;
    }
}
