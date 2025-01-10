package parser;

import common.MudgeSanity;
import server.Resources;

public abstract class Parser {
    protected Resources resources = null;

    public Parser(Resources resources) {
        this.resources = resources;
    }

    public abstract boolean check(String string, int n);

    public abstract void parse(String string1, String string2) throws Exception;

    public boolean isOutput(int n) {
        return (n == 0 || n == 30 || n == 32);
    }

    public void process(String string1, String string2, int n) {
        try {
            if (check(string1, n))
                parse(string1, string2);
        } catch (Exception exception) {
            MudgeSanity.logException("output parser", exception, false);
        }
    }
}
