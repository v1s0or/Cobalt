package c2profile;

import beacon.setup.ProcessInject;
import cloudstrike.NanoHTTPD;
import cloudstrike.Response;
import common.Authorization;
import common.CommonUtils;
import common.License;
import common.MudgeSanity;
import common.SleevedResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;

import net.jsign.DigestAlgorithm;
import net.jsign.timestamp.TimestampingMode;
import pe.MalleablePE;
import pe.PEParser;

public class Lint {
    public static final int PROGRAM_TRANSFORM = 0;

    public static final int PROGRAM_RECOVER = 1;

    protected Profile prof;

    protected String uri = "";

    protected Map headers = new HashMap();

    public Lint(Profile profile) {
        this.prof = profile;
    }

    public void bounds(String string, int n1, int n2) {
        int i = CommonUtils.toNumber(this.prof.getString(string), 0);
        if (i < n1)
            CommonUtils.print_error("Option " + string + " is " + i
                    + "; less than lower bound of " + n1);
        if (i > n2)
            CommonUtils.print_error("Option " + string + " is " + i
                    + "; greater than upper bound of " + n2);
    }

    public void boundsLen(String string, int n) throws Exception {
        String str = this.prof.getString(string);
        if (str.length() > n)
            CommonUtils.print_error("Length of option " + string
                    + " is " + str.length() + "; greater than upper bound of " + n);
    }

    public byte[] randomData(int n) {
        Random random = new Random();
        byte[] arrby = new byte[n];
        random.nextBytes(arrby);
        return arrby;
    }

    public void verb_compatability() {
        if ("GET".equals(this.prof.getString(".http-get.verb"))
                && this.prof.posts(".http-get.client.metadata")) {
            CommonUtils.print_error(".http-get.verb is GET, but .http-get.client.metadata needs POST");
        }
        if ("GET".equals(this.prof.getString(".http-post.verb"))) {
            if (this.prof.posts(".http-post.client.id")) {
                CommonUtils.print_error(".http-post.verb is GET, but .http-post.client.id needs POST");
            }
            if (this.prof.posts(".http-post.client.output")) {
                CommonUtils.print_error(".http-post.verb is GET, but .http-post.client.output needs POST");
            }
        }
    }

    public void safetylen(String string1, String string2, Map<String, String> map) {
        for (Map.Entry entry : map.entrySet()) {
            String str = entry.getValue() + "";
            if (str.length() > 1024) {
                CommonUtils.print_error(string2 + " " + string1 + " '" + entry.getKey()
                        + "' is " + str.length() + " bytes [should be <1024 bytes]");
            }
        }
    }

