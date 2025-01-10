package common;

import javax.swing.JOptionPane;

public class License {

    public static void checkLicenseGUI(Authorization authorization) {
        if (!authorization.isValid()) {
            CommonUtils.print_error("Your authorization file is not valid: " + authorization.getError());
            JOptionPane.showMessageDialog(null, "Your authorization file is not valid.\n" + authorization.getError(), null, 0);
            System.exit(0);
        }
        if (authorization.isPerpetual()) {
            return;
        }
        if (authorization.isExpired()) {
            CommonUtils.print_error("Your Cobalt Strike license is expired. Please contact sales@strategiccyber.com to renew. If you did renew, run the update program to refresh your authorization file.");
            JOptionPane.showMessageDialog(null, "Your Cobalt Strike license is expired.\nPlease contact sales@strategiccyber.com to renew\n\nIf you did renew, run the update program to refresh your\nauthorization file.", null, 0);
            System.exit(0);
        }
        if (authorization.isAlmostExpired()) {
            CommonUtils.print_warn("Your Cobalt Strike license expires in " + authorization.whenExpires() + ". Email sales@strategiccyber.com to renew. If you did renew, run the update program to refresh your authorization file.");
            JOptionPane.showMessageDialog(null, "Your Cobalt Strike license expires in " + authorization.whenExpires() + "\nEmail sales@strategiccyber.com to renew\n\nIf you did renew, run the update program to refresh your\nauthorization file.", null, 1);
        }
    }

    public static boolean isTrial() {
        // return false;
        return true;
    }

    public static void checkLicenseConsole(Authorization authorization) {
        if (!authorization.isValid()) {
            CommonUtils.print_error("Your authorization file is not valid: " + authorization.getError());
            System.exit(0);
        }
        if (authorization.isPerpetual())
            return;
        if (authorization.isExpired()) {
            CommonUtils.print_error("Your Cobalt Strike license is expired. Please contact sales@strategiccyber.com to renew. If you did renew, run the update program to refresh your authorization file.");
            System.exit(0);
        }
        if (authorization.isAlmostExpired())
            CommonUtils.print_warn("Your Cobalt Strike license expires in " + authorization.whenExpires() + ". Email sales@strategiccyber.com to renew. If you did renew, run the update program to refresh your authorization file.");
    }
}
