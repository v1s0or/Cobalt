package server;

import common.CommonUtils;
import common.DownloadMessage;
import common.MudgeSanity;
import common.Reply;
import common.Request;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DownloadCalls implements ServerHook {
    protected Resources resources = null;

    protected long ids = 0L;

    protected Map sessions = new HashMap();

    public void register(Map map) {
        map.put("download.start", this);
        map.put("download.get", this);
    }

    public DownloadCalls(Resources resources) {
        this.resources = resources;
    }

    public String makeSession(Request request, ManageUser manageUser, File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            synchronized (this) {
                this.ids++;
                this.sessions.put(this.ids + "", fileInputStream);
            }
            return this.ids + "";
        } catch (IOException iOException) {
            MudgeSanity.logException("makeSession", iOException, false);
            manageUser.writeNow(request.reply(DownloadMessage.Error(null, iOException.getMessage())));
            return null;
        }
    }

    public void getChunk(Request request, ManageUser manageUser, String string) {
        FileInputStream fileInputStream;
        synchronized (this) {
            fileInputStream = (FileInputStream) this.sessions.get(string);
        }
        if (fileInputStream == null) {
            manageUser.writeNow(request.reply(DownloadMessage.Error(string, "invalid download ID")));
            return;
        }
        try {
            byte[] arrby = new byte[262144];
            int i = fileInputStream.read(arrby);
            if (i > 0) {
                byte[] arrby1 = new byte[i];
                System.arraycopy(arrby, 0, arrby1, 0, i);
                manageUser.writeNow(request.reply(DownloadMessage.Chunk(string, arrby1)));
            } else {
                synchronized (this) {
                    this.sessions.remove(string);
                    fileInputStream.close();
                }
                manageUser.writeNow(request.reply(DownloadMessage.Done(string)));
            }
        } catch (IOException iOException) {
            MudgeSanity.logException("getChunk", iOException, false);
            manageUser.writeNow(request.reply(DownloadMessage.Error(string, iOException.getMessage())));
        }
    }

    public void call(Request request, ManageUser manageUser) {
        if (request.is("download.start", 1)) {
            File file = new File(request.arg(0) + "");
            if (!CommonUtils.isSafeFile(new File("downloads"), file)) {
                CommonUtils.print_error(manageUser.getNick() + " attempted to sync '" + request.arg(0) + "'. Rejected: not in the downloads/ folder.");
                manageUser.writeNow(request.reply(DownloadMessage.Error(null, "argument is not in downloads/ folder")));
                return;
            }
            if (!file.exists()) {
                manageUser.writeNow(request.reply(DownloadMessage.Error(null, "File does not exist")));
                return;
            }
            String str = makeSession(request, manageUser, file);
            if (str == null)
                return;
            manageUser.writeNow(request.reply(DownloadMessage.Start(str, file.length())));
        } else if (request.is("download.get", 1)) {
            getChunk(request, manageUser, request.arg(0) + "");
        } else {
            manageUser.writeNow(new Reply("server_error", 0L, request + ": incorrect number of arguments"));
        }
    }
}
