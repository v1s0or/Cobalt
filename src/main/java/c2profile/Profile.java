package c2profile;

import beacon.BeaconPayload;
import cloudstrike.Response;
import common.CodeSigner;
import common.CommonUtils;
import common.MudgeSanity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Profile implements Serializable {
    protected Map data = new HashMap();

    protected Preview preview = null;

    protected Map datal = new HashMap();

    protected Map variants = new HashMap();

    public String[] getVariants() {
        LinkedList linkedList = new LinkedList(this.variants.keySet());
        Collections.sort(linkedList);
        return CommonUtils.toArray(linkedList);
    }

    public Profile getVariantProfile(String string) {
        if (string == null || "".equals(string)) {
            return this;
        }
        if (!this.variants.containsKey(string)) {
            CommonUtils.print_warn("Profile variant '" + string + "' does not exist. Degrading to normal profile state.");
            return this;
        }
        ProfileVariant profileVariant1 = (ProfileVariant) this.variants.get("default");
        ProfileVariant profileVariant2 = (ProfileVariant) this.variants.get(string);
        Profile profile = new Profile();
        profile.data.putAll(profileVariant1.data);
        profile.data.putAll(profileVariant2.data);
        profile.datal.putAll(profileVariant1.datal);
        profile.datal.putAll(profileVariant2.datal);
        return profile;
    }

    public void activateVariant(String string) {
        if (!this.variants.containsKey(string)) {
            this.variants.put(string, new ProfileVariant());
        }
        ProfileVariant profileVariant = (ProfileVariant) this.variants.get(string);
        this.data = profileVariant.data;
        this.datal = profileVariant.datal;
    }

    public Profile() {
        activateVariant("default");
    }

    public void setList(String string, List list) {
        this.datal.put(string, list);
    }

    public void addList(String string) {
        this.datal.put(string, new LinkedList());
    }

    public void addParameter(String string, Object object) {
        this.data.put(string, object);
    }

    public void logToString(String string1, String string2) {
        String str = string1 + ".log.string";
        if (!this.data.containsKey(str)) {
            this.data.put(str, new LinkedList());
        }
        LinkedList linkedList = (LinkedList) this.data.get(str);
        linkedList.add(string2.trim());
    }

    public String getToStringLog(String string) {
        String str = string + ".log.string";
        if (!this.data.containsKey(str))
            return null;
        LinkedList linkedList = new LinkedList((LinkedList) this.data.get(str));
        return CommonUtils.join(linkedList, "\n");
    }

    public void addToString(String string, byte[] arrby) {
        String str = string + ".string";
        if (!this.data.containsKey(str)) {
            this.data.put(str, new SmartBuffer());
        }
        SmartBuffer smartBuffer = (SmartBuffer) this.data.get(str);
        smartBuffer.append(arrby);
    }

    public SmartBuffer getToString(String string) {
        String str = string + ".string";
        if (!this.data.containsKey(str)) {
            return new SmartBuffer();
        }
        return (SmartBuffer) this.data.get(str);
    }

    public static boolean usesCookieBeacon(Profile profile) {
        return (profile.usesCookie(".http-get.client")
                || profile.usesCookie(".http-post.client")
                || profile.usesCookie(".http-get.client.metadata")
                || profile.usesCookie(".http-post.client.id")
                || profile.usesCookie(".http-post.client.output"));
    }

    public static boolean usesHostBeacon(Profile profile) {
        return (profile.usesHost(".http-get.client.metadata")
                || profile.usesHost(".http-post.client.id")
                || profile.usesHost(".http-post.client.output"));
    }

    public boolean usesCookie(String string) {
        Program program = getProgram(string);
        return program != null && program.usesCookie();
    }

    public boolean usesHost(String string) {
        Program program = getProgram(string);
        return program != null && program.usesHost();
    }

    public boolean isSealed(String string) {
        Program program = getProgram(string);
        return program != null && program.isSealed();
    }

    public Preview getPreview() {
        synchronized (this) {
            if (this.preview == null) {
                this.preview = new Preview(this);
            }
            return this.preview;
        }
    }

    public void addCommand(String string1, String string2, String string3) {
        if (this.datal.containsKey(string1)) {
            LinkedList linkedList = (LinkedList) this.datal.get(string1);
            if (string3 == null) {
                linkedList.add(string2);
            } else {
                linkedList.add(string2 + " " + string3);
            }
            return;
        }
        if (!this.data.containsKey(string1))
            this.data.put(string1, new Program());
        Program program = getProgram(string1);
        program.addStep(string2, string3);
    }

    public void apply(String string, Response response, byte[] arrby) {
        Program program = getProgram(string);
        if (program != null)
            program.transform(this, response, arrby);
    }

    public String recover(String string1, Map map1, Map map2, String string2, String string3) {
        Program program = getProgram(string1);
        return program.recover(map1, map2, string2, string3);
    }

    public Program getProgram(String string) {
        return (Program) this.data.get(string);
    }

    public byte[] apply_binary(String string) throws IOException {
        Program program = getProgram(string);
        return program.transform_binary(this);
    }

    public byte[] recover_binary(String string) throws IOException {
        Program program = getProgram(string);
        return program.recover_binary();
    }

    public int size(String string, int n) throws IOException {
        byte[] arrby = new byte[n];
        Response response = new Response("200 OK", null, (InputStream) null);
        apply(string, response, arrby);
        if (response.data != null) {
            return response.data.available();
        }
        return 0;
    }

    public boolean hasString(String string) {
        return this.data.containsKey(string);
    }

    public List getList(String string) {
        return (LinkedList) this.datal.get(string);
    }

    public String getString(String string) {
        return this.data.get(string) + "";
    }

    public boolean option(String string) {
        return getString(string).equals("true");
    }

    public byte[] getByteArray(String string) throws IOException {
        return (byte[]) this.data.get(string);
    }

    public File getFile(String string) {
        return new File(getString(string));
    }

    public CodeSigner getCodeSigner() {
        return new CodeSigner(this);
    }

    public boolean isFile(String string) {
        return "".equals(getString(string)) ? false : getFile(string).exists();
    }

    public boolean posts(String string) {
        Program program = getProgram(string);
        if (program == null) {
            return false;
        }
        return program.postsData();
    }

    public boolean shouldChunkPosts() {
        return !posts(".http-post.client.output");
    }

    public boolean exerciseCFGCaution() {
        return !("".equals(getString(".stage.module_x86")) && "".equals(getString(".stage.module_x64")));
    }

    public int getInt(String string) {
        return Integer.parseInt(getString(string));
    }

    protected String certDescription() {
        return "CN=" + getString(".https-certificate.CN") + ", OU=" + getString(".https-certificate.OU") + ", O=" + getString(".https-certificate.O") + ", L=" + getString(".https-certificate.L") + ", ST=" + getString(".https-certificate.ST") + ", C=" + getString(".https-certificate.C");
    }

    public boolean regenerateKeystore() {
        return !("CN=, OU=, O=, L=, ST=, C=".equals(certDescription()) && getInt(".https-certificate.validity") == 3650);
    }

    public String getSSLPassword() {
        return getString(".https-certificate.password");
    }

    public boolean hasValidSSL() {
        return isFile(".https-certificate.keystore");
    }

    public InputStream getSSLKeystore() {
        try {
            if (isFile(".https-certificate.keystore"))
                return new FileInputStream(getFile(".https-certificate.keystore"));
            if (!regenerateKeystore())
                return null;
            File file = new File("./ssl" + System.currentTimeMillis() + ".store");
            file.deleteOnExit();
            LinkedList<String> linkedList = new LinkedList();
            linkedList.add("keytool");
            linkedList.add("-keystore");
            linkedList.add(file.getAbsolutePath());
            linkedList.add("-storepass");
            linkedList.add("123456");
            linkedList.add("-keypass");
            linkedList.add("123456");
            linkedList.add("-genkey");
            linkedList.add("-keyalg");
            linkedList.add("RSA");
            linkedList.add("-alias");
            linkedList.add("cobaltstrike");
            linkedList.add("-dname");
            linkedList.add(certDescription());
            linkedList.add("-validity");
            linkedList.add(getString(".https-certificate.validity"));
            ProcessBuilder processBuilder = new ProcessBuilder(linkedList);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            process.waitFor();
            return new FileInputStream(file);
        } catch (Exception exception) {
            CommonUtils.print_error("SSL certificate generation failed:\n\t" + exception.getMessage());
            return null;
        }
    }

    public byte[] getPrependedData(String string) throws IOException {
        Program program = getProgram(string);
        if (program == null) {
            return new byte[0];
        }
        return program.getPrependedData();
    }

    public byte[] getAppendedData(String string) throws IOException {
        Program program = getProgram(string);
        if (program == null) {
            return new byte[0];
        }
        return program.getAppendedData();
    }

    public long getHTTPContentOffset(String string) {
        Response response = new Response("200 OK", null, (InputStream) null);
        byte[] arrby = CommonUtils.randomData(16);
        apply(string, response, arrby);
        return response.offset;
    }

    public Map getHeadersAsMap(String string) {
        Response response = new Response("200 OK", null, (InputStream) null);
        apply(string, response, new byte[0]);
        return response.header;
    }

    protected boolean hasHeader(Map<String, String> map, String string) {
        for (String str : map.keySet()) {
            if (string.toLowerCase().equals(str.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public String getHeaders(String string1, String string2) {
        Response response = new Response("200 OK", null, (InputStream) null);
        byte[] arrby = CommonUtils.randomData(16);
        apply(string1, response, arrby);
        StringBuffer stringBuffer = new StringBuffer();
        if (!hasHeader(response.header, "User-Agent"))
            response.header.put("User-Agent", BeaconPayload.randua(this));
        if (!"".equals(string2) && !hasHeader(response.header, "Host"))
            response.header.put("Host", string2);
        for (Map.Entry entry : response.header.entrySet()) {
            String str1 = entry.getKey() + "";
            String str2 = entry.getValue() + "";
            if (!"".equals(string2) && str1.toLowerCase().equals("host")) {
                stringBuffer.append(str1 + ": " + string2 + "\r\n");
                continue;
            }
            stringBuffer.append(str1 + ": " + str2 + "\r\n");
        }
        return stringBuffer.toString();
    }

    public String getQueryString(String string) {
        Response response = new Response("200 OK", null, (InputStream) null);
        byte[] arrby = CommonUtils.randomData(16);
        apply(string, response, arrby);
        StringBuffer stringBuffer = new StringBuffer();
        Iterator iterator = response.params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String string2 = entry.getKey() + "";
            String string3 = entry.getValue() + "";
            try {
                entry.setValue(URLEncoder.encode(entry.getValue() + "", "UTF-8"));
            } catch (Exception exception) {
                MudgeSanity.logException("url encoding: " + entry, exception, false);
            }
            stringBuffer.append(string2 + "=" + string3);
            if (iterator.hasNext()) {
                stringBuffer.append("&");
            }
        }
        /*for (Map.Entry entry : response.params.entrySet()) {
            String str1 = entry.getKey() + "";
            String str2 = entry.getValue() + "";
            try {
                entry.setValue(URLEncoder.encode(entry.getValue() + "", "UTF-8"));
            } catch (Exception exception) {
                MudgeSanity.logException("url encoding: " + entry, exception, false);
            }
            stringBuffer.append(str1 + "=" + str2);
            if (null.hasNext())
                stringBuffer.append("&");
        }*/
        if (stringBuffer.length() == 0) {
            return "";
        }
        return "?" + stringBuffer;
    }
}
