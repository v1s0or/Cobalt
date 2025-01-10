package common;

import dialog.DialogUtils;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

public class DownloadFiles implements DownloadNotify {

    protected Iterator queue = null;

    protected File dest = null;

    protected TeamQueue conn = null;

    public void startNextDownload() {
        if (!this.queue.hasNext()) {
            DialogUtils.showInfo("Download complete!");
            return;
        }
        Map map = (Map) this.queue.next();
        String str1 = (String) map.get("lpath");
        String str2 = (String) map.get("name");
        (new DownloadFile(this.conn, str1, CommonUtils.SafeFile(this.dest, str2), this)).start();
    }

    public DownloadFiles(TeamQueue teamQueue, Map[] arrmap, File file) {
        this.conn = teamQueue;
        this.queue = CommonUtils.toList(arrmap).iterator();
        this.dest = file;
        file.mkdirs();
    }

    public void complete(String string) {
        startNextDownload();
    }

    public void cancel() {
    }
}
