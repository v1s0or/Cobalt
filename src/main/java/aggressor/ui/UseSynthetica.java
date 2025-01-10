package aggressor.ui;

import aggressor.Prefs;
import de.javasoft.plaf.synthetica.SyntheticaLookAndFeel;

import java.awt.Font;
import javax.swing.UIManager;

public class UseSynthetica extends UseLookAndFeel {

    public void setup() {
        try {
            SyntheticaLookAndFeel.setWindowsDecorated(false);
            set("Synthetica.extendedFileChooser.rememberPreferences", false);
            set("Synthetica.font.enabled", true);
            set("Synthetica.text.antialias", false);
            set("Synthetica.textArea.border.opaqueBackground", false);
            UIManager.put("Synthetica.font.scaleFactor", 115);
            set("Synthetica.font.respectSystemDPI", false);
            //todo UI set SyntheticaStandardLookAndFeel
            // UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlueIceLookAndFeel");
            UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
            Font font = Prefs.getPreferences().getFont("client.font.font", "Dialog-PLAIN-12");
            SyntheticaLookAndFeel.setFont(font, true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    static {
        String[] arrstring = {"Licensee=Strategic Cyber LLC", "LicenseRegistrationNumber=404478475", "Product=Synthetica", "LicenseType=Small Business License", "ExpireDate=--.--.----", "MaxVersion=2.30.999"};
        UIManager.put("Synthetica.license.info", arrstring);
        UIManager.put("Synthetica.license.key", "D6363B2A-F83CD00A-C4EB6105-31B2770B");
    }
}
