package importers;

import common.CommonUtils;

import java.io.File;

public class TestImporters implements ImportHandler {

    public TestImporters(File file) throws Exception {
        go(file);
    }

    public void go(File file) throws Exception {
        for (Importer importer : Importer.importers(this)) {
            if (importer.process(file)) {
                break;
            }
        }
        CommonUtils.print_info("Done!");
    }

    public void host(String string1, String string2, String string3, double d) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("host: " + string1);
        if (string2 != null) {
            stringBuffer.append(" / " + string2);
        }
        if (string3 != null) {
            stringBuffer.append(" (" + string3 + " " + d + ")");
        }
        CommonUtils.print_good(stringBuffer.toString());
    }

    public void service(String string1, String string2, String string3) {
        if (string3 != null) {
            CommonUtils.print_info(string1 + ":" + string2 + " - " + string3);
        } else {
            CommonUtils.print_info(string1 + ":" + string2);
        }
    }

    public static void main(String[] arrstring) throws Exception {
        File file = new File(arrstring[0]);
        new TestImporters(file);
    }
}
