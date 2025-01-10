package pe;

import common.CommonUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Iterator;

public class PEEditor {
    protected PEParser info = null;

    protected byte[] data;

    protected byte[] bdata = new byte[8];

    protected ByteBuffer buffer = null;

    protected int origch = 0;

    public byte[] getImage() {
        return this.data;
    }

    public void checkAssertions() {
        getInfo();
        int i = -8483;
        if ((this.origch & i) != 0) {
            CommonUtils.print_error("Beacon DLL has a Characteristic that's unexpected\n\tFlags: " + Integer.toBinaryString(i) + "\n\tOrigc: " + Integer.toBinaryString(this.origch));
        }
    }

    public boolean patchCode(byte[] arrby1, byte[] arrby2) {
        int i = this.info.get(".text.PointerToRawData");
        int j = i + this.info.get(".text.SizeOfRawData");
        int k = CommonUtils.indexOf(this.data, arrby1, i, j);
        if (k == -1) {
            return false;
        }
        System.arraycopy(arrby2, 0, this.data, k + 0, arrby2.length);
        /*for (int m = 0; m < arrby2.length; m++) {
            this.data[k + m] = arrby2[m];
        }*/
        return true;
    }

    public PEParser getInfo() {
        if (this.info == null) {
            this.info = PEParser.load(this.data);
            this.origch = getInfo().get("Characteristics");
        }
        return this.info;
    }

