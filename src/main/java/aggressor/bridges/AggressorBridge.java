package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.ColorManagerScripted;
import aggressor.DataUtils;
import aggressor.MultiFrame;
import aggressor.TabManager;
import aggressor.browsers.Beacons;
import aggressor.browsers.Sessions;
import aggressor.browsers.Targets;
import aggressor.dialogs.AboutDialog;
import aggressor.dialogs.AutoRunDialog;
import aggressor.dialogs.BrowserPivotSetup;
import aggressor.dialogs.CloneSiteDialog;
import aggressor.dialogs.ConnectDialog;
import aggressor.dialogs.CovertVPNSetup;
import aggressor.dialogs.GoldenTicketDialog;
import aggressor.dialogs.HTMLApplicationDialog;
import aggressor.dialogs.HostFileDialog;
import aggressor.dialogs.JavaSignedAppletDialog;
import aggressor.dialogs.JavaSmartAppletDialog;
import aggressor.dialogs.JumpDialogAlt;
import aggressor.dialogs.MakeTokenDialog;
import aggressor.dialogs.OfficeMacroDialog;
import aggressor.dialogs.PayloadGeneratorDialog;
import aggressor.dialogs.PayloadGeneratorStageDialog;
import aggressor.dialogs.PivotListenerSetup;
import aggressor.dialogs.PortScanDialog;
import aggressor.dialogs.PortScanLocalDialog;
import aggressor.dialogs.PreferencesDialog;
import aggressor.dialogs.SOCKSSetup;
import aggressor.dialogs.ScListenerChooser;
import aggressor.dialogs.ScriptedWebDialog;
import aggressor.dialogs.ScriptedWebStageDialog;
import aggressor.dialogs.SecureShellDialog;
import aggressor.dialogs.SecureShellPubKeyDialog;
import aggressor.dialogs.SpawnAsDialog;
import aggressor.dialogs.SpearPhishDialog;
import aggressor.dialogs.SystemInformationDialog;
import aggressor.dialogs.SystemProfilerDialog;
import aggressor.dialogs.WindowsDropperDialog;
import aggressor.dialogs.WindowsExecutableDialog;
import aggressor.dialogs.WindowsExecutableStageDialog;
import aggressor.viz.PivotGraph;
import aggressor.windows.ApplicationManager;
import aggressor.windows.BeaconBrowser;
import aggressor.windows.BeaconConsole;
import aggressor.windows.CortanaConsole;
import aggressor.windows.CredentialManager;
import aggressor.windows.DownloadBrowser;
import aggressor.windows.EventLog;
import aggressor.windows.FileBrowser;
import aggressor.windows.InterfaceManager;
import aggressor.windows.KeystrokeBrowser;
import aggressor.windows.ListenerManager;
import aggressor.windows.ProcessBrowser;
import aggressor.windows.ProcessBrowserMulti;
import aggressor.windows.SOCKSBrowser;
import aggressor.windows.ScreenshotBrowser;
import aggressor.windows.ScriptManager;
import aggressor.windows.SecureShellConsole;
import aggressor.windows.ServiceBrowser;
import aggressor.windows.SiteManager;
import aggressor.windows.TargetBrowser;
import aggressor.windows.WebLog;
import common.BeaconEntry;
import common.CommonUtils;
import common.Keys;
import common.ScriptUtils;
import common.TeamQueue;
import console.Console;
import cortana.Cortana;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.JComponent;

