package kerberos;

import common.DataParser;

import java.io.IOException;

public class AuthData {
    protected int authtype;

    protected String authdata;

    public AuthData(DataParser paramDataParser) throws IOException {
        this.authtype = paramDataParser.readShort();
        this.authdata = paramDataParser.readCountedString();
    }

    public String toString() {
        return this.authtype + "/" + this.authdata;
    }
}
