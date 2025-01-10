package aggressor;

import aggressor.dialogs.ConnectDialog;
import aggressor.ui.UseSynthetica;
import common.Authorization;
import common.License;
import common.Requirements;
import sleep.parser.ParserConfig;

import java.io.IOException;

public class Aggressor {

    public static final String VERSION = "4.0 (20191205) " + (License.isTrial() ? "Trial" : "Licensed");
    public static MultiFrame frame = null;

    public static MultiFrame getFrame() {
        return frame;
    }

    public static void main(String[] args) throws IOException {
        ParserConfig.installEscapeConstant('c', "\003");
        ParserConfig.installEscapeConstant('U', "\037");
        ParserConfig.installEscapeConstant('o', "\017");
        new UseSynthetica().setup();
        Requirements.checkGUI();
        // License.checkLicenseGUI(new Authorization());
        frame = new MultiFrame();
        new ConnectDialog(frame).show();
    }
}
