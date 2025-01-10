package c2profile;

import common.CommonUtils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

public class SmartBuffer implements Serializable {

    protected LinkedList<byte[]> data = new LinkedList();

    protected int prepend_len = 0;

    public SmartBuffer copy() {
        SmartBuffer smartBuffer = new SmartBuffer();
        smartBuffer.data = new LinkedList(this.data);
        smartBuffer.prepend_len = this.prepend_len;
        return smartBuffer;
    }

    public int getDataOffset() {
        return this.prepend_len;
    }

    public void append(byte[] arrby) {
        this.data.add(arrby);
    }

    public void prepend(byte[] arrby) {
        this.data.add(0, arrby);
        this.prepend_len += arrby.length;
    }

    public void clear() {
        this.prepend_len = 0;
        this.data.clear();
    }

    public void strrep(String string1, String string2) {
        Iterator iterator = (new LinkedList(this.data)).iterator();
        clear();
        while (iterator.hasNext()) {
            byte[] arrby = (byte[]) iterator.next();
            if (arrby.length >= string1.length()) {
                append(CommonUtils.strrep(arrby, string1, string2));
                continue;
            }
            append(arrby);
        }
    }

    public Iterator iterator() {
        return this.data.iterator();
    }

    public byte[] getBytes() {
        if (this.data.size() == 1)
            return (byte[]) this.data.getFirst();
        if (this.data.size() == 0)
            return new byte[0];
        byte[] arrby = new byte[size()];
        int i = 0;
        for (byte[] arrby1 : this.data) {
            System.arraycopy(arrby1, 0, arrby, i, arrby1.length);
            i += arrby1.length;
        }
        return arrby;
    }

    public int size() {
        if (this.data.size() == 1)
            return ((byte[]) this.data.getFirst()).length;
        if (this.data.size() == 0)
            return 0;
        int i = 0;
        for (byte[] arrby : this.data)
            i += arrby.length;
        return i;
    }

    public String toString() {
        byte[] arrby = getBytes();
        StringBuffer stringBuffer = new StringBuffer();
        for (byte b = 0; b < arrby.length; b++) {
            char c = (char) arrby[b];
            if (Character.isDigit(c) || Character.isLetter(c) || Character.isWhitespace(c) || c == '%' || c == '!' || c == '.') {
                stringBuffer.append(c);
            } else {
                stringBuffer.append("[" + arrby[b] + "]");
            }
        }
        return stringBuffer.toString();
    }
}
