package cortana.support;

import common.MudgeSanity;
import cortana.Cortana;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Heartbeat implements Runnable {
    protected Cortana engine;

    protected List<Beat> beats;

    public Heartbeat(Cortana cortana) {
        this.engine = cortana;
        this.beats = new LinkedList();
        this.beats.add(new Beat("heartbeat_1s", 1000L));
        this.beats.add(new Beat("heartbeat_5s", 5000L));
        this.beats.add(new Beat("heartbeat_10s", 10000L));
        this.beats.add(new Beat("heartbeat_15s", 15000L));
        this.beats.add(new Beat("heartbeat_30s", 30000L));
        this.beats.add(new Beat("heartbeat_1m", 60000L));
        this.beats.add(new Beat("heartbeat_5m", 300000L));
        this.beats.add(new Beat("heartbeat_10m", 600000L));
        this.beats.add(new Beat("heartbeat_15m", 900000L));
        this.beats.add(new Beat("heartbeat_20m", 1200000L));
        this.beats.add(new Beat("heartbeat_30m", 1800000L));
        this.beats.add(new Beat("heartbeat_60m", 3600000L));
    }

    public void start() {
        new Thread(this, "heartbeat thread").start();
    }

    public void run() {
        while (this.engine.isActive()) {
            try {
                long l = System.currentTimeMillis();
                for (Beat beat : this.beats) {
                    beat.check(l);
                }
                Thread.sleep(1000L);
            } catch (Exception exception) {
                MudgeSanity.logException("heartbeat error", exception, false);
            }
        }
        this.engine = null;
    }

    private class Beat {
        protected long next = 0L;

        protected long mark;

        protected String event;

        public Beat(String string, long pl1) {
            this.mark = pl1;
            this.event = string;
            this.next = System.currentTimeMillis() + pl1;
        }

        public void check(long pl1) {
            if (this.next <= pl1) {
                this.next = System.currentTimeMillis() + this.mark;
                Heartbeat.this.engine.getEventManager().fireEvent(this.event, new Stack());
            }
        }
    }
}
