package kerberos;

import common.DataParser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Credential {
    protected Principal client;

    protected Principal server;

    protected KeyBlock key;

    protected Times time;

    protected byte is_skey;

    protected int tktflags;

    protected List addresses = new LinkedList();

    protected List authdata = new LinkedList();

    protected String ticket;

    protected String second_ticket;

    public Credential(DataParser paramDataParser) throws IOException {
        this.client = new Principal(paramDataParser);
        this.server = new Principal(paramDataParser);
        this.key = new KeyBlock(paramDataParser);
        this.time = new Times(paramDataParser);
        this.is_skey = (byte) paramDataParser.readChar();
        this.tktflags = paramDataParser.readInt();
        int i = paramDataParser.readInt();
        int j;
        for (j = 0; j < i; j++)
            this.addresses.add(new Address(paramDataParser));
        j = paramDataParser.readInt();
        for (byte b = 0; b < j; b++)
            this.authdata.add(new AuthData(paramDataParser));
        this.ticket = paramDataParser.readCountedString();
        this.second_ticket = paramDataParser.readCountedString();
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Credential\n");
        stringBuffer.append("\tclient: " + this.client + "\n");
        stringBuffer.append("\tserver: " + this.client + "\n");
        stringBuffer.append("\tkey:    " + this.key + "\n");
        stringBuffer.append("\tticket: " + this.ticket.length() + "\n");
        stringBuffer.append("\tsecond: " + this.second_ticket.length() + "\n");
        return stringBuffer.toString();
    }
}
