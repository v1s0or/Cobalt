package c2profile;

import cloudstrike.Response;
import common.CommonUtils;
import common.MudgeSanity;
import dialog.DialogUtils;
import encoders.Base64;
import encoders.Base64Url;
import encoders.MaskEncoder;
import encoders.NetBIOS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Program implements Serializable {
    protected boolean sealed = false;

    protected boolean posts = false;

    protected boolean cookie = false;

    protected boolean host = false;

    public static final int APPEND = 1;

    public static final int PREPEND = 2;

    public static final int BASE64 = 3;

    public static final int PRINT = 4;

    public static final int PARAMETER = 5;

    public static final int HEADER = 6;

    public static final int BUILD = 7;

    public static final int NETBIOS = 8;

    public static final int _PARAMETER = 9;

    public static final int _HEADER = 10;

    public static final int NETBIOSU = 11;

    public static final int URI_APPEND = 12;

    public static final int BASE64URL = 13;

    public static final int STRREP = 14;

    public static final int MASK = 15;

    public static final int _HOSTHEADER = 16;

    protected List<Statement> tsteps = new LinkedList();

    protected LinkedList<Statement> rsteps = new LinkedList();

    public boolean usesHost() {
        return this.host;
    }

    public boolean usesCookie() {
        return this.cookie;
    }

    public boolean isSealed() {
        return this.sealed;
    }

    public boolean postsData() {
        return this.posts;
    }

    public void addStep(String string1, String string2) {
        Statement statement = new Statement();
        statement.argument = string2;
        if (string2 != null)
            statement.alen = string2.length();
        if (string1.equals("append")) {
            statement.action = 1;
        } else if (string1.equals("prepend")) {
            statement.action = 2;
        } else if (string1.equals("base64")) {
            statement.action = 3;
        } else if (string1.equals("print")) {
            statement.action = 4;
            this.sealed = true;
            this.posts = true;
        } else if (string1.equals("parameter")) {
            statement.action = 5;
            this.sealed = true;
        } else if (string1.equals("header")) {
            statement.action = 6;
            this.sealed = true;
        } else if (string1.equals("build")) {
            statement.action = 7;
        } else if (string1.equals("netbios")) {
            statement.action = 8;
        } else if (string1.equals("!parameter")) {
            statement.action = 9;
        } else if (string1.equals("!header")) {
            statement.action = 10;
        } else if (string1.equals("!hostheader")) {
            statement.action = 16;
        } else if (string1.equals("netbiosu")) {
            statement.action = 11;
        } else if (string1.equals("uri-append")) {
            statement.action = 12;
            this.sealed = true;
        } else if (string1.equals("base64url")) {
            statement.action = 13;
        } else if (string1.equals("strrep")) {
            statement.action = 14;
        } else if (string1.equals("mask")) {
            statement.action = 15;
        } else {
            throw new RuntimeException("Invalid action: " + string1);
        }
        if (statement.action == 6 && string2 != null && "cookie".equals(string2.toLowerCase())) {
            this.cookie = true;
        } else if (statement.action == 10 && string2 != null && string2.toLowerCase().startsWith("cookie: ")) {
            this.cookie = true;
        } else if (statement.action == 6 && string2 != null && "host".equals(string2.toLowerCase())) {
            this.host = true;
        }
        this.tsteps.add(statement);
        this.rsteps.addFirst(statement);
    }

    public byte[] transform_binary(Profile profile) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        transform_binary(profile, dataOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public void transform_binary(Profile profile, DataOutputStream dataOutputStream) throws IOException {
        for (Statement statement : this.tsteps) {
            int i;
            switch (statement.action) {
                case 1:
                    dataOutputStream.writeInt(1);
                    dataOutputStream.writeInt(statement.alen);
                    for (i = 0; i < statement.argument.length(); i++) {
                        dataOutputStream.write((byte) statement.argument.charAt(i));
                    }
                    continue;
                case 2:
                    dataOutputStream.writeInt(2);
                    dataOutputStream.writeInt(statement.alen);
                    for (i = 0; i < statement.argument.length(); i++) {
                        dataOutputStream.write((byte) statement.argument.charAt(i));
                    }
                    continue;
                case 3:
                    dataOutputStream.writeInt(3);
                    continue;
                case 13:
                    dataOutputStream.writeInt(13);
                    continue;
                case 4:
                    dataOutputStream.writeInt(4);
                    continue;
                case 5:
                    dataOutputStream.writeInt(5);
                    dataOutputStream.writeInt(statement.alen);
                    for (i = 0; i < statement.argument.length(); i++) {
                        dataOutputStream.write((byte) statement.argument.charAt(i));
                    }
                    continue;
                case 6:
                    dataOutputStream.writeInt(6);
                    dataOutputStream.writeInt(statement.alen);
                    for (i = 0; i < statement.argument.length(); i++) {
                        dataOutputStream.write((byte) statement.argument.charAt(i));
                    }
                    continue;
                case 7:
                    dataOutputStream.writeInt(7);
                    if (statement.argument.endsWith("metadata")) {
                        dataOutputStream.writeInt(0);
                    } else if (statement.argument.endsWith("id")) {
                        dataOutputStream.writeInt(0);
                    } else if (statement.argument.endsWith("output")) {
                        dataOutputStream.writeInt(1);
                    } else {
                        System.err.println("UNKNOWN DATA ARGUMENT: " + statement.argument);
                    }
                    profile.getProgram(statement.argument).transform_binary(profile, dataOutputStream);
                    continue;
                case 8:
                    dataOutputStream.writeInt(8);
                    continue;
                case 9:
                    dataOutputStream.writeInt(9);
                    dataOutputStream.writeInt(statement.alen);
                    for (i = 0; i < statement.argument.length(); i++) {
                        dataOutputStream.write((byte) statement.argument.charAt(i));
                    }
                    continue;
                case 10:
                    dataOutputStream.writeInt(10);
                    dataOutputStream.writeInt(statement.alen);
                    for (i = 0; i < statement.argument.length(); i++) {
                        dataOutputStream.write((byte) statement.argument.charAt(i));
                    }
                    continue;
                case 16:
                    dataOutputStream.writeInt(16);
                    dataOutputStream.writeInt(statement.alen);
                    for (i = 0; i < statement.argument.length(); i++) {
                        dataOutputStream.write((byte) statement.argument.charAt(i));
                    }
                    continue;
                case 11:
                    dataOutputStream.writeInt(11);
                    continue;
                case 12:
                    dataOutputStream.writeInt(12);
                    continue;
                case 15:
                    break;
                default:
                    continue;
            }
            dataOutputStream.writeInt(15);
        }
    }

    public void transform(Profile profile, Response response, byte[] arrby) {
        SmartBuffer smartBuffer = new SmartBuffer();
        smartBuffer.append(arrby);
        transform(profile, response, smartBuffer);
    }

    public void transform(Profile profile, Response response, SmartBuffer paramSmartBuffer) {
        for (Statement statement : this.tsteps) {
            byte[] arrby;
            String str;
            switch (statement.action) {
                case 1:
                    paramSmartBuffer.append(toBytes(statement.argument));
                    continue;
                case 2:
                    paramSmartBuffer.prepend(toBytes(statement.argument));
                    continue;
                case 3:
                    str = Base64.encode(paramSmartBuffer.getBytes());
                    paramSmartBuffer.clear();
                    paramSmartBuffer.append(toBytes(str));
                    continue;
                case 13:
                    str = Base64Url.encode(paramSmartBuffer.getBytes());
                    paramSmartBuffer.clear();
                    paramSmartBuffer.append(toBytes(str));
                    continue;
                case 4:
                    arrby = paramSmartBuffer.getBytes();
                    response.data = new ByteArrayInputStream(arrby);
                    response.size = arrby.length;
                    response.offset = paramSmartBuffer.getDataOffset();
                    response.addHeader("Content-Length", arrby.length + "");
                    continue;
                case 5:
                    response.addParameter(statement.argument + "=" + toBinaryString(paramSmartBuffer.getBytes()));
                    continue;
                case 6:
                    response.addHeader(statement.argument, toBinaryString(paramSmartBuffer.getBytes()));
                    continue;
                case 7:
                    if (".http-post.client.output".equals(statement.argument)) {
                        SmartBuffer smartBuffer = new SmartBuffer();
                        smartBuffer.append(CommonUtils.randomData(16));
                        profile.getProgram(statement.argument).transform(profile, response, smartBuffer);
                        continue;
                    }
                    profile.getProgram(statement.argument).transform(profile, response, paramSmartBuffer);
                    continue;
                case 8:
                    str = NetBIOS.encode('a', paramSmartBuffer.getBytes());
                    paramSmartBuffer.clear();
                    paramSmartBuffer.append(toBytes(str));
                    continue;
                case 9:
                    response.addParameter(statement.argument);
                    continue;
                case 10:
                case 16:
                    response.addHeader(statement.argument);
                    continue;
                case 11:
                    str = NetBIOS.encode('A', paramSmartBuffer.getBytes());
                    paramSmartBuffer.clear();
                    paramSmartBuffer.append(toBytes(str));
                    continue;
                case 12:
                    response.uri = toBinaryString(paramSmartBuffer.getBytes());
                    continue;
                case 15:
                    str = toBinaryString(MaskEncoder.encode(paramSmartBuffer.getBytes()));
                    paramSmartBuffer.clear();
                    paramSmartBuffer.append(toBytes(str));
                    continue;
            }
            System.err.println("Unknown: " + statement);
        }
    }

    public byte[] recover_binary() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        for (Statement statement : this.rsteps) {
            int i;
            switch (statement.action) {
                case 1:
                    dataOutputStream.writeInt(1);
                    dataOutputStream.writeInt(statement.alen);
                    continue;
                case 2:
                    dataOutputStream.writeInt(2);
                    dataOutputStream.writeInt(statement.alen);
                    continue;
                case 3:
                    dataOutputStream.writeInt(3);
                    continue;
                case 13:
                    dataOutputStream.writeInt(13);
                    continue;
                case 4:
                    dataOutputStream.writeInt(4);
                    continue;
                case 5:
                    dataOutputStream.writeInt(5);
                    dataOutputStream.writeInt(statement.alen);
                    for (i = 0; i < statement.argument.length(); i++) {
                        dataOutputStream.write(statement.argument.charAt(i));
                    }
                    continue;
                case 6:
                    dataOutputStream.writeInt(5);
                    dataOutputStream.writeInt(statement.alen);
                    for (i = 0; i < statement.argument.length(); i++) {
                        dataOutputStream.write(statement.argument.charAt(i));
                    }
                    continue;
                case 7:
                    continue;
                case 8:
                    dataOutputStream.writeInt(8);
                    continue;
                case 9:
                case 10:
                case 16:
                    continue;
                case 11:
                    dataOutputStream.writeInt(11);
                    continue;
                case 12:
                    continue;
                case 15:
                    break;
                default:
                    continue;
            }
            dataOutputStream.writeInt(15);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public List collissions(Profile profile) {
        HashSet hashSet = new HashSet();
        HashMap hashMap = new HashMap();
        LinkedList linkedList = new LinkedList();
        collissions(profile, null, hashSet, hashMap, linkedList);
        return linkedList;
    }

    private static String[] split(String string) {
        String[] arrstring = string.split("[:=]");
        if (arrstring.length != 2) {
            return new String[]{string, ""};
        }
        arrstring[1] = arrstring[1].trim();
        return arrstring;
    }

    public void collissions(Profile profile, String string, Set paramSet, Map map, List list) {
        Iterator iterator = this.tsteps.iterator();
        while (iterator.hasNext()) {
            String[] arrstring;
            String str1 = null;
            String str2 = null;
            Statement statement = (Statement) iterator.next();
            switch (statement.action) {
                case 12:
                    str1 = "uri-append";
                    str2 = "block '" + string + "'";
                    break;
                case 4:
                    str1 = "print";
                    str2 = "block '" + string + "'";
                    break;
                case 9:
                    arrstring = split(statement.argument);
                    str1 = "parameter " + arrstring[0];
                    str2 = "value '" + arrstring[1] + "'";
                    break;
                case 5:
                    str1 = "parameter " + statement.argument;
                    str2 = "block '" + string + "'";
                    break;
                case 10:
                case 16:
                    arrstring = split(statement.argument);
                    str1 = "header " + arrstring[0];
                    str2 = "value '" + arrstring[1] + "'";
                    break;
                case 6:
                    str1 = "header " + statement.argument;
                    str2 = "block '" + string + "'";
                    break;
                case 7:
                    profile.getProgram(statement.argument).collissions(profile, statement.argument, paramSet, map, list);
                    break;
            }
            if (str1 == null)
                continue;
            if (paramSet.contains(str1)) {
                list.add(str1 + ": " + str2 + ", " + map.get(str1));
                continue;
            }
            paramSet.add(str1);
            map.put(str1, str2);
        }
    }

    private static final String toBinaryString(byte[] arrby) {
        try {
            return new String(arrby, "ISO8859-1");
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            return "";
        }
    }

    public static final byte[] toBytes(String string) {
        int n = string.length();
        byte[] arrby = new byte[n];
        for (int i = 0; i < n; i++) {
            arrby[i] = (byte) string.charAt(i);
        }
        return arrby;
    }

    public String recover(Map map1, Map map2, String string1, String string2) {
        String str = "";
        for (Statement statement : this.rsteps) {
            switch (statement.action) {
                case 1:
                    try {
                        str = str.substring(0, str.length() - statement.alen);
                        continue;
                    } catch (RuntimeException runtimeException) {
                        MudgeSanity.logException("substr('" + str.replaceAll("\\P{Print}", ".") + "', 0, " + str.length() + " - " + statement.alen + ")", runtimeException, false);
                        return "";
                    }
                case 2:
                    try {
                        str = str.substring(statement.alen, str.length());
                        continue;
                    } catch (RuntimeException runtimeException) {
                        MudgeSanity.logException("substr('" + str.replaceAll("\\P{Print}", ".") + "', " + statement.alen + ", " + str.length() + ")", runtimeException, false);
                        return "";
                    }
                case 3:
                    try {
                        str = toBinaryString(Base64.decode(str));
                        continue;
                    } catch (RuntimeException runtimeException) {
                        MudgeSanity.logException("base64 decode: " + str, runtimeException, true);
                        return "";
                    }
                case 13:
                    try {
                        str = toBinaryString(Base64Url.decode(str));
                        continue;
                    } catch (RuntimeException runtimeException) {
                        MudgeSanity.logException("base64url decode: " + str, runtimeException, true);
                        return "";
                    }
                case 4:
                    str = string1;
                    continue;
                case 5:
                    str = DialogUtils.string(map2, statement.argument);
                    continue;
                case 6:
                    str = CommonUtils.getCaseInsensitive(map1, statement.argument, "");
                    continue;
                case 7:
                    continue;
                case 8:
                    str = toBinaryString(NetBIOS.decode('a', str));
                    continue;
                case 9:
                case 10:
                case 16:
                    continue;
                case 11:
                    str = toBinaryString(NetBIOS.decode('A', str));
                    continue;
                case 12:
                    str = string2;
                    continue;
                case 15:
                    str = toBinaryString(MaskEncoder.decode(CommonUtils.toBytes(str)));
                    continue;
            }
            System.err.println("Unknown: " + statement);
        }
        return str;
    }

    public byte[] transformData(byte[] arrby) {
        if (this.tsteps.size() == 0) {
            return arrby;
        }
        SmartBuffer smartBuffer = new SmartBuffer();
        smartBuffer.append(arrby);
        for (Statement statement : this.tsteps) {
            String str2;
            String str1;
            switch (statement.action) {
                case 1:
                    smartBuffer.append(toBytes(statement.argument));
                    continue;
                case 2:
                    smartBuffer.prepend(toBytes(statement.argument));
                    continue;
                case 14:
                    str1 = statement.argument.substring(0, statement.argument.length() / 2);
                    str2 = statement.argument.substring(statement.argument.length() / 2);
                    smartBuffer.strrep(str1, str2);
                    continue;
            }
            System.err.println("Unknown: " + statement);
        }
        return smartBuffer.getBytes();
    }

    public byte[] getPrependedData() throws IOException {
        if (this.tsteps.size() == 0) {
            return new byte[0];
        }
        SmartBuffer smartBuffer = new SmartBuffer();
        for (Statement statement : this.tsteps) {
            switch (statement.action) {
                case 2:
                    smartBuffer.prepend(toBytes(statement.argument));
                    break;
            }
        }
        return smartBuffer.getBytes();
    }

    public byte[] getAppendedData() throws IOException {
        if (this.tsteps.size() == 0) {
            return new byte[0];
        }
        SmartBuffer smartBuffer = new SmartBuffer();
        for (Statement statement : this.tsteps) {
            switch (statement.action) {
                case 1:
                    smartBuffer.append(toBytes(statement.argument));
                    break;
            }
        }
        return smartBuffer.getBytes();
    }

    public static final class Statement implements Serializable {
        public String argument = "";

        public int action = 0;

        public int alen = 0;

        public String toString() {
            return "(" + this.action + ":" + this.argument + ")";
        }
    }
}
