package aggressor.dialogs;

import aggressor.AggressorClient;
import common.CommonUtils;
import dialog.DialogUtils;
import importers.ImportHandler;
import importers.Importer;

import java.io.File;
import java.util.HashMap;

public class ImportHosts implements ImportHandler, Runnable {

    protected int hosts = 0;

    protected int services = 0;

    protected AggressorClient client;

    protected String[] files;

    public ImportHosts(AggressorClient aggressorClient, String[] arrstring) {
        this.client = aggressorClient;
        this.files = arrstring;
        (new Thread(this, "import " + arrstring.length + " file(s)")).start();
    }

    public boolean runForFile(String string) throws Exception {
        File file = new File(string);
        for (Importer importer : Importer.importers(this)) {
            if (importer.process(file)) {
                return true;
            }
        }
        DialogUtils.showError("Import canceled: " + file.getName() + " is not a recognized format");
        return false;
    }

    @Override
    public void run() {
        for (int i = 0; i < files.length; i++) {
            try {
                if (!runForFile(this.files[i])) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finish();
    }

    public void finish() {
        if (this.hosts > 0) {
            this.client.getConnection().call("targets.push");
        }
        if (this.services > 0) {
            this.client.getConnection().call("services.push");
        }
        if (this.hosts == 1) {
            DialogUtils.showInfo("Imported " + this.hosts + " host");
        } else {
            DialogUtils.showInfo("Imported " + this.hosts + " hosts");
        }
    }

    @Override
    public void host(String string1, String string2, String string3, double d) {
        this.hosts++;
        HashMap hashMap = new HashMap();
        hashMap.put("address", string1);
        if (string2 != null) {
            hashMap.put("name", string2);
        }
        if (string3 != null) {
            hashMap.put("os", string3);
            if (d != 0.0D) {
                hashMap.put("version", d + "");
            }
        }
        this.client.getConnection().call("targets.update", CommonUtils.args(CommonUtils.TargetKey(hashMap), hashMap));
    }

    @Override
    public void service(String string1, String string2, String string3) {
        this.services++;
        HashMap hashMap = new HashMap();
        hashMap.put("address", string1);
        hashMap.put("port", string2);
        if (string3 != null) {
            hashMap.put("banner", string3);
        }
        this.client.getConnection().call("services.update", CommonUtils.args(CommonUtils.ServiceKey(hashMap), hashMap));
    }
}
