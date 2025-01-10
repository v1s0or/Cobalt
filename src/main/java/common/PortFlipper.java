package common;

import java.util.Iterator;
import java.util.LinkedList;

public class PortFlipper {

    protected String ports;

    protected boolean hasError;

    protected String description;

    public boolean hasError() {
        return this.hasError;
    }

    public String getError() {
        return this.description;
    }

    public int check(int n) {
        if (n < 0 || n > 65535) {
            this.hasError = true;
            this.description = "Invalid port value '" + n + "'";
        }
        return n;
    }

    public LinkedList parse() {
        LinkedList linkedList = new LinkedList();
        String[] arrstring = this.ports.split(",");
        for (byte b = 0; b < arrstring.length; b++) {
            if (CommonUtils.isNumber(arrstring[b])) {
                linkedList.add(Integer.valueOf(check(CommonUtils.toNumber(arrstring[b], -1))));
            } else if (arrstring[b].matches("\\d+-\\d+")) {
                String[] strs1 = arrstring[b].split("-");
                int i = check(CommonUtils.toNumber(strs1[0], 0));
                int j = check(CommonUtils.toNumber(strs1[1], 0));
                while (i <= j) {
                    linkedList.add(Integer.valueOf(i));
                    i++;
                }
            } else {
                this.description = "Invalid port or range '" + arrstring[b] + "'";
                this.hasError = true;
            }
        }
        return linkedList;
    }

    public PortFlipper(String string) {
        this.ports = string;
    }

    private static void flip(byte[] arrby, int n) {
        int i = n / 8;
        int j = n % 8;
        arrby[i] = (byte) (arrby[i] + (1 << j));
    }

    public Iterator iterator() {
        return parse().iterator();
    }

    public byte[] getMask() {
        byte[] arrby = new byte[8192];
        Iterator iterator = iterator();
        while (iterator.hasNext())
            flip(arrby, ((Integer) iterator.next()).intValue());
        return arrby;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        Iterator iterator = iterator();
        while (iterator.hasNext()) {
            stringBuffer.append(iterator.next());
            if (iterator.hasNext())
                stringBuffer.append(", ");
        }
        return stringBuffer.toString();
    }
}
