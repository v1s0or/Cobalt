package common;

import dns.SleeveSecurity;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SleeveUtil {

    protected SleeveSecurity crypto;

    public SleeveUtil() {
        byte[] arrby = CommonUtils.readFile("resourcekey.bin");
        arrby = Arrays.copyOfRange(arrby, 0, 16);
        this.crypto = new SleeveSecurity();
        this.crypto.registerKey(arrby);
        process();
    }

    public List walk() {
        LinkedList linkedList = new LinkedList();
        File file = new File("resources/");
        String[] arrstring = file.list();
        Arrays.sort(arrstring);
        for (byte b = 0; b < arrstring.length; b++) {
            if (!arrstring[b].startsWith("artifact") && !arrstring[b].startsWith("covertvpn") && !arrstring[b].startsWith("dropper") && (arrstring[b].endsWith(".exe") || arrstring[b].endsWith(".dll")))
                linkedList.add(arrstring[b]);
        }
        return linkedList;
    }

    public void processFile(String string) {
        byte[] arrby1 = CommonUtils.readFile("resources/" + string);
        byte[] arrby2 = this.crypto.encrypt(arrby1);
        CommonUtils.writeToFile(new File("bin/sleeve/" + string), arrby2);
        CommonUtils.print_stat("Wrote '" + string + "' " + arrby2.length + " bytes");
        (new File("bin/resources/" + string)).delete();
    }

    public void process() {
        (new File("bin/sleeve")).mkdirs();
        Iterator iterator = walk().iterator();
        while (iterator.hasNext())
            processFile((String) iterator.next());
    }

    public static void main(String[] arrstring) {
        new SleeveUtil();
    }
}
