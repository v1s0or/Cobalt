package encoders;

import common.CommonUtils;
import common.Packer;

public class Transforms {
    public static byte[] toVeil(byte[] arrby) {
        Packer packer = new Packer();
        for (int i = 0; i < arrby.length; i++) {
            packer.addString("\\x");
            String str = Integer.toString(arrby[i] & 0xFF, 16);
            if (str.length() == 2) {
                packer.addString(str);
            } else {
                packer.addString("0" + str);
            }
        }
        return packer.getBytes();
    }

    public static String toArray(byte[] arrby) {
        Packer packer = new Packer();
        for (int i = 0; i < arrby.length; i++) {
            packer.addString("0x");
            String str = Integer.toString(arrby[i] & 0xFF, 16);
            if (str.length() == 2) {
                packer.addString(str);
            } else {
                packer.addString("0" + str);
            }
            if (i < arrby.length - 1)
                packer.addString(", ");
        }
        return CommonUtils.bString(packer.getBytes());
    }

    public static byte[] toC(byte[] arrby) {
        Packer packer = new Packer();
        packer.addString("/* length: " + arrby.length + " bytes */\n");
        packer.addString("unsigned char buf[] = \"" + CommonUtils.bString(toVeil(arrby)) + "\";\n");
        return packer.getBytes();
    }

    public static byte[] toPerl(byte[] arrby) {
        Packer packer = new Packer();
        packer.addString("# length: " + arrby.length + " bytes\n");
        packer.addString("$buf = \"" + CommonUtils.bString(toVeil(arrby)) + "\";\n");
        return packer.getBytes();
    }

    public static byte[] toPython(byte[] arrby) {
        Packer packer = new Packer();
        packer.addString("# length: " + arrby.length + " bytes\n");
        packer.addString("buf = \"" + CommonUtils.bString(toVeil(arrby)) + "\"\n");
        return packer.getBytes();
    }

    public static byte[] toJava(byte[] arrby) {
        Packer packer = new Packer();
        packer.addString("/* length: " + arrby.length + " bytes */\n");
        packer.addString("byte buf[] = new byte[] { " + toArray(arrby) + " };\n");
        return packer.getBytes();
    }

    public static byte[] toCSharp(byte[] arrby) {
        Packer packer = new Packer();
        packer.addString("/* length: " + arrby.length + " bytes */\n");
        packer.addString("byte[] buf = new byte[" + arrby.length + "] { " + toArray(arrby) + " };\n");
        return packer.getBytes();
    }

    public static String toVBA(byte[] arrby) {
        StringBuffer stringBuffer = new StringBuffer(arrby.length * 10);
        stringBuffer.append("Array(");
        for (int i = 0; i < arrby.length; i++) {
            stringBuffer.append(arrby[i]);
            if (i > 0 && i % 40 == 0 && (i + 1 < arrby.length)) {
                stringBuffer.append(", _\n");
            } else if (i + 1 < arrby.length) {
                stringBuffer.append(",");
            }
        }
        stringBuffer.append(")");
        return stringBuffer.toString();
    }
}
