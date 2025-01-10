package kerberos;

import common.DataParser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Principal {
    protected int name_type;

    protected String realm;

    protected List components = new LinkedList();

    public Principal(int n, String string) {
        this.name_type = n;
        this.realm = string;
    }

    public Principal(DataParser paramDataParser) throws IOException {
        this.name_type = paramDataParser.readInt();
        int i = paramDataParser.readInt();
        this.realm = paramDataParser.readCountedString();
        for (byte b = 0; b < i; b++)
            this.components.add(paramDataParser.readCountedString());
    }

    public String toString() {
        return "Principal(" + this.name_type + ") " + this.realm + " " + this.components;
    }
}
