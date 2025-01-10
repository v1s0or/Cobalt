package console;

import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

public class SearchPanel extends JPanel implements ActionListener {

    protected JTextField search = null;

    protected JLabel status = null;

    protected JTextComponent component = null;

    protected int index = 0;

    protected Color highlight = null;

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals(">")) {
            this.index++;
            scrollToIndex();
        } else if (actionEvent.getActionCommand().equals("<")) {
            this.index--;
            scrollToIndex();
        } else {
            searchBuffer();
            scrollToIndex();
        }
    }

    private void scrollToIndex() {
        Highlighter.Highlight[] arrhighlight = this.component.getHighlighter().getHighlights();
        if (arrhighlight.length == 0) {
            if (this.search.getText().trim().length() > 0) {
                this.status.setText("Phrase not found");
            }
            return;
        }
        try {
            if (this.index < 0) {
                this.index = arrhighlight.length - 1 - this.index;
            }
            int i = this.index % arrhighlight.length;
            this.status.setText((i + 1) + " of " + arrhighlight.length);
            int j = arrhighlight[i].getStartOffset();
            Rectangle rectangle = this.component.modelToView(j);
            this.component.scrollRectToVisible(rectangle);
        } catch (BadLocationException badLocationException) {
        }
    }

    private void searchBuffer() {
        clear();
        String str = this.search.getText().toLowerCase().trim();
        if (str.length() == 0) {
            return;
        }
        DefaultHighlighter.DefaultHighlightPainter defaultHighlightPainter = new DefaultHighlighter.DefaultHighlightPainter(this.highlight);
        try {
            String str1 = this.component.getText().toLowerCase();
            if ((System.getProperty("os.name") + "").indexOf("Windows") != -1) {
                str1 = str1.replaceAll("\r\n", "\n");
            }
            int i = -1;
            while ((i = str1.indexOf(str, i + 1)) != -1) {
                this.component.getHighlighter()
                        .addHighlight(i, i + str.length(), defaultHighlightPainter);
            }
        } catch (Exception exception) {
        }
    }

    public void requestFocus() {
        this.search.requestFocus();
    }

    public void clear() {
        this.component.getHighlighter().removeAllHighlights();
        this.index = 0;
        this.status.setText("");
    }

    public SearchPanel(JTextComponent paramJTextComponent, Color paramColor) {
        this.component = paramJTextComponent;
        this.highlight = paramColor;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(1, 1, 1, 1));
        JButton jButton1 = new JButton("<");
        jButton1.setActionCommand("<");
        JButton jButton2 = new JButton(">");
        jButton2.setActionCommand(">");
        DialogUtils.removeBorderFromButton(jButton1);
        DialogUtils.removeBorderFromButton(jButton2);
        jButton1.addActionListener(this);
        jButton2.addActionListener(this);
        JPanel jPanel1 = new JPanel();
        jPanel1.setLayout(new GridLayout(1, 2));
        jPanel1.add(jButton1);
        jPanel1.add(jButton2);
        this.search = new JTextField(15);
        this.search.addActionListener(this);
        JPanel jPanel2 = new JPanel();
        jPanel2.setLayout(new FlowLayout());
        jPanel2.add(new JLabel("Find: "));
        jPanel2.add(this.search);
        jPanel2.add(jPanel1);
        add(jPanel2, "West");
        this.status = new JLabel("");
        add(this.status, "Center");
    }
}
