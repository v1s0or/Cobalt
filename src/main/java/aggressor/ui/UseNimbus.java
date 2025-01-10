package aggressor.ui;

import javax.swing.UIManager;

public class UseNimbus extends UseLookAndFeel {

    public void setup() {
        try {
            for (UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(lookAndFeelInfo.getName())) {
                    UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
                    break;
                }
            }
        } catch (Exception exception) {
        }
    }
}
