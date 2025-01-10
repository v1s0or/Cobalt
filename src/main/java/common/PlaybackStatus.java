package common;

import java.io.Serializable;

public class PlaybackStatus implements Serializable {

    protected String message;

    protected int total = 1;

    protected int sent = 0;

    public PlaybackStatus copy() {
        PlaybackStatus playbackStatus = new PlaybackStatus(this.message, this.total);
        playbackStatus.sent = this.sent;
        return playbackStatus;
    }

    public PlaybackStatus(String string, int n) {
        this.total = n;
        this.message = string;
    }

    public String getMessage() {
        return this.message;
    }

    public void message(String string) {
        this.message = string;
    }

    public void more(int n) {
        this.total += n;
    }

    public int getSent() {
        return this.sent;
    }

    public int getTotal() {
        return this.total;
    }

    public void sent() {
        this.sent++;
    }

    public int percentage() {
        return (int) (this.sent / this.total * 100.0D);
    }

    public boolean isDone() {
        return (this.sent == this.total);
    }

    public boolean isStart() {
        return (this.sent == 0);
    }
}
