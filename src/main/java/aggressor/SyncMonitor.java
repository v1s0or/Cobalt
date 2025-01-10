package aggressor;

import common.Callback;
import common.PlaybackStatus;

import javax.swing.ProgressMonitor;

public class SyncMonitor implements Callback {
    protected ProgressMonitor monitor = null;

    protected AggressorClient client;

    public SyncMonitor(AggressorClient aggressorClient) {
        this.client = aggressorClient;
        aggressorClient.getData().subscribe("playback.status", this);
    }

    public void result(String string, Object object) {
        PlaybackStatus playbackStatus = (PlaybackStatus) object;
        if (playbackStatus.isStart()) {
            this.monitor = new ProgressMonitor(this.client, "Sync to Team Server", playbackStatus.getMessage(), 0, 100);
        } else if (playbackStatus.isDone() && this.monitor != null) {
            this.monitor.close();
        } else if (this.monitor != null) {
            this.monitor.setNote("[" + playbackStatus.getSent() + "/" + playbackStatus.getTotal() + "] " + playbackStatus.getMessage());
            this.monitor.setProgress(playbackStatus.percentage());
        }
    }
}
