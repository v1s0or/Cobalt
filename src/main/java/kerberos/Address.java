package kerberos;

import common.DataParser;

import java.io.IOException;

public class Address {
    protected int addrtype;

    protected String addrdata;

    public Address(DataParser paramDataParser) throws IOException {
        this.addrtype = paramDataParser.readShort();
        this.addrdata = paramDataParser.readCountedString();
    }

    public String toString() {
        return this.addrtype + "/" + this.addrdata;
    }
}
