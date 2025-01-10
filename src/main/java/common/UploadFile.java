package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.ProgressMonitor;

public class UploadFile implements Callback {

    protected FileInputStream in = null;

    protected byte[] buffer = new byte[262144];

    protected File file = null;

    protected TeamQueue conn = null;

    protected UploadNotify listener = null;

    protected long total;

    protected long start = System.currentTimeMillis();

    protected long read = 0L;

    protected long ret = 0L;

    protected long sofar = 0L;

    protected double time = 0.0D;

    protected ProgressMonitor progress;

    public UploadFile(TeamQueue teamQueue, File file, UploadNotify paramUploadNotify) {
        this.file = file;
        this.listener = paramUploadNotify;
        this.conn = teamQueue;
    }

    @Override
    public void result(String string, Object object) {
        String str = object + "";
        try {
            if (this.sofar < this.total) {
                this.time = (System.currentTimeMillis() - this.start) / 1000.0D;
                this.progress.setProgress((int) this.sofar);
                this.progress.setNote("Speed: " + Math.round((this.sofar / 1024L) / this.time) + " KB/s");
                if (this.progress.isCanceled()) {
                    this.progress.close();
                    this.in.close();
                    this.listener.cancel();
                    return;
                }
                if (str.startsWith("ERROR: ")) {
                    this.progress.setNote(str);
                    this.in.close();
                    return;
                }
                this.read = this.in.read(this.buffer);
                this.sofar += this.read;
                this.conn.call("armitage.append", CommonUtils.args(this.file.getName(), tailor(this.buffer, this.read)), this);
            } else {
                this.time = (System.currentTimeMillis() - this.start) / 1000.0D;
                this.progress.setProgress((int) this.sofar);
                this.progress.setNote("Speed: " + Math.round((this.sofar / 1024L) / this.time) + " KB/s");
                this.progress.close();
                this.in.close();
                this.listener.complete(object + "");
            }
        } catch (Exception exception) {
            MudgeSanity.logException("upload" + this.sofar + "/" + this.total + " of " + this.file, exception, false);
            this.listener.cancel();
        }
    }

    public void start() {
        try {
            this.total = this.file.length();
            this.in = new FileInputStream(this.file);
            progress = new ProgressMonitor(null, "Upload " + file.getName(),
                    "Starting upload", 0, (int) this.total);
            this.conn.call("armitage.upload", CommonUtils.args(this.file.getName()), this);
        } catch (IOException iOException) {
            MudgeSanity.logException("upload start: " + this.file, iOException, false);
            this.listener.cancel();
        }
    }

    protected byte[] tailor(byte[] arrby, long l) {
        byte[] arrby1 = new byte[(int) l];
        if (l >= 0) {
            System.arraycopy(arrby, 0, arrby1, 0, (int) l);
        }
        return arrby1;
    }

    public static interface UploadNotify {
        void complete(String string);

        void cancel();
    }
}
