package kerberos;

import common.CommonUtils;
import common.DataParser;
import common.MudgeSanity;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Ccache {
    protected Principal primary_principal;

    protected List credentials = new LinkedList();

    public Ccache(String string) {
        parse(CommonUtils.readFile(string));
    }

    public void parse(byte[] arrby) {
        try {
            DataParser dataParser = new DataParser(arrby);
            dataParser.big();
            int i = dataParser.readShort();
            if (i != 1284) {
                CommonUtils.print_error("VERSION FAIL: " + i);
                return;
            }
            int j = dataParser.readShort();
            dataParser.consume(j);
            this.primary_principal = new Principal(dataParser);
            while (dataParser.more())
                this.credentials.add(new Credential(dataParser));
        } catch (IOException iOException) {
            MudgeSanity.logException("CredCacheParse", iOException, false);
        }
    }

    public String toString() {
        return this.primary_principal + "\n" + this.credentials;
    }

    public static void main(String[] arrstring) {
        CommonUtils.print_good(new Ccache(arrstring[0]) + "");
    }
}
