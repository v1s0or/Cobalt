package aggressor.headless;

import aggressor.Aggressor;
import aggressor.MultiFrame;
import common.Authorization;
import common.Callback;
import common.CommonUtils;
import common.License;
import common.MudgeSanity;
import common.TeamQueue;
import common.TeamSocket;

import java.util.Map;

import sleep.parser.ParserConfig;
import ssl.ArmitageTrustListener;
import ssl.SecureSocket;

public class Start implements Callback, ArmitageTrustListener {

    protected MultiFrame window;

    protected TeamQueue tqueue = null;

    protected String desc = "";

    protected String script = "";

    public Start(MultiFrame paramMultiFrame) {
        this.window = paramMultiFrame;
    }

    public static void main(String[] arrstring) {
        ParserConfig.installEscapeConstant('c', "\003");
        ParserConfig.installEscapeConstant('U', "\037");
        ParserConfig.installEscapeConstant('o', "\017");
        License.checkLicenseConsole(new Authorization());
        if (arrstring.length == 5) {
            String str1 = arrstring[0];
            int i = CommonUtils.toNumber(arrstring[1], 50050);
            String str2 = arrstring[2];
            String str3 = arrstring[3];
            String str4 = arrstring[4];
            (new Start(null)).go(str1, i, str2, str3, str4);
        } else if (arrstring.length == 4) {
            String str1 = arrstring[0];
            int i = CommonUtils.toNumber(arrstring[1], 50050);
            String str2 = arrstring[2];
            String str3 = arrstring[3];
            (new Start(null)).go(str1, i, str2, str3, null);
        } else {
            System.out.println("Welcome to the Cobalt Strike (Headless) Client. Version " + Aggressor.VERSION + "\nCopyright 2015, Strategic Cyber LLC\n\nQuick help:\n\n\t./agscript [host] [port] [user] [pass]\n\t\tConnect to a team server and start the Aggressor Script console\n\n\t./agscript [host] [port] [user] [pass] </path/to/file.cna>\n\t\tConnect to a team server and execute the specified script");
            System.exit(0);
        }
    }

    public boolean trust(String string) {
        return true;
    }

    public void go(String string1, int n, String string2, String string3, String string4) {
        this.script = string4;
        try {
            SecureSocket secureSocket = new SecureSocket(string1, n, this);
            secureSocket.authenticate(string3);
            TeamSocket teamSocket = new TeamSocket(secureSocket.getSocket());
            this.tqueue = new TeamQueue(teamSocket);
            this.tqueue.call("aggressor.authenticate", CommonUtils.args(string2, string3, Aggressor.VERSION), this);
        } catch (Exception exception) {
            MudgeSanity.logException("client connect", exception, true);
        }
    }

    public void result(String string, Object object) {
        if ("aggressor.authenticate".equals(string)) {
            String str = object + "";
            if (str.equals("SUCCESS")) {
                this.tqueue.call("aggressor.metadata", CommonUtils.args(Long.valueOf(System.currentTimeMillis())), this);
            } else {
                CommonUtils.print_error(str);
                this.tqueue.close();
                System.exit(0);
            }
        } else if ("aggressor.metadata".equals(string)) {
            new HeadlessClient(this.window, this.tqueue, (Map) object, this.script);
        }
    }
}
