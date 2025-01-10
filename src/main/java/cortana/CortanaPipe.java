package cortana;

import common.CommonUtils;
import common.MudgeSanity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.LinkedList;
import java.util.List;

public class CortanaPipe implements Runnable {

    protected PipedInputStream readme;

    protected PipedOutputStream writeme;

    protected boolean run = true;

    protected List<CortanaPipeListener> listeners = new LinkedList();

    public OutputStream getOutput() {
        return this.writeme;
    }

    public CortanaPipe() {
        try {
            this.readme = new PipedInputStream(1048576);
            this.writeme = new PipedOutputStream(this.readme);
        } catch (IOException iOException) {
            MudgeSanity.logException("create cortana pipe", iOException, false);
        }
    }

    public void addCortanaPipeListener(CortanaPipeListener cortanaPipeListener) {
        synchronized (this) {
            this.listeners.add(cortanaPipeListener);
        }
        if (this.listeners.size() == 1) {
            new Thread(this, "cortana pipe reader").start();
        }
    }

    public void close() {
        try {
            this.run = false;
            this.writeme.close();
        } catch (IOException iOException) {
            MudgeSanity.logException("close cortana pipe", iOException, false);
        }
    }

    public void run() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.readme));
        while (this.run) {
            try {
                String str = bufferedReader.readLine();
                if (str != null) {
                    synchronized (this) {
                        for (CortanaPipeListener cortanaPipeListener : this.listeners) {
                            cortanaPipeListener.read(str);
                        }
                    }
                }
            } catch (IOException iOException) {
                CommonUtils.sleep(500L);
            }
        }
        try {
            bufferedReader.close();
        } catch (IOException iOException) {
            MudgeSanity.logException("cortana pipe cleanup", iOException, false);
        }
    }

    public static interface CortanaPipeListener {
        void read(String string);
    }
}
