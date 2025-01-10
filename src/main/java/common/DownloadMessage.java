package common;

import java.io.Serializable;

public class DownloadMessage implements Serializable {

    public static final int DOWNLOAD_START = 0;

    public static final int DOWNLOAD_CHUNK = 1;

    public static final int DOWNLOAD_DONE = 2;

    public static final int DOWNLOAD_ERROR = 3;

    protected String id = null;

    protected long size = 0L;

    protected byte[] data = null;

    protected String message = null;

    protected int type = DOWNLOAD_START;

    protected DownloadMessage(int n, String string) {
        this.type = n;
        this.id = string;
    }

    public static DownloadMessage Error(String string1, String string2) {
        DownloadMessage downloadMessage = new DownloadMessage(DOWNLOAD_ERROR, string1);
        downloadMessage.message = string2;
        return downloadMessage;
    }

    public static DownloadMessage Chunk(String string, byte[] arrby) {
        DownloadMessage downloadMessage = new DownloadMessage(DOWNLOAD_CHUNK, string);
        downloadMessage.data = arrby;
        return downloadMessage;
    }

    public static DownloadMessage Done(String string) {
        return new DownloadMessage(DOWNLOAD_DONE, string);
    }

    public static DownloadMessage Start(String string, long l) {
        DownloadMessage downloadMessage = new DownloadMessage(DOWNLOAD_START, string);
        downloadMessage.size = l;
        return downloadMessage;
    }

    public String getError() {
        if (this.type != DOWNLOAD_ERROR) {
            throw new RuntimeException("Wrong message type for that info");
        }
        return this.message;
    }

    public byte[] getData() {
        if (this.type != DOWNLOAD_CHUNK) {
            throw new RuntimeException("Wrong message type for that info");
        }
        return this.data;
    }

    public long getSize() {
        if (this.type != DOWNLOAD_START) {
            throw new RuntimeException("Wrong message type for that info");
        }
        return this.size;
    }

    public int getType() {
        return this.type;
    }

    public String id() {
        return this.id;
    }
}