    public void safetyuri(String string1, String string2, Map<String, String> map) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.uri + string2 + "?");
        for (Map.Entry entry : map.entrySet()) {
            String str1 = entry.getKey() + "";
            String str2 = entry.getValue() + "";
            stringBuffer.append(str1 + "=" + str2);
        }
        if (stringBuffer.toString().length() > 1024) {
            CommonUtils.print_error(string1 + " URI line (uri + parameters) is "
                    + stringBuffer.toString().length() + " bytes [should be <1024 bytes]");
        }
    }

    public void testuri(String string1, String string2, int n) {
        if (string2.length() > n)
            CommonUtils.print_error(string1 + " is too long! " + string2.length()
                    + " bytes [should be <=" + n + " bytes]");
        if (string2.indexOf("?") >= 0)
            CommonUtils.print_error(string1 + " '" + string2 + "' should not contain a ?");
        if (!string2.startsWith("/"))
            CommonUtils.print_error(string1 + " '" + string2 + "' must start with a /");
    }

    public void testuri_stager(String string) {
        String str1 = this.prof.getString(string);
        String str2 = this.prof.getQueryString(".http-stager.client");
        if (!"".equals(str1)) {
            testuri(string, str1, 79);
        } else {
            str1 = CommonUtils.MSFURI();
        }
        if (!"".equals(str2)) {
            str1 = str1 + "?" + str2;
            if (str1.length() > 79) {
                CommonUtils.print_error(string + " URI line (uri + parameters) is "
                        + str1.toString().length() + " bytes [should be <80 bytes]");
            }
        }
    }

    public void testuri(String string) {
        int num = 0;
        String[] arrstring = this.prof.getString(string + ".uri").split(" ");
        for (int i = 0; i < arrstring.length; i++) {
            if (arrstring[i].length() > num) {
                this.uri = arrstring[i];
                num = arrstring[i].length();
            }
            testuri(string + ".uri", arrstring[i], 63);
        }
    }

    public void testuriCompare(String string1, String string2) {
        LintURI lintURI = new LintURI();
        lintURI.add_split(string1, this.prof.getString(string1));
        lintURI.add_split(string2, this.prof.getString(string2));
        lintURI.add(".http-stager.uri_x86", this.prof.getString(".http-stager.uri_x86"));
        lintURI.add(".http-stager.uri_x64", this.prof.getString(".http-stager.uri_x64"));
        lintURI.checks();
    }

    public boolean test(String string1, String string2, int n) throws Exception {
        return test(string1, string2, n, false);
    }

    public boolean test(String string1, String string2, int n, boolean bl) throws Exception {
        byte[] arrby3;
        Response response = new Response("200 OK", null, (InputStream) null);
        byte[] arrby1 = randomData(n);
        byte[] arrby2 = Arrays.copyOf(arrby1, arrby1.length);
        if (string2.equals(".id")) {
            arrby1 = "1234".getBytes("UTF-8");
            arrby2 = Arrays.copyOf(arrby1, arrby1.length);
        }
        if (bl) {
            this.prof.apply(string1, response, arrby2);
        } else {
            this.prof.apply(string1 + string2, response, arrby2);
        }
        if (response.data != null) {
            arrby3 = new byte[response.data.available()];
            response.data.read(arrby3, 0, arrby3.length);
        } else {
            arrby3 = new byte[0];
        }
        safetyuri(string1, response.uri, response.params);
        safetylen("parameter", string1, response.params);
        safetylen("header", string1, response.header);
        String str = this.prof.recover(string1 + string2,
                response.header, response.params,
                new String(arrby3, "ISO8859-1"), response.uri);
        byte[] arrby4 = Program.toBytes(str);
        if (!Arrays.equals(arrby4, arrby1)) {
            CommonUtils.print_error(string1 + string2 + " transform+recover FAILED ("
                    + n + " byte[s])");
            return false;
        }
        for (Map.Entry entry : response.params.entrySet()) {
            String str1 = entry.getKey() + "";
            String str2 = entry.getValue() + "";
            entry.setValue(URLEncoder.encode(entry.getValue() + "", "UTF-8"));
        }
        for (Map.Entry entry : response.header.entrySet()) {
            String str1 = entry.getKey() + "";
            String str2 = entry.getValue() + "";
            entry.setValue(str2.replaceAll("\\P{Graph}", ""));
            if (".http-get.server".equals(string1))
                this.headers.put(str1.toLowerCase(), str2.toLowerCase());
            if (string1.endsWith(".client") && "cookie".equals(str1.toLowerCase())
                    && this.prof.option(".http_allow_cookies")) {
                CommonUtils.print_error(string1 + string2
                        + " uses HTTP cookie header, but http_allow_cookies is set to true.");
                return false;
            }
        }
        str = this.prof.recover(string1 + string2,
                response.header, response.params,
                new String(arrby3, "ISO8859-1"), response.uri);
        arrby4 = Program.toBytes(str);
        if (!Arrays.equals(arrby4, arrby1)) {
            CommonUtils.print_error(string1 + string2
                    + " transform+mangle+recover FAILED ("
                    + n + " byte[s]) - encode your data!");
            return false;
        }
        CommonUtils.print_good(string1 + string2
                + " transform+mangle+recover passed (" + n + " byte[s])");
        return true;
    }

    public boolean checkProgramSizes(String string, int n1, int n2) throws IOException {
        byte[] arrby;
        if (n2 == 0) {
            arrby = this.prof.apply_binary(string);
        } else {
            arrby = this.prof.recover_binary(string);
        }
        if (arrby.length < n1)
            return true;
        CommonUtils.print_error("Program " + string
                + " size check failed.\n\tProgram " + string
                + " must have a compiled size less than " + n1
                + " bytes. Current size is: " + arrby.length);
        return false;
    }

    public boolean checkPost3x() throws IOException {
        int i = this.prof.size(".http-post.client.output", 0x200000);
        if (i < 6291456)
            return true;
        CommonUtils.print_error("POST 3x check failed.\n\tEncoded HTTP POST must be less than 3x size of non-encoded post. Tested: 2097152 bytes; received " + i + " bytes");
        return false;
    }

    public void checkHeaders() {
        if ("chunked".equals(this.headers.get("transfer-encoding")))
            CommonUtils.print_error("Remove 'Transfer-Encoding: chunked' header. It will interfere with C2.");
    }

    public void checkCollissions(String string) {
        Program program = this.prof.getProgram(string);
        if (program == null) {
            return;
        }
        List<String> list = program.collissions(this.prof);
        for (String str : list) {
            CommonUtils.print_error(string + " collission for " + str);
        }
    }

    public void checkKeystore() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(this.prof.getSSLKeystore(),
                    this.prof.getSSLPassword().toCharArray());
            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, this.prof.getSSLPassword().toCharArray());
            SSLContext sSLContext = SSLContext.getInstance("SSL");
            sSLContext.init(keyManagerFactory.getKeyManagers(),
                    new TrustManager[]{new NanoHTTPD.TrustEverything()}, new SecureRandom());
            SSLServerSocketFactory sSLServerSocketFactory = sSLContext.getServerSocketFactory();
        } catch (Exception exception) {
            CommonUtils.print_error("Could not load SSL keystore: " + exception.getMessage());
        }
    }

    public void checkCodeSigner() {
        if ("".equals(this.prof.getString(".code-signer.alias")))
            CommonUtils.print_error(".code-signer.alias is empty. This is the keystore alias for your imported code signing cert");
        if ("".equals(this.prof.getString(".code-signer.password")))
            CommonUtils.print_error(".code-signer.password is empty. This is the keystore password");
        if (!"".equals(this.prof.getString(".code-signer.digest_algorithm"))) {
            String str = this.prof.getString(".code-signer.digest_algorithm");
            try {
                DigestAlgorithm.valueOf(str);
            } catch (Exception exception) {
                CommonUtils.print_error(".code-sign.digest_algorithm '" + str + "' is not valid. (Acceptable values: " + CommonUtils.joinObjects(DigestAlgorithm.values(), ", ") + ")");
            }
        }
        if (!"".equals(this.prof.getString(".code-signer.timestamp_mode"))) {
            String str = this.prof.getString(".code-signer.timestamp_mode");
            try {
                TimestampingMode.valueOf(str);
            } catch (Exception exception) {
                CommonUtils.print_error(".code-sign.timestamp_mode '" + str
                        + "' is not valid. (Acceptable values: "
                        + CommonUtils.joinObjects(TimestampingMode.values(), ", ") + ")");
            }
        }
        String str1 = this.prof.getString(".code-signer.keystore");
        String str2 = this.prof.getString(".code-signer.password");
        String str3 = this.prof.getString(".code-signer.alias");
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(str1), str2.toCharArray());
        } catch (Exception exception) {
            CommonUtils.print_error(".code-signer.keystore failed to load keystore: "
                    + exception.getMessage());
        }
    }

    public void checkPE() {
        try {
            PEParser pEParser1 = PEParser.load(
                    SleevedResource.readResource("resources/beacon.dll"));
            int i = this.prof.getInt(".stage.image_size_x86");
            int j = pEParser1.get("SizeOfImage");
            if (i > 0 && i < j) {
                CommonUtils.print_error(".stage.image_size_x86 must be larger than " + j + " bytes");
            }

            PEParser pEParser2 = PEParser.load(
                    SleevedResource.readResource("resources/beacon.x64.dll"));
            int k = this.prof.getInt(".stage.image_size_x64");
            int m = pEParser2.get("SizeOfImage");
            if (k > 0 && k < m) {
                CommonUtils.print_error(".stage.image_size_x64 must be larger than " + m + " bytes");
            }

            MalleablePE malleablePE1 = new MalleablePE(this.prof);
            byte[] arrby1 = malleablePE1.process(
                    SleevedResource.readResource("resources/beacon.dll"), "x86");
            if (arrby1.length > 271000) {
                CommonUtils.print_error(".stage.transform-x86 results in a stage that's too large");
            } else if (arrby1.length == 0) {
                CommonUtils.print_error(".stage.transform-x86 failed (unknown reason)");
            }

            MalleablePE malleablePE2 = new MalleablePE(this.prof);
            byte[] arrby2 = malleablePE2.process(
                    SleevedResource.readResource("resources/beacon.x64.dll"), "x64");
            if (arrby2.length > 271000) {
                CommonUtils.print_error(".stage.transform-x64 results in a stage that's too large");
            } else if (arrby2.length == 0) {
                CommonUtils.print_error(".stage.transform-x86 failed (unknown reason)");
            }

            String str1 = this.prof.getString(".stage.rich_header");
            if (str1.length() > 256) {
                CommonUtils.print_error(".stage.rich_header is too big. Reduce to <=256 bytes");
            }

            byte[] arrby3 = this.prof.getToString(".stage").getBytes();
            if (arrby3.length > 4096) {
                CommonUtils.print_error(".stage added " + arrby3.length
                        + " bytes of strings. Reduce to <=4096");
            }

            Set set1 = CommonUtils.toSetLC(
                    CommonUtils.readResourceAsString("resources/dlls.x86.txt").split("\n"));
            String str2 = this.prof.getString(".stage.module_x86").toLowerCase();
            if (!"".equals(str2) && set1.contains(str2)) {
                CommonUtils.print_error(".stage.module_x86 stomps '" + str2 + "' needed by x86 Beacon DLL.");
            }

            Set set2 = CommonUtils.toSetLC(
                    CommonUtils.readResourceAsString("resources/dlls.x64.txt").split("\n"));
            String str3 = this.prof.getString(".stage.module_x64").toLowerCase();
            if (!"".equals(str3) && set2.contains(str3)) {
                CommonUtils.print_error(".stage.module_x64 stomps '" + str3 + "' needed by x64 Beacon DLL.");
            }
            if (!"".equals(str2) && i > j) {
                CommonUtils.print_warn(".stage.module_x86 AND .stage.image_size_x86 are defined. Risky! Will " + str2 + " hold ~" + (i * 2) + " bytes?");
            }
            if (!"".equals(str3) && k > m) {
                CommonUtils.print_warn(".stage.module_x64 AND .stage.image_size_x64 are defined. Risky! Will " + str3 + " hold ~" + (k * 2) + " bytes?");
            }
        } catch (Exception exception) {
            MudgeSanity.logException("pe check", exception, false);
        }
    }

    public void checkProcessInject() throws IOException {
        boolean bool1 = this.prof.option(".process-inject.userwx");
        boolean bool2 = this.prof.option(".process-inject.startrwx");
        int i = this.prof.getInt(".process-inject.min_alloc");
        bounds(".process-inject.min_alloc", 0, 268435455);
        ProcessInject processInject = (new ProcessInject(this.prof)).check();
        Iterator iterator1 = processInject.getErrors().iterator();
        while (iterator1.hasNext()) {
            CommonUtils.print_error((String) iterator1.next());
        }
        Iterator iterator2 = processInject.getWarnings().iterator();
        while (iterator2.hasNext())
            CommonUtils.print_warn((String) iterator2.next());
    }

    public void setupProcessInject(String string) throws IOException {
        byte[] arrby1 = this.prof.getPrependedData(".process-inject.transform-" + string);
        byte[] arrby2 = this.prof.getAppendedData(".process-inject.transform-" + string);
        int i = arrby1.length + arrby2.length;
        if (i > 252) {
            CommonUtils.print_error(".process-inject.transform-" + string
                    + " is " + i + " bytes. Reduce to <=252 bytes");
        }
    }

    public void checkSpawnTo(String string1, String string2, String string3) {
        String str = this.prof.getString(string1);
        if (str.length() > 63)
            CommonUtils.print_error(string1 + " is too long. Limit to 63 characters");
        if (str.indexOf("\\") == -1)
            CommonUtils.print_error(string1 + " should refer to a full path.");
        if (str.indexOf("\\system32\\") > -1)
            CommonUtils.print_error(string1 + " references system32. This will break x86->x64 and x64->x86 spawns");
        if (str.indexOf(string2) > -1)
            CommonUtils.print_error(string1 + " references " + string2 + ". For this architecture, probably not what you want");
        if (str.indexOf(string3) == -1 && str.toLowerCase().indexOf(string3) > -1) {
            int i = str.toLowerCase().indexOf(string3);
            String str1 = str.substring(i, i + string3.length());
            CommonUtils.print_error(string1 + ": lowercase '" + str1 + "'. This allows runtime adjustments to work");
        }
        if (str.indexOf("rundll32.exe") > -1)
            CommonUtils.print_opsec("[OPSEC] " + string1 + " is '" + str + "'. This is a *really* bad OPSEC choice.");
    }

    public static void main(String[] arrstring) {
        if (arrstring.length == 0) {
            CommonUtils.print_info("Please specify a Beacon profile file\n\t./c2lint my.profile");
            return;
        }
        License.checkLicenseConsole(new Authorization());
        Profile profile = Loader.LoadProfile(arrstring[0]);
        if (profile == null) {
            return;
        }
        String[] variants = profile.getVariants();
        for (int i = 0; i < variants.length; i++) {
            Profile profile1 = profile.getVariantProfile(variants[i]);
            checkProfile(variants[i], profile1);
        }
    }

    public static void checkProfile(String string, Profile profile) {
        try {
            Lint lint = new Lint(profile);
            if (profile == null) {
                return;
            }
            StringBuffer sb = new StringBuffer();
            sb.append("\n\033[01;30m");//'\u001b'
            sb.append("===============\n");
            sb.append(string);
            sb.append("\n===============");
            sb.append("\033[0m\n\n");
            sb.append("http-get");
            sb.append("\n\033[01;30m");
            sb.append("--------");
            sb.append("\n\033[01;31m");
            sb.append(profile.getPreview().getClientSample(".http-get"));
            sb.append("\033[01;34m");
            sb.append(profile.getPreview().getServerSample(".http-get"));
            sb.append("\033[0m\n\n");
            sb.append("http-post");
            sb.append("\n\033[01;30m");
            sb.append("---------");
            sb.append("\n\033[01;31m");
            sb.append(profile.getPreview().getClientSample(".http-post"));
            sb.append("\033[01;34m");
            sb.append(profile.getPreview().getServerSample(".http-post"));
            sb.append("\033[0m\n\n");
            if (profile.getProgram(".http-stager") != null) {
                sb.append("http-stager");
                sb.append("\n\033[01;30m");
                sb.append("-----------");
                sb.append("\n\033[01;31m");
                sb.append(profile.getPreview().getClientSample(".http-stager"));
                sb.append("\033[01;34m");
                sb.append(profile.getPreview().getServerSample(".http-stager"));
                sb.append("\033[0m\n\n");
            }
            if (!"".equals(profile.getString(".dns_stager_subhost"))) {
                String str1 = profile.getString(".dns_stager_subhost");
                sb.append("\ndns staging host");
                sb.append("\n\033[01;30m");
                sb.append("----------------");
                sb.append("\n\033[01;31m");
                sb.append("aaa" + str1 + "<domain>");
                if (profile.hasString(".dns_stager_prepend")) {
                    sb.append(" = ");
                    sb.append(profile.getString(".dns_stager_prepend"));
                    sb.append("[...]");
                }
                sb.append("\n");
                sb.append("bdc" + str1 + "<domain>");
                sb.append("\033[0m\n");
            }
            System.out.println(sb.toString());
            if (lint.checkPost3x()) {
                CommonUtils.print_good("POST 3x check passed");
            }
            if (lint.checkProgramSizes(".http-get.server.output", 252, 1)) {
                CommonUtils.print_good(".http-get.server.output size is good");
            }
            if (lint.checkProgramSizes(".http-get.client", 252, 0)) {
                CommonUtils.print_good(".http-get.client size is good");
            }
            if (lint.checkProgramSizes(".http-post.client", 252, 0)) {
                CommonUtils.print_good(".http-post.client size is good");
            }
            lint.testuri(".http-get");
            lint.test(".http-get.client", ".metadata", 1, true);
            lint.test(".http-get.client", ".metadata", 100, true);
            lint.test(".http-get.client", ".metadata", 128, true);
            lint.test(".http-get.client", ".metadata", 256, true);
            lint.test(".http-get.server", ".output", 0, true);
            lint.test(".http-get.server", ".output", 1, true);
            lint.test(".http-get.server", ".output", 48248, true);
            lint.test(".http-get.server", ".output", 0x100000, true);
            lint.testuri(".http-post");
            lint.test(".http-post.client", ".id", 4);
            lint.test(".http-post.client", ".output", 0);
            lint.test(".http-post.client", ".output", 1);
            if (profile.shouldChunkPosts()) {
                CommonUtils.print_good(".http-post.client.output chunks results");
                lint.test(".http-post.client", ".output", 33);
                lint.test(".http-post.client", ".output", 128);
            } else {
                CommonUtils.print_good(".http-post.client.output POSTs results");
                lint.test(".http-post.client", ".output", 48248);
                lint.test(".http-post.client", ".output", 0x100000);
            }
            if (Profile.usesCookieBeacon(profile)) {
                CommonUtils.print_good("Beacon profile specifies an HTTP Cookie header. Will tell WinINet to allow this.");
            }
            if (profile.usesCookie(".http-stager.client")) {
                CommonUtils.print_good("Stager profile specifies an HTTP Cookie header. Will tell WinINet to allow this.");
            }
            if (Profile.usesHostBeacon(profile)) {
                CommonUtils.print_warn("Profile uses HTTP Host header for C&C. Will ignore Host header specified in payload config.");
            }
            lint.verb_compatability();
            lint.testuri_stager(".http-stager.uri_x86");
            lint.testuri_stager(".http-stager.uri_x64");
            String str = profile.getHeaders(".http-stager.client", "");
            if (str.length() > 303) {
                CommonUtils.print_error(".http-stager.client headers are " + str.length()
                        + " bytes. Max length is 303 bytes");
            }
            int i = (int) profile.getHTTPContentOffset(".http-stager.server");
            if (i > 0) {
                if ("".equals(profile.getString(".http-stager.uri_x86"))) {
                    CommonUtils.print_error(".http-stager.uri_x86 is not defined.");
                }
                if ("".equals(profile.getString(".http-stager.uri_x64"))) {
                    CommonUtils.print_error(".http-stager.uri_x64 is not defined.");
                }
            }
            if (i > 65535) {
                CommonUtils.print_error(".http-stager.server.output prepend value is " + i
                        + " bytes. Max is 65535. HTTP/S Stagers will crash");
            }
            lint.bounds(".sleeptime", 0, Integer.MAX_VALUE);
            lint.bounds(".jitter", 0, 99);
            lint.bounds(".maxdns", 1, 255);
            lint.bounds(".dns_max_txt", 4, 252);
            lint.bounds(".dns_ttl", 1, Integer.MAX_VALUE);
            int j = Integer.parseInt(profile.getString(".dns_max_txt"));
            if (j % 4 != 0) {
                CommonUtils.print_error(".dns_max_txt value (" + j
                        + ") must be divisible by four.");
            }
            lint.testuriCompare(".http-get.uri", ".http-post.uri");
            lint.boundsLen(".spawnto", 63);
            lint.boundsLen(".useragent", 128);
            lint.boundsLen(".pipename", 64);
            lint.boundsLen(".pipename_stager", 64);
            if (profile.getString(".pipename").equals(profile.getString(".pipename_stager"))) {
                CommonUtils.print_error(".pipename and .pipename_stager are the same. Make these different strings.");
            }
            lint.checkHeaders();
            lint.checkCollissions(".http-get.client");
            lint.checkCollissions(".http-get.server");
            lint.checkCollissions(".http-post.client");
            lint.checkCollissions(".http-post.server");
            lint.checkCollissions(".http-stager.client");
            lint.checkCollissions(".http-stager.server");
            if (!profile.option(".host_stage")) {
                CommonUtils.print_warn(".host_stage is FALSE. This will break staging over HTTP, HTTPS, and DNS!");
            } else {
                CommonUtils.print_opsec("[OPSEC] .host_stage is true. Your Beacon payload is available to anyone that connects to your server to request it. Are you OK with this? ");
            }
            if (!"rundll32.exe".equals(profile.getString(".spawnto"))) {
                CommonUtils.print_error(".spawnto is deprecated and has no effect. Set .post-ex.spawnto_x86 and .post-ex.spawnto_x64 instead.");
            }
            if (!"%windir%\\syswow64\\rundll32.exe".equals(profile.getString(".spawnto_x86"))) {
                CommonUtils.print_error(".spawnto_x86 is deprecated and has no effect. Set .post-ex.spawnto_x86 instead.");
            }
            if (!"%windir%\\sysnative\\rundll32.exe".equals(profile.getString(".spawnto_x64"))) {
                CommonUtils.print_error(".spawnto_x64 is deprecated and has no effect. Set .post-ex.spawnto_x64 instead.");
            }
            if (profile.option(".amsi_disable")) {
                CommonUtils.print_error(".amsi_disable is deprecated and has no effect. Set .post-ex.amsi_disable instead.");
            }
            lint.checkSpawnTo(".post-ex.spawnto_x86", "sysnative", "syswow64");
            lint.checkSpawnTo(".post-ex.spawnto_x64", "syswow64", "sysnative");
            if (profile.isFile(".code-signer.keystore")) {
                CommonUtils.print_good("Found code-signing configuration. Will sign executables and DLLs");
                lint.checkCodeSigner();
            } else {
                CommonUtils.print_warn(".code-signer.keystore is missing. Will not sign executables and DLLs");
            }
            if (profile.isFile(".https-certificate.keystore")) {
                CommonUtils.print_good("Found SSL certificate keystore");
                if (profile.getSSLPassword() == null || profile.getSSLPassword().length() == 0) {
                    CommonUtils.print_error(".https-certificate.password is empty. A password is required for your keystore.");
                } else if ("123456".equals(profile.getSSLPassword())) {
                    CommonUtils.print_warn(".https-certificate.password is the default '123456'. Is this really your keystore password?");
                }
            } else if (profile.regenerateKeystore()) {
                if (profile.getSSLKeystore() != null) {
                    CommonUtils.print_good("SSL certificate generation OK");
                }
            } else {
                CommonUtils.print_opsec("[OPSEC] .https-certificate options are missing [will use built-in SSL cert]");
            }
            lint.checkKeystore();
            lint.checkPE();
            if (!"".equals(profile.getString(".dns_stager_subhost"))) {
                String str1 = profile.getString(".dns_stager_subhost");
                if (!str1.endsWith(".")) {
                    CommonUtils.print_error(".dns_stager_subhost must end with a '.' (it's prepended to a parent domain)");
                }
                if (str1.length() > 32) {
                    CommonUtils.print_error(".dns_stager_subhost is too long. Keep it under 32 characters.");
                }
                if (str1.indexOf("..") > -1) {
                    CommonUtils.print_error(".dns_stager_subhost contains '..'. This is not valid in a hostname");
                }
            }
            if (!profile.option(".create_remote_thread")) {
                CommonUtils.print_warn(".create_remote_thread is deprecated and has no effect.");
            }
            if (!profile.option(".hijack_remote_thread")) {
                CommonUtils.print_warn(".hijack_remote_thread is deprecated and has no effect.");
            }
            lint.setupProcessInject("x86");
            lint.setupProcessInject("x64");
            lint.checkProcessInject();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
