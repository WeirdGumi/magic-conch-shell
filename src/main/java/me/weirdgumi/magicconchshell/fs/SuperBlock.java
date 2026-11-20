package me.weirdgumi.magicconchshell.fs;

public final class SuperBlock {
    private final Dentry root;
    private int nextIno = 1;

    public SuperBlock() {
        this.root = new Dentry(this.newInode(Stat.IFDIR), "/", null);
    }

    public Dentry getRoot() {
        return this.root;
    }

    public Dentry create(Dentry parent, String name, int mode) {
        Dentry dentry = new Dentry(this.newInode(mode), name, parent);
        parent.link(name, dentry);
        if (Stat.isDirectory(mode)) parent.getInode().incrementNlink();
        return dentry;
    }

    public Inode newInode(int mode) {
        Inode inode = new Inode(this.nextIno++, mode);
        return inode;
    }
}
