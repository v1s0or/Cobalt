package console;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Properties;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Colors {

    public static final char bold = '\002';//'\u0002'

    public static final char underline = '\037';//'\u001f'

    public static final char color = '\003';//'\u0003'

    public static final char cancel = '\017';//'\u000f'

    public static final char reverse = '\026';//'\u0016'

    protected boolean showcolors = true;

    protected Color[] colorTable = new Color[16];

    private StyledDocument dummy = new DefaultStyledDocument();

    private static final int MAX_DOCUMENT_LENGTH = 262144;

    public static String color(String string1, String string2) {
        return color + string2 + string1;
    }

    public static String underline(String string) {
        return underline + string + cancel;
    }

    public Colors(Properties properties) {
        this.colorTable[0] = Color.white;
        this.colorTable[1] = new Color(0, 0, 0);
        this.colorTable[2] = Color.decode("#3465A4");
        this.colorTable[3] = Color.decode("#4E9A06");
        this.colorTable[4] = Color.decode("#EF2929");
        this.colorTable[5] = Color.decode("#CC0000");
        this.colorTable[6] = Color.decode("#75507B");
        this.colorTable[7] = Color.decode("#C4A000");
        this.colorTable[8] = Color.decode("#FCE94F");
        this.colorTable[9] = Color.decode("#8AE234");
        this.colorTable[10] = Color.decode("#06989A");
        this.colorTable[11] = Color.decode("#34E2E2");
        this.colorTable[12] = Color.decode("#729FCF");
        this.colorTable[13] = Color.decode("#AD7FA8");
        this.colorTable[14] = Color.decode("#808080");
        this.colorTable[15] = Color.lightGray;
        for (int i = 0; i < 16; i++) {
            String str = properties.getProperty("console.color_" + i + ".color", null);
            if (str != null) {
                this.colorTable[i] = Color.decode(str);
            }
        }
        this.showcolors = "true".equals(properties.getProperty("console.show_colors.boolean", "true"));
    }

    public String strip(String string) {
        Fragment fragment = parse(string);
        return strip(fragment);
    }

    private String strip(Fragment fragment) {
        StringBuffer stringBuffer = new StringBuffer(128);
        while (fragment != null) {
            stringBuffer.append(fragment.text);
            fragment = fragment.next;
        }
        return stringBuffer.toString();
    }

    private void append(StyledDocument styledDocument, Fragment fragment) {
        while (fragment != null) {
            try {
                if (fragment.text.length() > 0) {
                    styledDocument.insertString(styledDocument.getLength(),
                            fragment.text.toString(), fragment.attr);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            fragment = fragment.next;
        }
    }

    public void append(StyledDocument styledDocument, String string) {
        if (string.length() > 262144) {
            string = string.substring(string.length() - 262144, string.length());
        }
        Fragment fragment = parse(string);
        append(styledDocument, fragment);
        if (styledDocument.getLength() > 262144) {
            try {
                styledDocument.remove(0, styledDocument.getLength() - 262144 + 131072);
            } catch (BadLocationException badLocationException) {
            }
        }
    }

    public void append(JTextPane jTextPane, String string) {
        StyledDocument styledDocument = jTextPane.getStyledDocument();
        if (this.showcolors) {
            jTextPane.setDocument(this.dummy);
            append(styledDocument, string);
            jTextPane.setDocument(styledDocument);
        } else {
            Fragment fragment = parse(string);
            append(styledDocument, parse(strip(fragment)));
        }
    }

    public void set(JTextPane jTextPane, String string) {
        Fragment fragment = parse(string);
        if (strip(fragment).equals(jTextPane.getText())) {
            return;
        }
        DefaultStyledDocument defaultStyledDocument = new DefaultStyledDocument();
        if (this.showcolors) {
            append(defaultStyledDocument, fragment);
        } else {
            append(defaultStyledDocument, parse(strip(fragment)));
        }
        jTextPane.setDocument(defaultStyledDocument);
        jTextPane.setSize(new Dimension(1000, jTextPane.getSize().height));
    }

    public void setNoHack(JTextPane jTextPane, String string) {
        Fragment fragment = parse(string);
        if (strip(fragment).equals(jTextPane.getText())) {
            return;
        }
        DefaultStyledDocument defaultStyledDocument = new DefaultStyledDocument();
        if (this.showcolors) {
            append(defaultStyledDocument, fragment);
        } else {
            append(defaultStyledDocument, parse(strip(fragment)));
        }
        jTextPane.setDocument(defaultStyledDocument);
    }

    private Fragment parse(String string) {
        Fragment fragment1 = new Fragment();
        Fragment fragment2 = fragment1;
        if (string == null) {
            return fragment1;
        }
        char[] arrc = string.toCharArray();
        for (int i = 0; i < arrc.length; i++) {
            switch (arrc[i]) {
                case bold:
                    fragment1.advance();
                    StyleConstants.setBold(fragment1.next.attr,
                            !StyleConstants.isBold(fragment1.attr));
                    fragment1 = fragment1.next;
                    break;
                case underline:
                    fragment1.advance();
                    StyleConstants.setUnderline(fragment1.next.attr,
                            !StyleConstants.isUnderline(fragment1.attr));
                    fragment1 = fragment1.next;
                    break;
                case color:
                    fragment1.advance();
                    if (i + 1 < arrc.length && (arrc[i + 1] >= '0'
                            && arrc[i + 1] <= '9' || arrc[i + 1] >= 'A' && arrc[i + 1] <= 'F')) {
                        int n = Integer.parseInt(arrc[i + 1] + "", 16);
                        StyleConstants.setForeground(fragment1.next.attr, this.colorTable[n]);
                        i++;
                    }
                    fragment1 = fragment1.next;
                    break;
                case '\n':
                    fragment1.advance();
                    fragment1 = fragment1.next;
                    fragment1.attr = new SimpleAttributeSet();
                    fragment1.text.append(arrc[i]);
                    break;
                case cancel:
                    fragment1.advance();
                    fragment1 = fragment1.next;
                    fragment1.attr = new SimpleAttributeSet();
                    break;
                default:
                    fragment1.text.append(arrc[i]);
                    break;
            }
        }
        return fragment2;
    }

    private static final class Fragment {
        protected SimpleAttributeSet attr = new SimpleAttributeSet();

        protected StringBuffer text = new StringBuffer(32);

        protected Fragment next = null;

        private Fragment() {
        }

        public void advance() {
            this.next = new Fragment();
            this.next.attr = (SimpleAttributeSet) this.attr.clone();
        }
    }
}
