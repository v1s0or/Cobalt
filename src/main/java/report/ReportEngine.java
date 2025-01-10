package report;

import aggressor.AggressorClient;
import aggressor.Prefs;
import aggressor.bridges.AggregateBridge;
import aggressor.bridges.AttackBridge;
import common.CommonUtils;
import common.MudgeSanity;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import sleep.error.YourCodeSucksException;
import sleep.runtime.ScriptInstance;
import sleep.runtime.ScriptLoader;

public class ReportEngine {

    protected AggressorClient client;

    protected ReportBridge bridge;

    protected LinkedList<String> reportassets = new LinkedList();

    public ReportEngine(AggressorClient aggressorClient) {
        this.client = aggressorClient;
        this.bridge = new ReportBridge();
    }

    public List reportTitles() {
        rehash();
        List list = this.bridge.reportTitles();
        Collections.sort(list);
        return list;
    }

    public String describe(String string) {
        return this.bridge.describe(string);
    }

    public Document buildReport(String string1, String string2, Stack stack) {
        return this.bridge.buildReport(string1, string2, stack);
    }

    public void load(String string, InputStream inputStream) {
        Hashtable hashtable = new Hashtable();
        ScriptLoader scriptLoader = new ScriptLoader();
        try {
            scriptLoader.addGlobalBridge(this.bridge);
            scriptLoader.addGlobalBridge(this.client.getScriptEngine());
            scriptLoader.addGlobalBridge(new AggregateBridge(this.client));
            scriptLoader.addGlobalBridge(new AttackBridge());
            ScriptInstance scriptInstance = scriptLoader.loadScript(string, inputStream);
            scriptInstance.addWarningWatcher(this.client.getScriptEngine());
            scriptInstance.runScript();
        } catch (YourCodeSucksException yourCodeSucksException) {
            CommonUtils.print_error("Could not load: " + string + " (syntax errors; go to View -> Script Console)");
            this.client.getScriptEngine().perror("Could not load " + string + ":\n" + yourCodeSucksException.formatErrors());
        } catch (Exception exception) {
            this.client.getScriptEngine().perror("Could not load " + string + ": " + exception.getMessage());
            MudgeSanity.logException("Could not load:" + string, exception, false);
        }
    }

    public void registerInternal(String string) {
        this.reportassets.add(string);
        rehash();
    }

    public void rehash() {
        this.bridge = new ReportBridge();
        // for (Object str : this.reportassets) {
        for (String str : this.reportassets) {
            try {
                load(str, CommonUtils.resource(str));
            } catch (Exception exception) {
                MudgeSanity.logException("asset: " + str, exception, false);
            }
        }
        for (String str : Prefs.getPreferences().getList("reporting.custom_reports")) {
            try {
                load(str, new FileInputStream(str));
            } catch (Exception exception) {
                MudgeSanity.logException("file: " + str, exception, false);
            }
        }
    }
}
