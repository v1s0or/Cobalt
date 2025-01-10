package common;

import java.util.Arrays;

public class ByteIterator {

    protected byte[] buffer;

    protected int index = 0;

    public ByteIterator(byte[] arrby) {
        this.buffer = arrby;
    }

    public boolean hasNext() {
        return (this.index < this.buffer.length);
    }

    public byte[] next(long l) {
        int i = (int) l;
        if (this.index >= this.buffer.length) {
            return new byte[0];
        }
        if (this.index + i < this.buffer.length) {
            byte[] arrby1 = Arrays.copyOfRange(this.buffer, this.index, this.index + i);
            this.index += i;
            return arrby1;
        }
        byte[] arrby = Arrays.copyOfRange(this.buffer, this.index, this.buffer.length);
        this.index = this.buffer.length;
        return arrby;
    }

    public void reset() {
        this.index = 0;
    }

    public static void test1() {
        byte[] arrby1 = CommonUtils.randomData(CommonUtils.rand(0xA00000));
        CommonUtils.print_warn("Garbage is: " + arrby1.length);
        String str1 = CommonUtils.toHex(CommonUtils.MD5(arrby1));
        ByteIterator byteIterator = new ByteIterator(arrby1);
        byte[] arrby2 = new byte[0];
        for (int b = 0; byteIterator.hasNext(); b++) {
            byte[] arrby = byteIterator.next(0x100000L);
            CommonUtils.print_warn("Chunk " + b + ": " + arrby.length);
            arrby2 = CommonUtils.join(arrby2, arrby);
        }
        String str2 = CommonUtils.toHex(CommonUtils.MD5(arrby2));
        CommonUtils.print_info("MD5 (before): " + str1);
        CommonUtils.print_info("MD5  (after): " + str2);
        if (!str1.equals(str2)) {
            CommonUtils.print_error("FAILED!");
            System.exit(0);
        }
    }

    public static void main(String[] arrstring) {
        for (int b = 0; b < 8192; b++) {
            test1();
        }
        CommonUtils.print_good("PASSED!");
    }
}
