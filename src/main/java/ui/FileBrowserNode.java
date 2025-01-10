package ui;

import javax.swing.tree.DefaultMutableTreeNode;

public class FileBrowserNode implements Comparable {

    protected String display;

    protected String path;

    protected String[] walk;

    protected String cache = null;

    protected boolean isUNC = false;

    public FileBrowserNode(String string) {
        if (string.startsWith("\\\\")) {
            string = string.substring(2);
            this.isUNC = true;
        }
        this.walk = string.split("\\\\");
        this.path = getPath();
        if (this.walk.length == 0) {
            this.isUNC = true;
            this.display = "[error]";
            this.path = "[error]";
        } else {
            this.display = this.walk[this.walk.length - 1];
        }
    }

    public int compareTo(Object object) {
        FileBrowserNode fileBrowserNode = (FileBrowserNode) object;
        return this.path.toLowerCase().compareTo(fileBrowserNode.path.toLowerCase());
    }

    public boolean isComputer() {
        return (isDrive() && this.isUNC);
    }

    public boolean isDrive() {
        return (this.walk.length == 1);
    }

    public DefaultMutableTreeNode getTreeNode() {
        return new DefaultMutableTreeNode(this);
    }

    public boolean hasCache() {
        return (this.cache != null);
    }

    public String getCache() {
        return this.cache;
    }

    public void setCache(String string) {
        this.cache = string;
    }

    public String getParent() {
        if (isComputer())
            return "";
        StringBuffer stringBuffer = new StringBuffer();
        if (this.isUNC)
            stringBuffer.append("\\\\");
        for (byte b = 0; b < this.walk.length - 1; b++) {
            stringBuffer.append(this.walk[b]);
            stringBuffer.append("\\");
        }
        return stringBuffer.toString();
    }

    public String getPathNoTrailingSlash() {
        String str = getPath();
        return str.substring(0, str.length() - 1);
    }

    public String getPath() {
        StringBuffer stringBuffer = new StringBuffer();
        if (this.isUNC)
            stringBuffer.append("\\\\");
        for (byte b = 0; b < this.walk.length; b++) {
            stringBuffer.append(this.walk[b]);
            stringBuffer.append("\\");
        }
        return stringBuffer.toString();
    }

    public String getChild(String string) {
        return getPath() + string;
    }

    public String getName() {
        return this.display;
    }

    public String toString() {
        return getName();
    }
}
