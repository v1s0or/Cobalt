package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.dialogs.BypassUACDialog;
import aggressor.dialogs.ElevateDialog;
import aggressor.dialogs.OneLinerDialog;
import beacon.BeaconCommands;
import beacon.EncodedCommandBuilder;
import beacon.PowerShellTasks;
import beacon.TaskBeacon;
import common.BeaconEntry;
import common.Callback;
import common.CommonUtils;
import common.ListenerUtils;
import common.ScListener;
import common.TeamQueue;
import cortana.Cortana;
import dialog.DialogUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.interfaces.Predicate;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class BeaconBridge implements Function, Loadable, Predicate {

    protected Cortana engine;

    protected TeamQueue conn;

    protected AggressorClient client;

    public BeaconBridge(AggressorClient aggressorClient, Cortana cortana, TeamQueue teamQueue) {
        this.client = aggressorClient;
        this.engine = cortana;
        this.conn = teamQueue;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&externalc2_start", this);
        Cortana.put(scriptInstance, "&beacon_commands", this);
        Cortana.put(scriptInstance, "&ssh_commands", this);
        Cortana.put(scriptInstance, "&beacon_command_describe", this);
        Cortana.put(scriptInstance, "&ssh_command_describe", this);
        Cortana.put(scriptInstance, "&beacon_command_detail", this);
        Cortana.put(scriptInstance, "&ssh_command_detail", this);
        Cortana.put(scriptInstance, "&beacons", this);
        Cortana.put(scriptInstance, "&beacon_data", this);
        Cortana.put(scriptInstance, "&bdata", this);
        Cortana.put(scriptInstance, "&beacon_info", this);
        Cortana.put(scriptInstance, "&binfo", this);
        Cortana.put(scriptInstance, "&beacon_note", this);
        Cortana.put(scriptInstance, "&beacon_remove", this);
        Cortana.put(scriptInstance, "&beacon_command_register", this);
        Cortana.put(scriptInstance, "&ssh_command_register", this);
        Cortana.put(scriptInstance, "&beacon_ids", this);
        Cortana.put(scriptInstance, "&beacon_host_script", this);
        Cortana.put(scriptInstance, "&beacon_host_imported_script", this);
        Cortana.put(scriptInstance, "&beacon_execute_job", this);
        Cortana.put(scriptInstance, "&barch", this);
        Cortana.put(scriptInstance, "&beacon_stage_tcp", this);
        Cortana.put(scriptInstance, "&beacon_stage_pipe", this);
        Cortana.put(scriptInstance, "&bls", this);
        Cortana.put(scriptInstance, "&bps", this);
        Cortana.put(scriptInstance, "&bipconfig", this);
        Cortana.put(scriptInstance, "&openOrActivate", this);
        Cortana.put(scriptInstance, "&openBypassUACDialog", this);
        Cortana.put(scriptInstance, "&openElevateDialog", this);
        Cortana.put(scriptInstance, "&openOneLinerDialog", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("-isssh", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("-isbeacon", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("-isadmin", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("-is64", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("-isactive", this);
    }

    public boolean decide(String string, ScriptInstance scriptInstance, Stack stack) {
        String str = BridgeUtilities.getString(stack, "");
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), str);
        if (beaconEntry == null) {
            return false;
        }
        if ("-isssh".equals(string)) {
            return beaconEntry.isSSH();
        }
        if ("-isbeacon".equals(string)) {
            return beaconEntry.isBeacon();
        }
        if ("-isadmin".equals(string)) {
            return beaconEntry.isAdmin();
        }
        if ("-is64".equals(string)) {
            return beaconEntry.is64();
        }
        if ("-isactive".equals(string)) {
            return beaconEntry.isActive();
        }
        return false;
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public static String[] bids(Stack stack) {
        if (stack.isEmpty()) {
            return new String[0];
        }
        Scalar scalar = (Scalar) stack.peek();
        if (scalar.getArray() != null) {
            return CommonUtils.toStringArray(BridgeUtilities.getArray(stack));
        }
        return new String[]{((Scalar) stack.pop()).stringValue()};
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        try {


            if (string.equals("&externalc2_start")) {
                final String bid = BridgeUtilities.getString(stack, "0.0.0.0");
                int i = BridgeUtilities.getInt(stack, 2222);
                this.conn.call("exoticc2.start", CommonUtils.args(bid, Integer.valueOf(i)));
                return SleepUtils.getEmptyScalar();
            }
            if (string.equals("&beacon_commands")) {
                BeaconCommands beaconCommands = DataUtils.getBeaconCommands(this.client.getData());
                return SleepUtils.getArrayWrapper(beaconCommands.commands());
            }
            if (string.equals("&ssh_commands")) {
                BeaconCommands beaconCommands = DataUtils.getSSHCommands(this.client.getData());
                return SleepUtils.getArrayWrapper(beaconCommands.commands());
            }
            if (string.equals("&beacon_command_describe")) {
                final String bid = BridgeUtilities.getString(stack, "");
                BeaconCommands beaconCommands = DataUtils.getBeaconCommands(this.client.getData());
                return SleepUtils.getScalar(beaconCommands.getDescription(bid));
            }
            if (string.equals("&ssh_command_describe")) {
                final String bid = BridgeUtilities.getString(stack, "");
                BeaconCommands beaconCommands = DataUtils.getSSHCommands(this.client.getData());
                return SleepUtils.getScalar(beaconCommands.getDescription(bid));
            }
            if (string.equals("&beacon_command_detail")) {
                final String bid = BridgeUtilities.getString(stack, "");
                BeaconCommands beaconCommands = DataUtils.getBeaconCommands(this.client.getData());
                return SleepUtils.getScalar(beaconCommands.getDetails(bid));
            }
            if (string.equals("&ssh_command_detail")) {
                final String bid = BridgeUtilities.getString(stack, "");
                BeaconCommands beaconCommands = DataUtils.getSSHCommands(this.client.getData());
                return SleepUtils.getScalar(beaconCommands.getDetails(bid));
            }
            if (string.equals("&beacon_command_register")) {
                final String bid = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                String str3 = BridgeUtilities.getString(stack, "");
                BeaconCommands beaconCommands = DataUtils.getBeaconCommands(this.client.getData());
                beaconCommands.register(bid, str2, str3);
            } else if (string.equals("&ssh_command_register")) {
                final String bid = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                String str3 = BridgeUtilities.getString(stack, "");
                BeaconCommands beaconCommands = DataUtils.getSSHCommands(this.client.getData());
                beaconCommands.register(bid, str2, str3);
            } else if (string.equals("&beacon_note")) {
                String[] arrstring = bids(stack);
                final String bid = BridgeUtilities.getString(stack, "");
                for (String s : arrstring) {
                    this.conn.call("beacons.note", CommonUtils.args(s, bid));
                }
            } else if (string.equals("&beacon_remove")) {
                String[] arrstring = bids(stack);
                for (String s : arrstring) {
                    this.conn.call("beacons.remove", CommonUtils.args(s));
                }
            } else {
                if (string.equals("&beacons")) {
                    Map map = DataUtils.getBeacons(this.client.getData());
                    return CommonUtils.convertAll(new LinkedList(map.values()));
                }
                if (string.equals("&beacon_ids")) {
                    Map map = DataUtils.getBeacons(this.client.getData());
                    return CommonUtils.convertAll(new LinkedList(map.keySet()));
                }
                if (string.equals("&beacon_execute_job")) {
                    String[] arrstring = bids(stack);
                    final String bid = BridgeUtilities.getString(stack, "");
                    String str2 = BridgeUtilities.getString(stack, "");
                    int i = BridgeUtilities.getInt(stack, 0);
                    for (String s : arrstring) {
                        EncodedCommandBuilder encodedCommandBuilder = new EncodedCommandBuilder(this.client);
                        encodedCommandBuilder.setCommand(78);
                        encodedCommandBuilder.addLengthAndEncodedString(s, bid);
                        encodedCommandBuilder.addLengthAndEncodedString(s, str2);
                        encodedCommandBuilder.addShort(i);
                        byte[] arrby = encodedCommandBuilder.build();
                        this.conn.call("beacons.task", CommonUtils.args(s, arrby));
                    }
                } else {
                    if (string.equals("&beacon_host_imported_script")) {
                        final String bid = BridgeUtilities.getString(stack, "");
                        return SleepUtils.getScalar((new PowerShellTasks(this.client, bid)).getImportCradle());
                    }
                    if (string.equals("&beacon_host_script")) {
                        final String bid = BridgeUtilities.getString(stack, "");
                        String str2 = BridgeUtilities.getString(stack, "");
                        return SleepUtils.getScalar((new PowerShellTasks(this.client, bid)).getScriptCradle(str2));
                    }
                    if (string.equals("&barch")) {
                        final String bid = BridgeUtilities.getString(stack, "");
                        BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), bid);
                        return (beaconEntry == null) ? SleepUtils.getScalar("x86") : SleepUtils.getScalar(beaconEntry.arch());
                    }
                    if (string.equals("&beacon_info") || string.equals("&binfo")) {
                        final String bid = BridgeUtilities.getString(stack, "");
                        BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), bid);
                        if (beaconEntry == null) {
                            return SleepUtils.getEmptyScalar();
                        }
                        if (!stack.isEmpty()) {
                            final String bid1 = BridgeUtilities.getString(stack, "");
                            return CommonUtils.convertAll(beaconEntry.toMap().get(bid1));
                        }
                        return CommonUtils.convertAll(beaconEntry.toMap());
                    }
                    if (string.equals("&beacon_data") || string.equals("&bdata")) {
                        final String bid = BridgeUtilities.getString(stack, "");
                        String str2 = BridgeUtilities.getString(stack, "");
                        BeaconEntry beaconEntry = DataUtils.getBeacon(client.getData(), bid);
                        if (beaconEntry == null) {
                            return SleepUtils.getEmptyScalar();
                        }
                        return CommonUtils.convertAll(beaconEntry.toMap());
                    }
                    if (string.equals("&bipconfig")) {
                        String[] arrstring = bids(stack);
                        final SleepClosure f = BridgeUtilities.getFunction(stack, scriptInstance);
                        for (final String bid : arrstring) {
                            conn.call("beacons.task_ipconfig",
                                    CommonUtils.args(bid), new Callback() {

                                        @Override
                                        public void result(String string, Object object) {
                                            Stack stack = new Stack();
                                            stack.push(CommonUtils.convertAll(object));
                                            stack.push(SleepUtils.getScalar(bid));
                                            SleepUtils.runCode(f, string, null, stack);
                                        }
                                    });
                        }
                    } else if (string.equals("&bls")) {
                        String[] arrstring = bids(stack);
                        final String bid = BridgeUtilities.getString(stack, ".");
                        if (!stack.isEmpty()) {
                            final SleepClosure f = BridgeUtilities
                                    .getFunction(stack, scriptInstance);
                            for (int i = 0; i < arrstring.length; i++) {
                                final String bid1 = arrstring[i];
                                this.conn.call("beacons.task_ls",
                                        CommonUtils.args(arrstring[i], bid), new Callback() {

                                            @Override
                                            public void result(String string, Object object) {
                                                Stack stack = new Stack();
                                                stack.push(CommonUtils.convertAll(object));
                                                // stack.push(SleepUtils.getScalar(folder));
                                                stack.push(SleepUtils.getScalar(bid));
                                                stack.push(SleepUtils.getScalar(bid1));
                                                SleepUtils.runCode(f, string, null, stack);
                                            }
                                        });
                            }
                        } else {
                            TaskBeacon taskBeacon = new TaskBeacon(
                                    client, client.getData(), conn, arrstring);
                            taskBeacon.Ls(bid);
                        }
                    } else if (string.equals("&bps")) {
                        String[] arrstring = bids(stack);
                        if (stack.isEmpty()) {
                            TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(), this.conn, arrstring);
                            taskBeacon.Ps();
                        } else {
                            final SleepClosure f = BridgeUtilities
                                    .getFunction(stack, scriptInstance);
                            for (int i = 0; i < arrstring.length; i++) {
                                final String bid = arrstring[i];
                                this.conn.call("beacons.task_ps",
                                        CommonUtils.args(arrstring[i]), new Callback() {

                                            @Override
                                            public void result(String string, Object object) {
                                                Stack stack = new Stack();
                                                stack.push(CommonUtils.convertAll(object));
                                                stack.push(SleepUtils.getScalar(bid));
                                                SleepUtils.runCode(f, string, null, stack);
                                            }
                                        });
                            }
                        }
                    } else if (string.equals("&beacon_stage_tcp")) {
                        final String bid = BridgeUtilities.getString(stack, "");
                        String str2 = BridgeUtilities.getString(stack, "127.0.0.1");
                        int i = BridgeUtilities.getInt(stack, 0);
                        String str3 = BridgeUtilities.getString(stack, "");
                        String str4 = BridgeUtilities.getString(stack, "x86");
                        ScListener scListener = ListenerUtils.getListener(client, str3);
                        TaskBeacon taskBeacon = new TaskBeacon(
                                client, client.getData(), conn, new String[]{bid});
                        taskBeacon.StageTCP(bid, str2, i, str4, scListener);
                    } else if (string.equals("&beacon_stage_pipe")) {
                        final String bid = BridgeUtilities.getString(stack, "");
                        String str2 = BridgeUtilities.getString(stack, "127.0.0.1");
                        String str3 = BridgeUtilities.getString(stack, "");
                        String str4 = BridgeUtilities.getString(stack, "x86");
                        ScListener scListener = ListenerUtils.getListener(client, str3);
                        String str5 = scListener.getConfig().getStagerPipe();
                        TaskBeacon taskBeacon = new TaskBeacon(
                                client, client.getData(), conn, new String[]{bid});
                        taskBeacon.StagePipe(bid, str2, str5, str4, scListener);
                    } else if (string.equals("&openOrActivate")) {
                        String[] arrstring = bids(stack);
                        if (arrstring.length == 1) {
                            DialogUtils.openOrActivate(this.client, arrstring[0]);
                        }
                    } else if (string.equals("&openBypassUACDialog")) {
                        String[] arrstring = bids(stack);
                        try {
                            new BypassUACDialog(this.client, arrstring).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (string.equals("&openElevateDialog")) {
                        String[] arrstring = bids(stack);
                        try {
                            new ElevateDialog(this.client, arrstring).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (string.equals("&openOneLinerDialog")) {
                        String[] arrstring = bids(stack);
                        new OneLinerDialog(this.client, arrstring).show();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SleepUtils.getEmptyScalar();
    }
}
