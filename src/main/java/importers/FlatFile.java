package importers;

import common.CommonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class FlatFile extends Importer {
    public FlatFile(ImportHandler importHandler) {
        super(importHandler);
    }

    public boolean isHostAndPort(String string) {
        String[] arrstring = string.split(":");
        if (arrstring.length == 2 && CommonUtils.isIP(arrstring[0])) {
            host(arrstring[0], null, null, 0.0D);
            service(arrstring[0], arrstring[1], null);
            return true;
        }
        return false;
    }

    public boolean parse(File file) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        for (String str = bufferedReader.readLine(); str != null; str = bufferedReader.readLine()) {
            str = str.trim();
            if (!str.startsWith("# "))
                if (CommonUtils.isIP(str)) {
                    host(str, null, null, 0.0D);
                } else if (!isHostAndPort(str) && !"".equals(str)) {
                    bufferedReader.close();
                    return false;
                }
        }
        return true;
    }
}
