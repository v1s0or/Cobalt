package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DownloadFileSimple extends AObject implements Callback {

    protected FileOutputStream out = null;

    protected File file = null;

    protected TeamQueue conn = null;

    protected DownloadNotify listener = null;

    protected String rpath = null;

    public DownloadFileSimple(TeamQueue teamQueue, String string, File file, DownloadNotify downloadNotify) {
        this.file = file;
        this.listener = downloadNotify;
        this.conn = teamQueue;
        this.rpath = string;
    }

    public void result(String string, Object object) {
        try {
            _result(string, object);
        } catch (Exception e) {
            MudgeSanity.logException(string + " " + object, e, false);
        }
    }

    public void _result(String string, Object object) {
        try {
            DownloadMessage downloadMessage = (DownloadMessage) object;
            if (downloadMessage.getType() == 0) {
                this.out = new FileOutputStream(this.file, false);
                this.conn.call("download.get", CommonUtils.args(downloadMessage.id()), this);
            } else if (downloadMessage.getType() == 1) {
                this.out.write(downloadMessage.getData());
                this.conn.call("download.get", CommonUtils.args(downloadMessage.id()), this);
            } else if (downloadMessage.getType() == 2) {
                this.out.close();
                if (this.listener != null) {
                    this.listener.complete(this.file.getAbsolutePath());
                }
            } else if (downloadMessage.getType() == 3) {
                if (this.out != null) {
                    this.out.close();
                    if (this.listener != null) {
                        this.listener.cancel();
                    }
                } else {
                    CommonUtils.print_error("download sync " + this.rpath + " failed: " + downloadMessage.getError());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        this.conn.call("download.start", CommonUtils.args(this.rpath), this);
    }
}
