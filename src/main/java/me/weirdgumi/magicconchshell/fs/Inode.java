package me.weirdgumi.magicconchshell.fs;

import java.time.Instant;

public final class Inode {
    private final int ino, mode;
    private final AddressSpace mapping;
    private Instant atime, mtime, ctime;
    private int size, nlink;

    public Inode(int ino, int mode) {
        this.ino = ino;
        this.mode = mode;
        this.mapping = new AddressSpace(this);
        this.atime = this.mtime = this.ctime = Instant.now();
        this.nlink = this.isDirectory() ? 2 : 1;
    }

    public int getIno() {
        return this.ino;
    }

    public AddressSpace getMapping() {
        return this.mapping;
    }

    public int getSize() {
        return this.size;
    }

    void setSize(int size) {
        this.size = size;
    }

    public boolean isRegularFile() {
        return Stat.isRegularFile(this.mode);
    }

    public boolean isDirectory() {
        return Stat.isDirectory(this.mode);
    }

    public Stat stat() {
        return new Stat(this.ino, this.mode, this.nlink, this.size, this.atime, this.mtime, this.ctime);
    }

    public void incrementNlink() {
        this.nlink++;
    }

    public void decrementNlink() {
        if (this.nlink > 1) this.nlink--;
    }

    public void touchAccess() {
        this.atime = Instant.now();
    }

    public void touch() {
        this.mtime = this.ctime = Instant.now();
    }
}
