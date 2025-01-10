package logger;

import common.CommonUtils;
import common.MudgeSanity;

import java.util.LinkedList;

public abstract class ProcessBackend implements Runnable {
    protected LinkedList tasks = new LinkedList();

    public void start(String string) {
        (new Thread(this, string)).start();
    }

    public void act(Object object) {
        synchronized (this) {
            this.tasks.add(object);
        }
    }

    protected Object grabTask() {
        synchronized (this) {
            return this.tasks.pollFirst();
        }
    }

    public abstract void process(Object object);

    public void run() {
        while (true) {
            Object object = grabTask();
            if (object != null) {
                try {
                    process(object);
                } catch (Exception exception) {
                    MudgeSanity.logException("ProcessBackend: " + object.getClass(), exception, false);
                }
                Thread.yield();
                continue;
            }
            CommonUtils.sleep(10000L);
        }
    }
}
