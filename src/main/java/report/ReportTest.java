package report;

import common.CommonUtils;
import common.MudgeSanity;

import java.io.File;
import java.io.InputStream;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Stack;

import report.Document;
import report.ReportBridge;
import sleep.error.RuntimeWarningWatcher;
import sleep.error.ScriptWarning;
import sleep.error.YourCodeSucksException;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.ScriptLoader;
import sleep.runtime.SleepUtils;

public class ReportTest implements RuntimeWarningWatcher {
    protected ReportBridge bridge = new ReportBridge();

    public void processScriptWarning(ScriptWarning scriptWarning) {
        String str1 = scriptWarning.getNameShort() + ":" + scriptWarning.getLineNumber();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String str2 = simpleDateFormat.format(date, new StringBuffer(),
                new FieldPosition(0)).toString();
        if (scriptWarning.isDebugTrace()) {
            CommonUtils.print_info("[" + str2 + "] Trace: "
                    + scriptWarning.getMessage() + " at " + str1);
        } else {
            CommonUtils.print_info("[" + str2 + "] "
                    + scriptWarning.getMessage() + " at " + str1);
        }
    }

    public Document buildReport(String string, Stack stack) {
        return this.bridge.buildReport(string, string, stack);
    }

    public void load(String string, InputStream inputStream) {
        Hashtable hashtable = new Hashtable();
        ScriptLoader scriptLoader = new ScriptLoader();
        try {
            scriptLoader.addGlobalBridge(this.bridge);
            ScriptInstance scriptInstance = scriptLoader.loadScript(string, inputStream);
            scriptInstance.addWarningWatcher(this);
            scriptInstance.runScript();
        } catch (YourCodeSucksException yourCodeSucksException) {
            CommonUtils.print_error("Could not load: " + string + "\n" + yourCodeSucksException.formatErrors());
        } catch (Exception exception) {
            MudgeSanity.logException("Could not load:" + string, exception, false);
        }
    }

    public static void main(String[] arrstring) {
        if (arrstring.length < 3) {
            CommonUtils.print_warn("ReportTest [file.rpt] [title] [/path/to/out.pdf] [args...]");
            return;
        }
        try {
            ReportTest reportTest = new ReportTest();
            reportTest.load(arrstring[0], CommonUtils.resource(arrstring[0]));
            Stack<Scalar> stack = new Stack();
            for (int b = 3; b < arrstring.length; b++) {
                stack.add(0, SleepUtils.getScalar(arrstring[b]));
            }
            Document document = reportTest.buildReport(arrstring[1], stack);
            document.toPDF(new File(arrstring[2]));
        } catch (Exception exception) {
            MudgeSanity.logException("Error", exception, false);
        }
    }
}
