package common;

import common.AObject;
import common.Callback;
import common.CommonUtils;
import common.DownloadMessage;
import common.DownloadNotify;
import common.MudgeSanity;
import common.TeamQueue;
import dialog.DialogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.ProgressMonitor;

public class DownloadFile extends AObject implements Callback {

    protected FileOutputStream out = null;

    protected File file = null;

    protected TeamQueue conn = null;

    protected DownloadNotify listener = null;

    protected String rpath = null;

    protected long total = 0L;

    protected long start = System.currentTimeMillis();

    protected long read = 0L;

    protected long ret = 0L;

    protected long sofar = 0L;

    protected double time = 0.0D;

    protected ProgressMonitor progress = null;

    public DownloadFile(TeamQueue teamQueue, String string,
                        File file, DownloadNotify downloadNotify) {
        this.file = file;
        this.listener = downloadNotify;
        this.conn = teamQueue;
        this.rpath = string;
    }

    public void result(String string, Object object) {
        try {
            _result(string, object);
        } catch (IOException iOException) {
            MudgeSanity.logException(string + " " + object, iOException, false);
        }
    }

    public void _result(String string, Object object) throws IOException {
        DownloadMessage downloadMessage = (DownloadMessage) object;
        if (downloadMessage.getType() == 0) {
            this.total = downloadMessage.getSize();
            this.out = new FileOutputStream(this.file, false);
            this.progress = new ProgressMonitor(null, "Download "
                    + this.file.getName(), "Starting download", 0, (int) this.total);
            this.conn.call("download.get", CommonUtils.args(downloadMessage.id()), this);
        } else if (downloadMessage.getType() == 1) {
            this.time = (System.currentTimeMillis() - this.start) / 1000.0D;
            this.sofar += downloadMessage.getData().length;
            this.progress.setProgress((int) this.sofar);
            this.progress.setNote("Speed: "
                    + Math.round((this.sofar / 1024L) / this.time) + " KB/s");
            if (this.progress.isCanceled()) {
                this.progress.close();
                this.out.close();
                if (this.listener != null) {
                    this.listener.cancel();
                }
                return;
            }
            this.out.write(downloadMessage.getData());
            this.conn.call("download.get", CommonUtils.args(downloadMessage.id()), this);
        } else if (downloadMessage.getType() == 2) {
            this.progress.close();
            this.out.close();
            if (this.listener != null) {
                this.listener.complete(this.file.getAbsolutePath());
            }
        } else if (downloadMessage.getType() == 3) {
            if (this.out != null && this.progress != null) {
                this.out.close();
                this.progress.setNote(downloadMessage.getError());
                if (this.listener != null) {
                    this.listener.cancel();
                }
            } else {
                DialogUtils.showError(downloadMessage.getError());
            }
        }
    }

    public void start() {
        this.conn.call("download.start", CommonUtils.args(this.rpath), this);
    }
}
