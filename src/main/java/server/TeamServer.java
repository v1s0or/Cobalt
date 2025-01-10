package server;

import c2profile.Loader;
import c2profile.Profile;
import common.AssertUtils;
import common.Authorization;
import common.CommonUtils;
import common.Keys;
import common.License;
import common.MudgeSanity;
import common.Requirements;
import common.TeamSocket;
import dns.QuickSecurity;

import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ssl.PostAuthentication;
import ssl.SecureServerSocket;

public class TeamServer {
    protected int port;

    protected String host;

    protected Resources resources;

    protected Map calls = new HashMap();

    protected Profile c2profile = null;

    protected String pass;

    protected Authorization auth;

    private static String host_help = "It's best if your targets can reach your team server via this IP address. It's OK if this IP address is a redirector.\n\nWhy does this matter?\n\nCobalt Strike uses this IP address as a default throughout its workflows. Cobalt Strike's DNS Beacon also uses this IP address for its HTTP channel. The Covert VPN feature uses this IP too. If your target can't reach your team server via this IP, it's possible some CS features may not work as expected.";

    public TeamServer(String string1, int n, String string2, Profile profile, Authorization authorization) {
        this.host = string1;
        this.port = n;
        this.pass = string2;
        this.c2profile = profile;
        this.auth = authorization;
    }

    public void go() {
        try {
            new ProfileEdits(this.c2profile);
            this.c2profile.addParameter(".watermark", this.auth.getWatermark());
            this.c2profile.addParameter(".self", CommonUtils.readAndSumFi1e(TeamServer.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
            this.resources = new Resources(this.calls);
            this.resources.put("c2profile", this.c2profile);
            this.resources.put("localip", this.host);
            this.resources.put("password", this.pass);
            (new TestCall()).register(this.calls);
            WebCalls webCalls = new WebCalls(this.resources);
            webCalls.register(this.calls);
            this.resources.put("webcalls", webCalls);
            (new Listeners(this.resources)).register(this.calls);
            (new Beacons(this.resources)).register(this.calls);
            (new Phisher(this.resources)).register(this.calls);
            (new VPN(this.resources)).register(this.calls);
            (new BrowserPivotCalls(this.resources)).register(this.calls);
            (new DownloadCalls(this.resources)).register(this.calls);
            Iterator iterator = Keys.getDataModelIterator();
            while (iterator.hasNext())
                (new DataCalls(this.resources, (String) iterator.next())).register(this.calls);
            if (!ServerUtils.hasPublicStage(this.resources))
                CommonUtils.print_warn("Woah! Your profile disables hosted payload stages. Payload staging won't work.");
            SecureServerSocket secureServerSocket = new SecureServerSocket(this.port);
            CommonUtils.print_good("Team server is up on " + this.port);
            CommonUtils.print_info("SHA256 hash of SSL cert is: " + secureServerSocket.fingerprint());
            this.resources.call("listeners.go");
            while (true) {
                secureServerSocket.acceptAndAuthenticate(this.pass, new PostAuthentication() {
                    public void clientAuthenticated(Socket param1Socket) {
                        try {
                            param1Socket.setSoTimeout(0);
                            TeamSocket teamSocket = new TeamSocket(param1Socket);
                            (new Thread(new ManageUser(teamSocket, TeamServer.this.resources, TeamServer.this.calls), "Manage: unauth'd user")).start();
                        } catch (Exception exception) {
                            MudgeSanity.logException("Start client thread", exception, false);
                        }
                    }
                });
            }
        } catch (Exception exception) {
            MudgeSanity.logException("team server startup", exception, false);
            return;
        }
    }

    public static void main(String[] arrstring) {
        int i = CommonUtils.toNumber(System.getProperty("cobaltstrike.server_port", "50050"), 50050);
        if (!AssertUtils.TestPort(i))
            System.exit(0);
        Requirements.checkConsole();
        Authorization authorization = new Authorization();
        License.checkLicenseConsole(authorization);
        MudgeSanity.systemDetail("scheme", QuickSecurity.getCryptoScheme() + "");
        if (arrstring.length == 0 || (arrstring.length == 1 && ("-h".equals(arrstring[0]) || "--help".equals(arrstring[0])))) {
            CommonUtils.print_info("./teamserver <host> <password> [/path/to/c2.profile] [YYYY-MM-DD]\n\n\t<host> is the (default) IP address of this Cobalt Strike team server\n\t<password> is the shared password to connect to this server\n\t[/path/to/c2.profile] is your Malleable C2 profile\n\t[YYYY-MM-DD] is a kill date for Beacon payloads run from this server\n");
        } else if (arrstring.length != 2 && arrstring.length != 3 && arrstring.length != 4) {
            CommonUtils.print_error("Missing arguments to start team server\n\t./teamserver <host> <password> [/path/to/c2.profile] [YYYY-MM-DD]");
        } else if (!CommonUtils.isIP(arrstring[0])) {
            CommonUtils.print_error("The team server <host> must be an IP address. " + host_help);
        } else if ("127.0.0.1".equals(arrstring[0])) {
            CommonUtils.print_error("Don't use 127.0.0.1 for the team server <host>. " + host_help);
        } else if ("0.0.0.0".equals(arrstring[0])) {
            CommonUtils.print_error("Don't use 0.0.0.0 for the team server <host>. " + host_help);
        } else if (arrstring.length == 2) {
            MudgeSanity.systemDetail("c2Profile", "default");
            TeamServer teamServer = new TeamServer(arrstring[0], i, arrstring[1], Loader.LoadDefaultProfile(), authorization);
            teamServer.go();
        } else if (arrstring.length == 3 || arrstring.length == 4) {
            MudgeSanity.systemDetail("c2Profile", arrstring[2]);
            Profile profile = Loader.LoadProfile(arrstring[2]);
            if (profile == null) {
                CommonUtils.print_error("exiting because of errors in " + arrstring[2] + ". Use ./c2lint to check the file");
                System.exit(0);
            }
            CommonUtils.print_good("I see you're into threat replication. " + arrstring[2] + " loaded.");
            if (arrstring.length == 4) {
                long l = CommonUtils.parseDate(arrstring[3], "yyyy-MM-dd");
                if (l < System.currentTimeMillis()) {
                    CommonUtils.print_error("Beacon kill date " + arrstring[3] + " is in the past!");
                    System.exit(0);
                } else if (l > 0L) {
                    CommonUtils.print_good("Beacon kill date is: " + arrstring[3] + "!");
                    profile.addParameter(".killdate", arrstring[3]);
                } else {
                    CommonUtils.print_error("Invalid kill date: '" + arrstring[3] + "' (format is YYYY-MM-DD)");
                    System.exit(0);
                }
                MudgeSanity.systemDetail("kill date", arrstring[3]);
            } else {
                MudgeSanity.systemDetail("kill date", "none");
            }
            TeamServer teamServer = new TeamServer(arrstring[0], i, arrstring[1], profile, authorization);
            teamServer.go();
        }
    }
}
