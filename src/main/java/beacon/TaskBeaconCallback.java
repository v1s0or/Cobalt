package beacon;

public class TaskBeaconCallback {
    protected CommandBuilder builder = new CommandBuilder();

    protected byte[] taskNoArgsCallback(int n1, int n2) {
        this.builder.setCommand(n1);
        this.builder.addInteger(n2);
        return this.builder.build();
    }

    public byte[] IPConfig(int n) {
        return taskNoArgsCallback(48, n);
    }

    public byte[] Ps(int n) {
        return taskNoArgsCallback(32, n);
    }

    public byte[] Ls(int n, String string) {
        this.builder.setCommand(53);
        this.builder.addInteger(n);
        if (string.endsWith("\\")) {
            this.builder.addLengthAndString(string + "*");
        } else {
            this.builder.addLengthAndString(string + "\\*");
        }
        return this.builder.build();
    }

    public byte[] Drives(int n) {
        return taskNoArgsCallback(55, n);
    }
}
