package aggressor.dialogs;

import aggressor.AggressorClient;

import java.util.Stack;

public class JavaSignedAppletDialog extends JavaAppletDialog {

    public JavaSignedAppletDialog(AggressorClient aggressorClient) {
        super(aggressorClient);
    }

    public String getResourceName() {
        String str = this.client.getScriptEngine().format("SIGNED_APPLET_RESOURCE", new Stack());
        return (str != null) ? str : "resources/applet_signed.jar";
    }

    public String getMainClass() {
        String str = this.client.getScriptEngine().format("SIGNED_APPLET_MAINCLASS", new Stack());
        return (str != null) ? str : "Java.class";
    }

    public String getShortDescription() {
        return "signed applet";
    }

    public String getTitle() {
        return "Self-signed Applet Attack";
    }

    public String getURL() {
        return "https://www.cobaltstrike.com/help-java-signed-applet-attack";
    }

    public String getDescription() {
        return "This package sets up a self-signed Java applet. This package will spawn the specified listener if the user gives the applet permission to run.";
    }

    public String getDefaultURL() {
        return "/mPlayer";
    }
}
