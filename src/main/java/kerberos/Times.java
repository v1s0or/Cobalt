package kerberos;

import common.DataParser;

import java.io.IOException;

public class Times {
    protected int authtime;

    protected int starttime;

    protected int endtime;

    protected int renew_till;

    public Times(DataParser paramDataParser) throws IOException {
        this.authtime = paramDataParser.readInt();
        this.starttime = paramDataParser.readInt();
        this.endtime = paramDataParser.readInt();
        this.renew_till = paramDataParser.readInt();
    }

    public String toString() {
        return "Times... meh";
    }
}
