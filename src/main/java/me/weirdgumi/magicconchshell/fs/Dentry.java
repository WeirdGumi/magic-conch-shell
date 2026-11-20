package me.weirdgumi.magicconchshell.fs;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public final class Dentry {
    private final Inode inode;
    private final NavigableMap<String, Dentry> children = new TreeMap<>();
    private String name;
    private Dentry parent;

    public Dentry(Inode inode, String name, Dentry parent) {
        this.inode = inode;
        this.name = name;
        this.parent = parent;
    }

    public Inode getInode() {
        return this.inode;
    }

    public List<Dentry> getChildren() {
        return new ArrayList<>(this.children.values());
    }

    public Dentry getChild(String childName) {
        return this.children.get(childName);
    }

    public String getName() {
        return this.name;
    }

    public Dentry getParent() {
        return this.parent;
    }

    public void link(String childName, Dentry child) {
        this.children.put(childName, child);
        child.name = childName;
        child.parent = this;
        this.inode.touch();
    }

    public void unlink(String childName) {
        this.children.remove(childName);
        this.inode.touch();
    }

    public String getAbsolutePath() {
        if (this.parent == null) return "/";
        String path = this.parent.getAbsolutePath();
        return "/".equals(path) ? "/" + this.name : path + "/" + this.name;
    }

    public boolean isDirectory() {
        return this.inode.isDirectory();
    }
}
