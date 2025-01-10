package beacon;

import common.CommonUtils;
import common.Download;
import common.MudgeSanity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BeaconDownloads {
    protected List<BeaconDownload> downloads = new LinkedList();

    public void start(String string1, int n, String string2, String string3, long l) {
        try {
            File file = CommonUtils.SafeFile("downloads", CommonUtils.garbage("file name"));
            file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(file, false);
            String str1 = new File(string3.replace("\\", "/")).getName();
            String str2 = CommonUtils.stripRight(string3, str1);
            BeaconDownload beaconDownload = new BeaconDownload(str1, fos, string1, n);
            beaconDownload.flen = l;
            beaconDownload.rpath = str2;
            beaconDownload.lpath = file.getCanonicalPath();
            beaconDownload.host = string2;
            synchronized (this) {
                this.downloads.add(beaconDownload);
            }
        } catch (IOException iOException) {
            MudgeSanity.logException("start download: " + string3, iOException, false);
        }
    }

    protected List getDownloads(String string) {
        LinkedList<Map> linkedList = new LinkedList();
        synchronized (this) {
            for (BeaconDownload beaconDownload : this.downloads) {
                if (beaconDownload.bid.equals(string)) {
                    linkedList.add(beaconDownload.toDownload().toMap());
                }
            }
        }
        return linkedList;
    }

    protected BeaconDownload find(String string, int n) {
        synchronized (this) {
            for (BeaconDownload beaconDownload : this.downloads) {
                if (beaconDownload.is(string, n)) {
                    return beaconDownload;
                }
            }
        }
        return null;
    }

    public void write(String string, int n, byte[] arrby) {
        synchronized (this) {
            try {
                BeaconDownload beaconDownload = find(string, n);
                beaconDownload.rcvd += arrby.length;
                beaconDownload.handle.write(arrby, 0, arrby.length);
            } catch (IOException iOException) {
                MudgeSanity.logException("write download", iOException, false);
            }
        }
    }

    public boolean exists(String string, int n) {
        return (find(string, n) != null);
    }

    public boolean isComplete(String string, int n) {
        synchronized (this) {
            BeaconDownload beaconDownload = find(string, n);
            return (beaconDownload != null && beaconDownload.flen == beaconDownload.rcvd);
        }
    }

    public boolean isActive(String string) {
        synchronized (this) {
            for (BeaconDownload beaconDownload : this.downloads) {
                if (beaconDownload.bid.equals(string)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getName(String string, int n) {
        BeaconDownload beaconDownload = find(string, n);
        if (beaconDownload == null) {
            return "unknown";
        }
        return beaconDownload.fname;
    }

    public Download getDownload(String string, int n) {
        BeaconDownload beaconDownload = find(string, n);
        if (beaconDownload == null) {
            return null;
        }
        return beaconDownload.toDownload();
    }

    public void close(String string, int n) {
        synchronized (this) {
            Iterator iterator = this.downloads.iterator();
            while (iterator.hasNext()) {
                BeaconDownload beaconDownload = (BeaconDownload) iterator.next();
                if (beaconDownload.is(string, n)) {
                    iterator.remove();
                    try {
                        beaconDownload.handle.close();
                    } catch (IOException iOException) {
                        MudgeSanity.logException("write close", iOException, false);
                    }
                }
            }
        }
    }

    public static class BeaconDownload {
        public String fname;

        public OutputStream handle;

        public String bid;

        public int fid;

        public long start = System.currentTimeMillis();

        public long flen;

        public long rcvd = 0L;

        public String rpath;

        public String lpath;

        public String host;

        public BeaconDownload(String string1, OutputStream outputStream, String string2, int n) {
            this.fname = string1;
            this.handle = outputStream;
            this.bid = string2;
            this.fid = n;
        }

        public boolean is(String string, int n) {
            return this.bid.equals(string) && this.fid == n;
        }

        public Download toDownload() {
            return new Download(this.fid, this.bid, this.host,
                    this.fname, this.rpath, this.lpath, this.flen);
        }
    }
}
