package common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexParser {

    protected String text;

    protected Matcher last = null;

    public RegexParser(String string) {
        this.text = string;
    }

    public static boolean isMatch(String string1, String string2) {
        Pattern pattern = Pattern.compile(string2);
        Matcher matcher = pattern.matcher(string1);
        return matcher.matches();
    }

    public boolean matches(String string) {
        Pattern pattern = Pattern.compile(string);
        Matcher matcher = pattern.matcher(this.text);
        this.last = matcher;
        return matcher.matches();
    }

    public boolean endsWith(String string) {
        if (this.text.endsWith(string)) {
            this.text = this.text.substring(0, this.text.length() - string.length());
            return true;
        }
        return false;
    }

    public String group(int n) {
        return this.last.group(n);
    }

    public void whittle(int n) {
        this.text = this.last.group(n);
    }

    public String getText() {
        return this.text;
    }
}
