package common;

import common.CommonUtils;
import common.Do;
import common.MudgeSanity;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Timers implements Runnable {

    private static Timers mytimer = null;

    protected List<ActionItem> timers = new LinkedList();

    public static synchronized Timers getTimers() {
        if (mytimer == null) {
            mytimer = new Timers();
        }
        return mytimer;
    }

    public void every(long l, String string, Do do_) {
        synchronized (this) {
            this.timers.add(new ActionItem(do_, string, l));
        }
    }

    private Timers() {
        new Thread(this, "global timer").start();
    }

    public void fire(ActionItem actionItem) {
        actionItem.moment();
    }

    public void run() {
        LinkedList<ActionItem> linkedList = null;
        while (true) {
            synchronized (this) {
                linkedList = new LinkedList(this.timers);
            }
            long l = System.currentTimeMillis();
            for (ActionItem actionItem : linkedList) {
                if (actionItem.isDue(l)) {
                    fire(actionItem);
                }
            }
            synchronized (this) {
                Iterator iterator = this.timers.iterator();
                while (iterator.hasNext()) {
                    ActionItem actionItem = (ActionItem) iterator.next();
                    if (!actionItem.shouldKeep()) {
                        iterator.remove();
                    }
                }
            }
            CommonUtils.sleep(1000L);
        }
    }

    private static class ActionItem {
        public Do action;

        public long every;

        public long last;

        public boolean keep = true;

        public String msg;

        public ActionItem(Do do_, String string, long pl1) {
            this.action = do_;
            this.every = pl1;
            this.last = 0L;
            this.msg = string;
        }

        public boolean isDue(long pl1) {
            return pl1 - this.last >= this.every;
        }

        public void moment() {
            try {
                this.last = System.currentTimeMillis();
                this.keep = this.action.moment(this.msg);
            } catch (Exception exception) {
                MudgeSanity.logException("timer to " + this.action.getClass() + "/" + this.msg + " every " + this.last + "ms", exception, false);
                this.keep = false;
            }
        }

        public boolean shouldKeep() {
            return this.keep;
        }
    }
}
