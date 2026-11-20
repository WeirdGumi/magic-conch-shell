package me.weirdgumi.magicconchshell.fs;

public final class File {
    private final Inode inode;
    private int pos;

    public File(Dentry dentry) {
        this.inode = dentry.getInode();
    }

    public int read(byte[] buffer, int offset, int length) {
        int next = this.inode.getMapping().read(this.pos, buffer, offset, length);
        this.pos += next;
        this.inode.touchAccess();
        return next;
    }

    public int write(byte[] buffer, int offset, int length) {
        int next = this.inode.getMapping().write(this.pos, buffer, offset, length);
        this.pos += next;
        return next;
    }

    public void seek(int pos) {
        this.pos = pos;
    }

    public void truncate() {
        this.inode.getMapping().truncate(0);
        this.pos = 0;
    }

    public int getSize() {
        return this.inode.getSize();
    }
}