    public PEEditor(byte[] arrby) {
        this.data = arrby;
        this.buffer = ByteBuffer.wrap(this.bdata);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void updateChecksum() {
        long l = getInfo().checksum();
        setChecksum(l);
    }

    public void setModuleStomp(String string) {
        setCharacteristic(16384, true);
        setString(64, CommonUtils.randomData(64));
        setStringZ(64, string);
    }

    public void stompPE() {
        setCharacteristic(1, true);
    }

    public void insertRichHeader(byte[] arrby) {
        removeRichHeader();
        if (arrby.length == 0)
            return;
        long l = getInfo().get("e_lfanew");
        setValueAt("e_lfanew", l + arrby.length);
        byte[] arrby1 = Arrays.copyOfRange(this.data, 0, 128);
        byte[] arrby2 = Arrays.copyOfRange(this.data, (int) l, 1024 - arrby.length);
        byte[] arrby3 = CommonUtils.join(arrby1, arrby, arrby2);
        System.arraycopy(arrby3, 0, this.data, 0, 1024);
        this.info = PEParser.load(this.data);
    }

    public void removeRichHeader() {
        if (getInfo().getRichHeaderSize() == 0)
            return;
        long l = getInfo().get("e_lfanew");
        setValueAt("e_lfanew", 128L);
        byte[] arrby1 = Arrays.copyOfRange(this.data, 0, 128);
        byte[] arrby2 = Arrays.copyOfRange(this.data, (int) l, 1024);
        byte[] arrby3 = new byte[1024 - arrby1.length + arrby2.length];
        byte[] arrby4 = CommonUtils.join(arrby1, arrby2, arrby3);
        System.arraycopy(arrby4, 0, this.data, 0, 1024);
        this.info = PEParser.load(this.data);
    }

    public void setExportName(String string) {
        if (string.equals(getInfo().getString("Export.Name"))) {
            return;
        }
        int i = CommonUtils.bString(this.data).indexOf(string + Character.MIN_VALUE);
        if (i > 0) {
            int j = getInfo().getLocation("Export.Name");
            int k = getInfo().getPointerForLocation(0, i);
            setLong(j, k);
        } else {
            CommonUtils.print_warn("setExportName() failed. " + string
                    + " not found in strings table");
        }
    }

    public void setChecksum(long l) {
        setLong(getInfo().getLocation("CheckSum"), l);
    }

    public void setAddressOfEntryPoint(long l) {
        setValueAt("AddressOfEntryPoint", l);
    }

    public void setEntryPoint(long l) {
        long l2 = getInfo().get("AddressOfEntryPoint");
        setValueAt("LoaderFlags", l2);
        setCharacteristic(4096, true);
        setAddressOfEntryPoint(l);
    }

    protected void setString(int n, byte[] arrby) {
        for (int i = 0; i < arrby.length; i++)
            this.data[i + n] = arrby[i];
    }

    protected void setStringZ(int n, String string) {
        for (int i = 0; i < string.length(); i++)
            this.data[i + n] = (byte) string.charAt(i);
        this.data[n + string.length()] = 0;
    }

    public int getInt(int n) {
        this.buffer.clear();
        for (int i = 0; i < 4; i++)
            this.buffer.put(i, this.data[i + n]);
        return (int) getInfo().fixAddress(this.buffer.getInt());
    }

    protected void setLong(int n, long l) {
        this.buffer.clear();
        this.buffer.putLong(0, l);
        for (int i = 0; i < 4; i++)
            this.data[i + n] = this.bdata[i];
    }

    protected void setShort(int n, long l) {
        this.buffer.clear();
        this.buffer.putShort(0, (short) (int) l);
        for (int i = 0; i < 2; i++)
            this.data[i + n] = this.bdata[i];
    }

    protected void setCharacteristic(int n, boolean bl) {
        int i = getInfo().getLocation("Characteristics");
        if (bl) {
            this.origch = this.origch |= n;
        } else {
            this.origch = this.origch &= ~n;
        }
        if (bl == true) {
            this.origch |= n;
            // this.origch = this.origch |= n;
        } else {
            this.origch &= (n ^ 0xFFFFFFFF);
            // this.origch = this.origch &= ~n;
        }
        setShort(i, this.origch);
    }

    public void setCompileTime(String string) {
        setCompileTime(CommonUtils.parseDate(string, "dd MMM yyyy HH:mm:ss"));
    }

    public void setCompileTime(long l) {
        int i = getInfo().getLocation("TimeDateStamp");
        setLong(i, l / 1000L);
    }

    public void setValueAt(String string, long l) {
        int i = getInfo().getLocation(string);
        setLong(i, l);
    }

    public void setImageSize(long l) {
        int i = getInfo().getLocation("SizeOfImage");
        setLong(i, l);
    }

    public void setRWXHint(boolean bl) {
        setCharacteristic(32768, bl);
    }

    public void stomp(int n) {
        StringBuffer stringBuffer = new StringBuffer();
        while (this.data[n] != 0) {
            stringBuffer.append((char) this.data[n]);
            this.data[n] = 0;
            n++;
        }
        this.data[n] = 0;
    }

    public void mask(int n1, int n2, byte by) {
        for (int i = n1; i < n1 + n2; i++)
            this.data[i] = (byte) (this.data[i] ^ by);
    }

    protected void maskString(int n, byte by) {
        StringBuffer stringBuffer = new StringBuffer();
        while (this.data[n] != 0) {
            stringBuffer.append((char) this.data[n]);
            this.data[n] = (byte) (this.data[n] ^ by);
            n++;
        }
        this.data[n] = (byte) (this.data[n] ^ by);
        if (stringBuffer.toString().length() >= 63) {
            CommonUtils.print_error("String '" + stringBuffer.toString() + "' is >=63 characters! Obfuscate WILL crash");
        }
    }

    public void obfuscate(boolean bl) {
        if (bl) {
            _obfuscate();
            obfuscatePEHeader();
        } else {
            setLong(getInfo().getLocation("NumberOfSymbols"), 0L);
        }
    }

    public void obfuscatePEHeader() {
        int i = getInfo().get("e_lfanew");
        byte[] arrby = CommonUtils.randomData(i - 64);
        setString(64, arrby);
        i = getInfo().get("SizeOfHeaders") - getInfo().getLocation("HeaderSlack");
        arrby = CommonUtils.randomData(i - 4);
        setString(getInfo().getLocation("HeaderSlack"), arrby);
    }

    protected void _obfuscate() {
        byte b = -50;
        setLong(getInfo().getLocation("NumberOfSymbols"), b);
        Iterator iterator = getInfo().stringIterator();
        while (iterator.hasNext()) {
            int i = ((Integer) iterator.next()).intValue();
            maskString(i, b);
        }
    }

    public static void main(String[] arrstring) {
        byte[] arrby = CommonUtils.readFile(arrstring[0]);
        PEEditor pEEditor = new PEEditor(arrby);
        pEEditor.setCompileTime(System.currentTimeMillis() + 3600000L);
        pEEditor.setImageSize(512000L);
        pEEditor.setRWXHint(true);
        pEEditor.obfuscate(false);
        PEParser pEParser = PEParser.load(pEEditor.getImage());
        System.out.println(pEParser.toString());
    }
}
