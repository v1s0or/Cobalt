package cortana.core;

import common.MudgeSanity;

import java.util.LinkedList;
import java.util.Stack;

import sleep.runtime.SleepUtils;

public class EventQueue implements Runnable {
    protected EventManager manager;

    protected LinkedList queue = new LinkedList();

    protected boolean run = true;

    public EventQueue(EventManager paramEventManager) {
        this.manager = paramEventManager;
        (new Thread(this, "Aggressor Script Event Queue")).start();
    }

    public void add(String string, Stack stack) {
        Event event = new Event();
        event.name = string;
        event.args = stack;
        synchronized (this) {
            if (this.manager.hasWildcardListener()) {
                this.queue.add(event.wildcard());
            }
            this.queue.add(event);
        }
    }

    protected Event grabEvent() {
        synchronized (this) {
            return (Event) this.queue.pollFirst();
        }
    }

    public void stop() {
        this.run = false;
    }

    public void run() {
        while (this.run) {
            Event event = grabEvent();
            try {
                if (event != null) {
                    this.manager.fireEventNoQueue(event.name, event.args, null);
                    continue;
                }
                Thread.sleep(25L);
            } catch (Exception exception) {
                if (event != null) {
                    MudgeSanity.logException("event: " + event.name + "/" + SleepUtils.describe(event.args), exception, false);
                    continue;
                }
                MudgeSanity.logException("event (none)", exception, false);
            }
        }
    }

    private static class Event {
        public String name;

        public Stack args;

        private Event() {
        }

        public Event wildcard() {
            Event event = new Event();
            event.name = "*";
            event.args = new Stack();
            event.args.addAll(this.args);
            event.args.push(SleepUtils.getScalar(this.name));
            return event;
        }
    }
}
