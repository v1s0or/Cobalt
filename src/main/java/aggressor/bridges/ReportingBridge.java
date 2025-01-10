package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.dialogs.ExportDataDialog;
import aggressor.dialogs.ExportReportDialog;
import cortana.Cortana;

import java.io.IOException;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class ReportingBridge implements Function, Loadable {

    protected AggressorClient client;

    public ReportingBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&reports", this);
        Cortana.put(scriptInstance, "&reportDescription", this);
        Cortana.put(scriptInstance, "&openReportDialog", this);
        Cortana.put(scriptInstance, "&openExportDataDialog", this);
        Cortana.put(scriptInstance, "&rehash_reports", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if ("&reports".equals(string))
            return SleepUtils.getArrayWrapper(this.client.getReportEngine().reportTitles());
        if ("&reportDescription".equals(string)) {
            String str = BridgeUtilities.getString(stack, "");
            return SleepUtils.getScalar(this.client.getReportEngine().describe(str));
        }
        if ("&openReportDialog".equals(string)) {
            String str = BridgeUtilities.getString(stack, "");
            ExportReportDialog exportReportDialog = new ExportReportDialog(this.client, str);
            try {
                exportReportDialog.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ("&openExportDataDialog".equals(string)) {
            ExportDataDialog exportDataDialog = new ExportDataDialog(this.client);
            try {
                exportDataDialog.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ("&rehash_reports".equals(string)) {
            this.client.getReportEngine().rehash();
            return SleepUtils.getScalar("done");
        }
        return SleepUtils.getEmptyScalar();
    }
}
