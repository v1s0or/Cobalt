package aggressor.windows;

import aggressor.AggressorClient;
import common.AObject;
import console.AssociatedPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import ui.FileBrowserNode;

public class FileBrowser extends AObject {
    protected String bid = "";

    protected AggressorClient client = null;

    protected Files browser = null;
    protected JTree tree = null;

    public FileBrowser(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        this.bid = string;
        this.browser = new Files(aggressorClient, string);
    }

    public JComponent getContent() {
        AssociatedPanel associatedPanel = new AssociatedPanel(this.bid);
        associatedPanel.setLayout(new BorderLayout());
        JSplitPane jSplitPane = new JSplitPane(1);
        jSplitPane.add(getTree());
        jSplitPane.add(this.browser.getContent());
        jSplitPane.setDividerLocation(320);
        associatedPanel.add(jSplitPane, "Center");
        associatedPanel.add(this.browser.getButtons(), "South");
        this.browser.ls(".");
        return associatedPanel;
    }

    public TreeCellRenderer getNewRenderer() {
        DefaultTreeCellRenderer defaultTreeCellRenderer = new DefaultTreeCellRenderer() {
            public Component getTreeCellRendererComponent(JTree jTree, Object object, boolean bl, boolean bl2, boolean bl3, int n, boolean bl4) {
                super.getTreeCellRendererComponent(jTree, null, false, bl2, bl3, n, false);
                DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) object;
                if (defaultMutableTreeNode == null)
                    return this;
                FileBrowserNode fileBrowserNode = (FileBrowserNode) defaultMutableTreeNode.getUserObject();
                if (fileBrowserNode == null)
                    return this;
                if (FileBrowser.this.browser.getNodes().isSelected(fileBrowserNode))
                    super.getTreeCellRendererComponent(jTree, null, true, bl2, bl3, n, false);
                setText(fileBrowserNode.getName());
                if (fileBrowserNode.isComputer()) {
                    setIcon(UIManager.getIcon("FileView.computerIcon"));
                } else if (fileBrowserNode.isDrive()) {
                    setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
                } else {
                    setIcon(UIManager.getIcon("FileView.directoryIcon"));
                }
                setEnabled((fileBrowserNode.hasCache() || fileBrowserNode.isComputer()));
                return this;
            }
        };
        defaultTreeCellRenderer.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        return defaultTreeCellRenderer;
    }

    public void doMouseClicked(MouseEvent mouseEvent) {
        TreePath treePath = this.tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
        if (treePath == null)
            return;
        DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
        if (defaultMutableTreeNode == null)
            return;
        FileBrowserNode fileBrowserNode = (FileBrowserNode) defaultMutableTreeNode.getUserObject();
        if (fileBrowserNode == null)
            return;
        this.browser.gotof(fileBrowserNode);
    }

    public JComponent getTree() {
        this.tree = new JTree(this.browser.getTreeModel());
        this.tree.setRootVisible(false);
        this.tree.setCellRenderer(getNewRenderer());
        this.tree.setScrollsOnExpand(true);
        this.tree.setPreferredSize(null);
        this.tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                FileBrowser.this.doMouseClicked(mouseEvent);
            }
        });
        this.browser.setTree(this.tree);
        return new JScrollPane(this.tree);
    }
}