import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class AggressorBridge implements Function, Loadable {
    protected TabManager manager;

    protected Cortana engine;

    protected MultiFrame window;

    protected AggressorClient client;

    protected TeamQueue conn;

    public AggressorBridge(AggressorClient aggressorClient, Cortana cortana, TabManager paramTabManager, MultiFrame paramMultiFrame, TeamQueue teamQueue) {
        this.client = aggressorClient;
        this.engine = cortana;
        this.manager = paramTabManager;
        this.window = paramMultiFrame;
        this.conn = teamQueue;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&openScriptConsole", this);
        Cortana.put(scriptInstance, "&openEventLog", this);
        Cortana.put(scriptInstance, "&openConnectDialog", this);
        Cortana.put(scriptInstance, "&closeClient", this);
        Cortana.put(scriptInstance, "&openHostFileDialog", this);
        Cortana.put(scriptInstance, "&openWebLog", this);
        Cortana.put(scriptInstance, "&openSiteManager", this);
        Cortana.put(scriptInstance, "&openListenerManager", this);
        Cortana.put(scriptInstance, "&openBeaconBrowser", this);
        Cortana.put(scriptInstance, "&openWindowsExecutableStageDialog", this);
        Cortana.put(scriptInstance, "&openAutoRunDialog", this);
        Cortana.put(scriptInstance, "&openPayloadHelper", this);
        Cortana.put(scriptInstance, "&openWindowsExecutableDialog", this);
        Cortana.put(scriptInstance, "&openPayloadGeneratorDialog", this);
        Cortana.put(scriptInstance, "&openPayloadGeneratorStageDialog", this);
        Cortana.put(scriptInstance, "&openOfficeMacroDialog", this);
        Cortana.put(scriptInstance, "&openJavaSignedAppletDialog", this);
        Cortana.put(scriptInstance, "&openJavaSmartAppletDialog", this);
        Cortana.put(scriptInstance, "&openHTMLApplicationDialog", this);
        Cortana.put(scriptInstance, "&openWindowsDropperDialog", this);
        Cortana.put(scriptInstance, "&openPowerShellWebDialog", this);
        Cortana.put(scriptInstance, "&openScriptedWebDialog", this);
        Cortana.put(scriptInstance, "&openBeaconConsole", this);
        Cortana.put(scriptInstance, "&openProcessBrowser", this);
        Cortana.put(scriptInstance, "&openFileBrowser", this);
        Cortana.put(scriptInstance, "&openCloneSiteDialog", this);
        Cortana.put(scriptInstance, "&openSystemProfilerDialog", this);
        Cortana.put(scriptInstance, "&openSpearPhishDialog", this);
        Cortana.put(scriptInstance, "&openPreferencesDialog", this);
        Cortana.put(scriptInstance, "&openScriptManager", this);
        Cortana.put(scriptInstance, "&openAboutDialog", this);
        Cortana.put(scriptInstance, "&openInterfaceManager", this);
        Cortana.put(scriptInstance, "&openScreenshotBrowser", this);
        Cortana.put(scriptInstance, "&openKeystrokeBrowser", this);
        Cortana.put(scriptInstance, "&openDownloadBrowser", this);
        Cortana.put(scriptInstance, "&openBrowserPivotSetup", this);
        Cortana.put(scriptInstance, "&openCovertVPNSetup", this);
        Cortana.put(scriptInstance, "&openSOCKSSetup", this);
        Cortana.put(scriptInstance, "&openPivotListenerSetup", this);
        Cortana.put(scriptInstance, "&openSOCKSBrowser", this);
        Cortana.put(scriptInstance, "&openGoldenTicketDialog", this);
        Cortana.put(scriptInstance, "&openMakeTokenDialog", this);
        Cortana.put(scriptInstance, "&openSpawnAsDialog", this);
        Cortana.put(scriptInstance, "&openCredentialManager", this);
        Cortana.put(scriptInstance, "&openApplicationManager", this);
        Cortana.put(scriptInstance, "&openJumpDialog", this);
        Cortana.put(scriptInstance, "&openTargetBrowser", this);
        Cortana.put(scriptInstance, "&openServiceBrowser", this);
        Cortana.put(scriptInstance, "&openPortScanner", this);
        Cortana.put(scriptInstance, "&openPortScannerLocal", this);
        Cortana.put(scriptInstance, "&openSystemInformationDialog", this);
        Cortana.put(scriptInstance, "&getAggressorClient", this);
        Cortana.put(scriptInstance, "&highlight", this);
        Cortana.put(scriptInstance, "&addVisualization", this);
        Cortana.put(scriptInstance, "&showVisualization", this);
        Cortana.put(scriptInstance, "&pgraph", this);
        Cortana.put(scriptInstance, "&tbrowser", this);
        Cortana.put(scriptInstance, "&bbrowser", this);
        Cortana.put(scriptInstance, "&sbrowser", this);
        Cortana.put(scriptInstance, "&colorPanel", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        try {
            if (string.equals("&openScriptConsole")) {
                Console console = (new CortanaConsole(this.engine)).getConsole();
                this.manager.addTab("Script Console", console, null, "Cortana script console");
                return SleepUtils.getScalar(console);
            }
            if (string.equals("&openEventLog")) {
                EventLog eventLog = new EventLog(this.client.getData(), this.engine, this.client.getConnection());
                Console console = eventLog.getConsole();
                this.manager.addTab("Event Log", console, eventLog.cleanup(), "Log of events/chat messages");
                return SleepUtils.getScalar(console);
            }
            if (string.equals("&openWebLog")) {
                WebLog webLog = new WebLog(this.client.getData(), this.engine, this.client.getConnection());
                Console console = webLog.getConsole();
                this.manager.addTab("Web Log", console, webLog.cleanup(), "Log of web server activity");
                return SleepUtils.getScalar(console);
            }
            if (string.equals("&openSiteManager")) {
                SiteManager siteManager = new SiteManager(this.client.getData(), this.engine, this.client.getConnection());
                this.manager.addTab("Sites", siteManager.getContent(), siteManager.cleanup(), "Manage Cobalt Strike's web server");
                return SleepUtils.getEmptyScalar();
            }
            if (string.equals("&openListenerManager")) {
                ListenerManager listenerManager = new ListenerManager(this.client);
                this.manager.addTab("Listeners", listenerManager.getContent(), listenerManager.cleanup(), "Manage Cobalt Strike's listeners");
                return SleepUtils.getEmptyScalar();
            }
            if (string.equals("&openCredentialManager")) {
                CredentialManager credentialManager = new CredentialManager(this.client);
                this.manager.addTab("Credentials", credentialManager.getContent(), credentialManager.cleanup(), "Manage credentials");
                return SleepUtils.getEmptyScalar();
            }
            if (string.equals("&openApplicationManager")) {
                ApplicationManager applicationManager = new ApplicationManager(this.client);
                this.manager.addTab("Applications", applicationManager.getContent(), applicationManager.cleanup(), "View system profiler results");
                return SleepUtils.getEmptyScalar();
            }
            if (string.equals("&openBeaconBrowser")) {
                BeaconBrowser beaconBrowser = new BeaconBrowser(this.client);
                this.manager.addTab("Beacons", beaconBrowser.getContent(), beaconBrowser.cleanup(), "Haters gonna hate, beacons gonna beacon");
                return SleepUtils.getEmptyScalar();
            }
            if (string.equals("&openTargetBrowser")) {
                TargetBrowser targetBrowser = new TargetBrowser(this.client);
                this.manager.addTab("Targets", targetBrowser.getContent(), targetBrowser.cleanup(), "Hosts that Cobalt Strike knows about");
                return SleepUtils.getEmptyScalar();
            }
            if (string.equals("&openServiceBrowser")) {
                String[] arrstring = CommonUtils.toStringArray(BridgeUtilities.getArray(stack));
                ServiceBrowser serviceBrowser = new ServiceBrowser(this.client, arrstring);
                this.manager.addTab("Services", serviceBrowser.getContent(), serviceBrowser.cleanup(), "Services known by Cobalt Strike");
                return SleepUtils.getEmptyScalar();
            }
            if (string.equals("&openPortScanner")) {
                String[] arrstring = CommonUtils.toStringArray(BridgeUtilities.getArray(stack));
                (new PortScanDialog(this.client, arrstring)).show();
                return SleepUtils.getEmptyScalar();
            }
            if (string.equals("&openPortScannerLocal")) {
                String str = BridgeUtilities.getString(stack, "");
                (new PortScanLocalDialog(this.client, str)).show();
                return SleepUtils.getEmptyScalar();
            }
            if (string.equals("&openBeaconConsole")) {
                String str = BridgeUtilities.getString(stack, "");
                BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), str);
                if (beaconEntry == null) {
                    throw new RuntimeException("No beacon entry for: '" + str + "'");
                }
                if (beaconEntry.isBeacon()) {
                    BeaconConsole beaconConsole = new BeaconConsole(str, this.client);
                    this.manager.addTab(beaconEntry.title(), beaconConsole.getConsole(), beaconConsole.cleanup(), "Beacon console");
                } else if (beaconEntry.isSSH()) {
                    SecureShellConsole secureShellConsole = new SecureShellConsole(str, this.client);
                    this.manager.addTab(beaconEntry.title(), secureShellConsole.getConsole(), secureShellConsole.cleanup(), "SSH console");
                }
            } else if (string.equals("&openProcessBrowser")) {
                String[] arrstring = BeaconBridge.bids(stack);
                if (arrstring.length == 1) {
                    BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), arrstring[0]);
                    ProcessBrowser processBrowser = new ProcessBrowser(this.client, arrstring[0]);
                    this.manager.addTab(beaconEntry.title("Processes"), processBrowser.getContent(), null, "Process Browser");
                } else {
                    ProcessBrowserMulti processBrowserMulti = new ProcessBrowserMulti(this.client, arrstring);
                    this.manager.addTab("Processes", processBrowserMulti.getContent(), null, "Process Browser");
                }
            } else if (string.equals("&openFileBrowser")) {
                String[] arrstring = BeaconBridge.bids(stack);
                if (arrstring.length == 1) {
                    BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), arrstring[0]);
                    FileBrowser fileBrowser = new FileBrowser(this.client, arrstring[0]);
                    this.manager.addTab(beaconEntry.title("Files"), fileBrowser.getContent(), null, "File Browser");
                }
            } else if (string.equals("&openBrowserPivotSetup")) {
                String str = BridgeUtilities.getString(stack, "");
                try {
                    new BrowserPivotSetup(this.client, str).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (string.equals("&openGoldenTicketDialog")) {
                String str = BridgeUtilities.getString(stack, "");
                try {
                    new GoldenTicketDialog(this.client, str).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (string.equals("&openMakeTokenDialog")) {
                String str = BridgeUtilities.getString(stack, "");
                new MakeTokenDialog(this.client, str).show();
            } else if (string.equals("&openSpawnAsDialog")) {
                String str = BridgeUtilities.getString(stack, "");
                new SpawnAsDialog(this.client, str).show();
            } else if (string.equals("&openJumpDialog")) {
                String str = BridgeUtilities.getString(stack, "");
                String[] arrstring = CommonUtils.toStringArray(BridgeUtilities.getArray(stack));
                if (str.equals("ssh")) {
                    new SecureShellDialog(this.client, arrstring).show();
                } else if (str.equals("ssh-key")) {
                    new SecureShellPubKeyDialog(this.client, arrstring).show();
                } else {
                    new JumpDialogAlt(this.client, arrstring, str).show();
                }
            } else if (string.equals("&openSOCKSSetup")) {
                String str = BridgeUtilities.getString(stack, "");
                new SOCKSSetup(this.client, str).show();
            } else if (string.equals("&openPivotListenerSetup")) {
                String str = BridgeUtilities.getString(stack, "");
                new PivotListenerSetup(this.client, str).show();
            } else if (string.equals("&openCovertVPNSetup")) {
                String str = BridgeUtilities.getString(stack, "");
                try {
                    new CovertVPNSetup(this.client, str).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (string.equals("&openScreenshotBrowser")) {
                ScreenshotBrowser screenshotBrowser = new ScreenshotBrowser(this.client.getData(), this.engine, this.client.getConnection());
                this.manager.addTab("Screenshots", screenshotBrowser.getContent(), screenshotBrowser.cleanup(), "Screenshot browser");
            } else if (string.equals("&openSOCKSBrowser")) {
                SOCKSBrowser sOCKSBrowser = new SOCKSBrowser(this.client);
                this.manager.addTab("Proxy Pivots", sOCKSBrowser.getContent(), sOCKSBrowser.cleanup(), "Beacon SOCKS Servers, port forwards, and reverse port forwards.");
            } else if (string.equals("&openKeystrokeBrowser")) {
                KeystrokeBrowser keystrokeBrowser = new KeystrokeBrowser(this.client.getData(), this.engine, this.client.getConnection());
                this.manager.addTab("Keystrokes", keystrokeBrowser.getContent(), keystrokeBrowser.cleanup(), "Keystroke browser");
            } else if (string.equals("&openDownloadBrowser")) {
                DownloadBrowser downloadBrowser = new DownloadBrowser(this.client.getData(), this.engine, this.client.getConnection());
                this.manager.addTab("Downloads", downloadBrowser.getContent(), downloadBrowser.cleanup(), "Downloads browser");
            } else if (string.equals("&openConnectDialog")) {
                try {
                    new ConnectDialog(this.window).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (string.equals("&openHostFileDialog")) {
                try {
                    new HostFileDialog(this.window, this.conn, this.client.getData()).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (string.equals("&openCloneSiteDialog")) {
                try {
                    new CloneSiteDialog(this.window, this.conn, this.client.getData()).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (string.equals("&openSystemProfilerDialog")) {
                new SystemProfilerDialog(this.window, this.conn, this.client.getData()).show();
            } else if (string.equals("&openSpearPhishDialog")) {
                new SpearPhishDialog(this.client, this.window, this.conn, this.client.getData()).show();
            } else if (string.equals("&closeClient")) {
                this.client.kill();
            } else if (string.equals("&openWindowsExecutableStageDialog")) {
                new WindowsExecutableStageDialog(this.client).show();
            } else if (string.equals("&openAutoRunDialog")) {
                try {
                    new AutoRunDialog(this.window, this.conn).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (string.equals("&openPayloadHelper")) {
                final SleepClosure f = BridgeUtilities.getFunction(stack, scriptInstance);
                ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {
                    public void dialogResult(String string) {
                        Stack stack = new Stack();
                        stack.push(SleepUtils.getScalar(string));
                        SleepUtils.runCode(f, "dialogResult", null, stack);
                    }
                }).show();
            } else if (string.equals("&openWindowsExecutableDialog")) {
                new WindowsExecutableDialog(this.client).show();
            } else if (string.equals("&openPayloadGeneratorDialog")) {
                new PayloadGeneratorDialog(this.client).show();
            } else if (string.equals("&openPayloadGeneratorStageDialog")) {
                new PayloadGeneratorStageDialog(this.client).show();
            } else if (string.equals("&openOfficeMacroDialog")) {
                new OfficeMacroDialog(this.client).show();
            } else if (string.equals("&openJavaSignedAppletDialog")) {
                new JavaSignedAppletDialog(this.client).show();
            } else if (string.equals("&openJavaSmartAppletDialog")) {
                new JavaSmartAppletDialog(this.client).show();
            } else if (string.equals("&openHTMLApplicationDialog")) {
                new HTMLApplicationDialog(this.client).show();
            } else if (string.equals("&openWindowsDropperDialog")) {
                new WindowsDropperDialog(this.client).show();
            } else if (string.equals("&openPowerShellWebDialog")) {
                new ScriptedWebDialog(this.client).show();
            } else if (string.equals("&openScriptedWebDialog")) {
                new ScriptedWebStageDialog(this.client).show();
            } else if (string.equals("&openPreferencesDialog")) {
                new PreferencesDialog().show();
            } else if (string.equals("&openAboutDialog")) {
                new AboutDialog().show();
            } else if (string.equals("&openScriptManager")) {
                ScriptManager scriptManager = new ScriptManager(this.client);
                this.manager.addTab("Scripts", scriptManager.getContent(), null, "Manage your Aggressor scripts.");
            } else if (string.equals("&openInterfaceManager")) {
                InterfaceManager interfaceManager = new InterfaceManager(this.client.getData(), this.engine, this.client.getConnection());
                this.manager.addTab("Interfaces", interfaceManager.getContent(), interfaceManager.cleanup(), "Manage Covert VPN Interfaces");
            } else if (string.equals("&openSystemInformationDialog")) {
                new SystemInformationDialog(this.client).show();
            } else if (string.equals("&addVisualization")) {
                String str = BridgeUtilities.getString(stack, "");
                JComponent jComponent = (JComponent) BridgeUtilities.getObject(stack);
                this.client.addViz(str, jComponent);
            } else if (string.equals("&showVisualization")) {
                String str = BridgeUtilities.getString(stack, "");
                this.client.showViz(str);
            } else {
                if (string.equals("&pgraph")) {
                    PivotGraph pivotGraph = new PivotGraph(this.client);
                    pivotGraph.ready();
                    return SleepUtils.getScalar(pivotGraph.getContent());
                }
                if (string.equals("&tbrowser")) {
                    Targets targets = new Targets(this.client);
                    JComponent jComponent = targets.getContent();
                    DialogUtils.setupScreenshotShortcut(this.client, targets.getTable(), "Targets");
                    return SleepUtils.getScalar(jComponent);
                }
                if (string.equals("&bbrowser")) {
                    Beacons beacons = new Beacons(this.client, true);
                    JComponent jComponent = beacons.getContent();
                    DialogUtils.setupScreenshotShortcut(this.client, beacons.getTable(), "Beacons");
                    return SleepUtils.getScalar(jComponent);
                }
                if (string.equals("&sbrowser")) {
                    Sessions sessions = new Sessions(this.client, true);
                    JComponent jComponent = sessions.getContent();
                    DialogUtils.setupScreenshotShortcut(this.client, sessions.getTable(), "Sessions");
                    return SleepUtils.getScalar(jComponent);
                }
                if (string.equals("&colorPanel")) {
                    String str = BridgeUtilities.getString(stack, "");
                    String[] arrstring = ScriptUtils.ArrayOrString(stack);
                    ColorManagerScripted colorManagerScripted = new ColorManagerScripted(this.client, str, arrstring);
                    return SleepUtils.getScalar(colorManagerScripted.getColorPanel());
                }
                if (string.equals("&getAggressorClient")) {
                    return SleepUtils.getScalar(this.client);
                }
                if (string.equals("&highlight")) {
                    String str1 = BridgeUtilities.getString(stack, "");
                    List<Map> list = SleepUtils.getListFromArray(BridgeUtilities.getArray(stack));
                    String str2 = BridgeUtilities.getString(stack, "");
                    HashMap hashMap = new HashMap();
                    hashMap.put("_accent", str2);
                    for (Map map : list) {
                        client.getConnection().call(str1 + ".update",
                                CommonUtils.args(Keys.ToKey(str1, map), hashMap));
                    }
                    client.getConnection().call(str1 + ".push");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return SleepUtils.getEmptyScalar();

    }
}
