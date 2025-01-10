package common;

import java.util.LinkedList;
import java.util.List;

public class StringStack {

    protected String string;

    protected String delimeter;

    public StringStack(String string) {
        this(string, " ");
    }

    public StringStack(String string1, String string2) {
        this.string = string1;
        this.delimeter = string2;
    }

    public List toList() {
        LinkedList linkedList = new LinkedList();
        StringStack stringStack = new StringStack(this.string, this.delimeter);
        while (!stringStack.isEmpty())
            linkedList.add(stringStack.shift());
        return linkedList;
    }

    public void push(String string) {
        if (this.string.length() > 0) {
            this.string += this.delimeter + string;
        } else {
            this.string = string;
        }
    }

    public int length() {
        return this.string.length();
    }

    public boolean isEmpty() {
        return (this.string.length() == 0);
    }

    public String peekFirst() {
        return (this.string.indexOf(this.delimeter) > -1) ? this.string.substring(0, this.string.indexOf(this.delimeter)) : this.string;
    }

    public String shift() {
        if (this.string.indexOf(this.delimeter) > -1) {
            String str1 = this.string.substring(0, this.string.indexOf(this.delimeter));
            if (str1.length() >= this.string.length()) {
                this.string = "";
                return str1;
            }
            this.string = this.string.substring(str1.length() + 1, this.string.length());
            return str1;
        }
        String str = this.string;
        this.string = "";
        return str;
    }

    public String pop() {
        int i = this.string.lastIndexOf(this.delimeter);
        if (i > -1) {
            String str1 = this.string.substring(i + 1, this.string.length());
            this.string = this.string.substring(0, i);
            return str1;
        }
        String str = this.string;
        this.string = "";
        return str;
    }

    public String toString() {
        return this.string;
    }

    public void setDelimeter(String string) {
        this.delimeter = string;
    }
}
