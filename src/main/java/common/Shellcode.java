package common;

public class Shellcode {

    public static byte[] BindProtocolPackage(byte[] arrby) {
        Packer packer = new Packer();
        packer.little();
        packer.addInt(arrby.length);
        packer.addInt(arrby.length);
        packer.addString(arrby, arrby.length);
        return packer.getBytes();
    }
}
