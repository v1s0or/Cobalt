package dialog;

import common.CommonUtils;

import java.awt.Color;
import java.io.File;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class SafeDialogs {

    protected static File lastSaveDirectory = null;

    protected static File lastOpenDirectory = null;

    public static void askYesNo(final String text, final String title, final SafeDialogCallback callback) {
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                int n = JOptionPane.showConfirmDialog(null, text, title, 0);
                if (n == 0 || n == 0) {
                    SafeDialogs.post(callback, "yes");
                }
            }
        });
    }

    private static void post(SafeDialogCallback callback, String string) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                callback.dialogResult(string);
            }
        }, "dialog result thread").start();
    }

    public static void ask(final String text, final String initial, final SafeDialogCallback callback) {
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                String str = JOptionPane.showInputDialog(text, initial);
                if (str != null) {
                    SafeDialogs.post(callback, str);
                }
            }
        });
    }

    public static void saveFile(final JFrame frame, final String selection, final SafeDialogCallback callback) {
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                JFileChooser jFileChooser = new JFileChooser();
                if (selection != null) {
                    jFileChooser.setSelectedFile(new File(selection));
                }
                if (SafeDialogs.lastSaveDirectory != null) {
                    jFileChooser.setCurrentDirectory(SafeDialogs.lastSaveDirectory);
                }
                if (jFileChooser.showSaveDialog(frame) == 0) {
                    File file = jFileChooser.getSelectedFile();
                    if (file != null) {
                        if (file.isDirectory()) {
                            SafeDialogs.lastSaveDirectory = file;
                        } else {
                            SafeDialogs.lastSaveDirectory = file.getParentFile();
                        }
                        SafeDialogs.post(callback, file + "");
                        return;
                    }
                }
            }
        });
    }

    public static void chooseColor(final String title, final Color defaultv, final SafeDialogCallback callback) {
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                Color color = JColorChooser.showDialog(null, title, defaultv);
                if (color != null) {
                    SafeDialogs.post(callback, DialogUtils.encodeColor(color));
                }
            }
        });
    }

    public static void openFile(final String title, final String sel, final String dir, final boolean multi, final boolean dirsonly, final SafeDialogCallback callback) {
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                JFileChooser jFileChooser = new JFileChooser();
                if (title != null) {
                    jFileChooser.setDialogTitle(title);
                }
                if (sel != null) {
                    jFileChooser.setSelectedFile(new File(sel));
                }
                if (dir != null) {
                    jFileChooser.setCurrentDirectory(new File(dir));
                } else if (SafeDialogs.lastOpenDirectory != null) {
                    jFileChooser.setCurrentDirectory(SafeDialogs.lastOpenDirectory);
                }
                jFileChooser.setMultiSelectionEnabled(multi);
                if (dirsonly) {
                    jFileChooser.setFileSelectionMode(1);
                }
                if (jFileChooser.showOpenDialog(null) != 0) {
                    return;
                }
                if (multi) {
                    StringBuffer stringBuffer = new StringBuffer();
                    File[] arrfile = jFileChooser.getSelectedFiles();
                    for (int i = 0; i < arrfile.length; i++) {
                        if (arrfile[i] != null && arrfile[i].exists()) {
                            stringBuffer.append(arrfile[i]);
                            if (i + 1 < arrfile.length) {
                                stringBuffer.append(",");
                            }
                        }
                    }
                    SafeDialogs.post(callback, stringBuffer.toString());
                } else {
                    if (jFileChooser.getSelectedFile() != null && jFileChooser.getSelectedFile().exists()) {
                        if (jFileChooser.getSelectedFile().isDirectory()) {
                            SafeDialogs.lastOpenDirectory = jFileChooser.getSelectedFile();
                        } else {
                            SafeDialogs.lastOpenDirectory = jFileChooser.getSelectedFile().getParentFile();
                        }
                    }
                    SafeDialogs.post(callback, jFileChooser.getSelectedFile() + "");
                }
            }
        });
    }
}
