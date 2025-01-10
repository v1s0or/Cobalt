package aggressor.bridges;

import common.Callback;
import common.DisconnectListener;
import common.TeamQueue;
import common.TeamSocket;
import cortana.Cortana;

import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.engine.ObjectUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class TeamServerBridge implements Function, Loadable, DisconnectListener {

    protected TeamQueue conn;

    protected Cortana engine;

    public TeamServerBridge(Cortana cortana, TeamQueue teamQueue) {
        this.engine = cortana;
        this.conn = teamQueue;
        teamQueue.addDisconnectListener(this);
    }

    public void disconnected(TeamSocket paramTeamSocket) {
        this.engine.getEventManager().fireEvent("disconnect", new Stack());
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&call", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&call")) {
            Callback callback;
            String str = BridgeUtilities.getString(stack, "");
            Scalar scalar = (Scalar) stack.pop();
            if (SleepUtils.isEmptyScalar(scalar)) {
                callback = null;
            } else {
                callback = (Callback) ObjectUtilities.buildArgument(Callback.class, scalar, scriptInstance);
            }
            Object[] arrobject = new Object[stack.size()];
            for (byte b = 0; b < arrobject.length; b++)
                arrobject[b] = BridgeUtilities.getObject(stack);
            this.conn.call(str, arrobject, callback);
        }
        return SleepUtils.getEmptyScalar();
    }
}
