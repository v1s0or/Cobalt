package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.BeaconCommands;
import beacon.SecureShellTabCompletion;
import common.BeaconOutput;
import common.Callback;
import common.CommandParser;
import common.CommonUtils;
import console.Colors;
import console.GenericTabCompletion;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Stack;
import javax.swing.JTextField;

import sleep.runtime.SleepUtils;

public class SecureShellConsole extends BeaconConsole {
    public SecureShellConsole(String string, AggressorClient aggressorClient) {
        super(string, aggressorClient);
    }

    public String getPrompt() {
        return Colors.underline("ssh") + "> ";
    }

    public String Script(String string) {
        return "SSH_" + string;
    }

    public GenericTabCompletion getTabCompletion() {
        return new SecureShellTabCompletion(this.bid, this.client, this.console);
    }

    public void showPopup(String string, MouseEvent mouseEvent) {
        Stack stack = new Stack();
        LinkedList linkedList = new LinkedList();
        linkedList.add(this.bid);
        stack.push(SleepUtils.getArrayWrapper(linkedList));
        this.engine.getMenuBuilder().installMenu(mouseEvent, "ssh", stack);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        String str = actionEvent.getActionCommand().trim();
        ((JTextField) actionEvent.getSource()).setText("");
        CommandParser commandParser = new CommandParser(str);
        if (this.client.getSSHAliases().isAlias(commandParser.getCommand())) {
            this.master.input(str);
            this.client.getSSHAliases().fireCommand(this.bid, commandParser.getCommand(), commandParser.getArguments());
            return;
        }
        if (commandParser.is("help") || commandParser.is("?")) {
            this.console.append(formatLocal(BeaconOutput.Input(this.bid, str)) + "\n");
            if (commandParser.verify("Z") || commandParser.reset()) {
                String str1 = commandParser.popString();
                BeaconCommands beaconCommands = DataUtils.getSSHCommands(this.data);
                if (beaconCommands.isHelpAvailable(str1)) {
                    Stack stack = new Stack();
                    stack.push(SleepUtils.getScalar(str1));
                    this.console.append(this.engine.format("SSH_OUTPUT_HELP_COMMAND", stack) + "\n");
                } else {
                    commandParser.error("no help is available for '" + str1 + "'");
                }
            } else {
                this.console.append(this.engine.format("SSH_OUTPUT_HELP", new Stack()) + "\n");
            }
            if (commandParser.hasError())
                this.console.append(formatLocal(BeaconOutput.Error(this.bid, commandParser.error())) + "\n");
            return;
        }
        if (commandParser.is("downloads")) {
            this.console.append(formatLocal(BeaconOutput.Input(this.bid, str)) + "\n");
            this.conn.call("beacons.downloads", CommonUtils.args(this.bid), new Callback() {
                public void result(String string, Object object) {
                    Stack stack = new Stack();
                    stack.push(CommonUtils.convertAll(object));
                    stack.push(SleepUtils.getScalar(SecureShellConsole.this.bid));
                    SecureShellConsole.this.console.append(SecureShellConsole.this.engine.format("BEACON_OUTPUT_DOWNLOADS", stack) + "\n");
                }
            });
            return;
        }
        this.master.input(str);
        if (commandParser.is("cancel")) {
            if (commandParser.verify("Z"))
                this.master.Cancel(commandParser.popString());
        } else if (commandParser.is("cd")) {
            if (commandParser.verify("Z"))
                this.master.Cd(commandParser.popString());
        } else if (commandParser.is("clear")) {
            this.master.Clear();
        } else if (commandParser.is("connect")) {
            if (commandParser.verify("AI") || commandParser.reset()) {
                int i = commandParser.popInt();
                String str1 = commandParser.popString();
                this.master.Connect(str1, i);
            } else if (commandParser.verify("Z")) {
                String str1 = commandParser.popString();
                this.master.Connect(str1);
            }
        } else if (commandParser.is("download")) {
            if (commandParser.verify("Z"))
                this.master.Download(commandParser.popString());
        } else if (commandParser.is("exit")) {
            this.master.Die();
        } else if (commandParser.is("getuid")) {
            this.master.GetUID();
        } else if (commandParser.is("note")) {
            if (commandParser.verify("Z")) {
                String str1 = commandParser.popString();
                this.master.Note(str1);
            } else if (commandParser.isMissingArguments()) {
                this.master.Note("");
            }
        } else if (commandParser.is("pwd")) {
            this.master.Pwd();
        } else if (commandParser.is("rportfwd")) {
            if (commandParser.verify("IAI") || commandParser.reset()) {
                int i = commandParser.popInt();
                String str1 = commandParser.popString();
                int j = commandParser.popInt();
                this.master.PortForward(j, str1, i);
            } else if (commandParser.verify("AI")) {
                int i = commandParser.popInt();
                String str1 = commandParser.popString();
                if (!"stop".equals(str1)) {
                    commandParser.error("only acceptable argument is stop");
                } else {
                    this.master.PortForwardStop(i);
                }
            }
        } else if (commandParser.is("shell")) {
            if (commandParser.verify("Z"))
                this.master.Shell(commandParser.popString());
        } else if (commandParser.is("sleep")) {
            if (commandParser.verify("I%") || commandParser.reset()) {
                int i = commandParser.popInt();
                int j = commandParser.popInt();
                this.master.Sleep(j, i);
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
        } else if (commandParser.is("sudo")) {
            if (commandParser.verify("AZ")) {
                String str1 = commandParser.popString();
                String str2 = commandParser.popString();
                this.master.ShellSudo(str2, str1);
            }
        } else if (commandParser.is("unlink")) {
            if (commandParser.verify("AI") || commandParser.reset()) {
                String str1 = commandParser.popString();
                String str2 = commandParser.popString();
                this.master.Unlink(str2, str1);
            } else if (commandParser.verify("Z")) {
                this.master.Unlink(commandParser.popString());
            }
        } else if (commandParser.is("upload") && commandParser.empty()) {
            SafeDialogs.openFile("Select file to upload", null, null, false, false, new SafeDialogCallback() {
                public void dialogResult(String string) {
                    if (CommonUtils.lof(string) > 786432L) {
                        SecureShellConsole.this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(SecureShellConsole.this.bid, "File " + string + " is larger than 768KB")));
                    } else {
                        SecureShellConsole.this.master.Upload(string);
                    }
                }
            });
        } else if (commandParser.is("upload")) {
            if (commandParser.verify("F")) {
                String str1 = commandParser.popString();
                if (CommonUtils.lof(str1) > 786432L) {
                    this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, "File " + str1 + " is larger than 768KB")));
                } else {
                    this.master.Upload(str1);
                }
            }
        } else {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, "Unknown command: " + str)));
        }
        if (commandParser.hasError())
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, commandParser.error())));
    }
}
