package dialog;

import java.util.LinkedList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LightSwitch implements ChangeListener {

    protected JCheckBox the_switch = null;

    protected LinkedList<JComponent> components = new LinkedList();

    protected boolean negate = false;

    public void stateChanged(ChangeEvent changeEvent) {
        check();
    }

    public void check() {
        boolean bool = this.the_switch.isSelected();
        if (this.negate)
            bool = !bool;
        for (JComponent jComponent : this.components) {
            jComponent.setEnabled(bool);
        }
    }

    public void set(JCheckBox paramJCheckBox, boolean bl) {
        this.the_switch = paramJCheckBox;
        this.negate = bl;
        this.the_switch.addChangeListener(this);
        check();
    }

    public void set(DialogManager.DialogRow dialogRow, boolean bl) {
        set((JCheckBox) dialogRow.get(1), bl);
    }

    public void add(DialogManager.DialogRow dialogRow) {
        add(dialogRow.get(0));
        add(dialogRow.get(1));
        add(dialogRow.get(2));
    }

    public void add(List<DialogManager.DialogRow> list) {
        for (DialogManager.DialogRow dialogRow : list) {
            add(dialogRow);
        }
    }

    public void add(JComponent jComponent) {
        if (jComponent != null) {
            this.components.add(jComponent);
        }
    }
}
