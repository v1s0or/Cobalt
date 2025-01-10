package ui;

import common.CommonUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class FileBrowserNodes {

    protected Map cache = new HashMap();

    protected List<FileBrowserNode> all = new LinkedList();

    protected FileBrowserNode selected = null;

    protected DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());

    public boolean isSelected(FileBrowserNode paramFileBrowserNode) {
        return (paramFileBrowserNode == this.selected);
    }

    public FileBrowserNode getSelected() {
        return this.selected;
    }

    public void setSelected(FileBrowserNode paramFileBrowserNode) {
        this.selected = paramFileBrowserNode;
    }

    public TreeModel getModel() {
        return this.model;
    }

    public void refresh(final JTree tree) {
        DefaultMutableTreeNode defaultMutableTreeNode1 = null;
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        Map<String, DefaultMutableTreeNode> hashMap = new HashMap();
        // hashMap.put("", defaultMutableTreeNode2);
        hashMap.put("", root);
        Collections.sort(this.all);
        for (FileBrowserNode fileBrowserNode : this.all) {
            DefaultMutableTreeNode defaultMutableTreeNode4 = fileBrowserNode.getTreeNode();
            DefaultMutableTreeNode defaultMutableTreeNode5 = hashMap.get(fileBrowserNode.getParent().toLowerCase());
            if (defaultMutableTreeNode5 != null)
                defaultMutableTreeNode5.add(defaultMutableTreeNode4);
            if (isSelected(fileBrowserNode))
                defaultMutableTreeNode1 = defaultMutableTreeNode4;
            hashMap.put(fileBrowserNode.getPath().toLowerCase(), defaultMutableTreeNode4);
        }
        final DefaultMutableTreeNode expandTo2 = (defaultMutableTreeNode1 != null && defaultMutableTreeNode1.isLeaf()) ? (DefaultMutableTreeNode) defaultMutableTreeNode1.getParent() : defaultMutableTreeNode1;
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                FileBrowserNodes.this.model.setRoot(root);
                if (expandTo2 != null)
                    tree.expandPath(new TreePath(expandTo2.getPath()));
            }
        });
    }

    public FileBrowserNode getNodeFromCache(String string) {
        string = string.toLowerCase();
        return this.cache.containsKey(string) ? (FileBrowserNode) this.cache.get(string) : (this.cache.containsKey(string + "\\") ? (FileBrowserNode) this.cache.get(string + "\\") : null);
    }

    public FileBrowserNode getNode(String string) {
        String str = string.toLowerCase();
        if (this.cache.containsKey(str))
            return (FileBrowserNode) this.cache.get(str);
        if (this.cache.containsKey(str + "\\"))
            return (FileBrowserNode) this.cache.get(str + "\\");
        FileBrowserNode fileBrowserNode = new FileBrowserNode(string);
        this.cache.put(fileBrowserNode.getPath().toLowerCase(), fileBrowserNode);
        this.all.add(fileBrowserNode);
        if (!"".equals(fileBrowserNode.getParent()))
            getNode(fileBrowserNode.getParent());
        return fileBrowserNode;
    }

    public FileBrowserNode addNode(String string1, String string2) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(string1);
        if (!string1.endsWith("\\"))
            stringBuffer.append("\\");
        stringBuffer.append(string2);
        return getNode(stringBuffer.toString());
    }
}
