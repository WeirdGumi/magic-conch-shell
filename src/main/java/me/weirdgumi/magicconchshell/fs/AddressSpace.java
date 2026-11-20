package me.weirdgumi.magicconchshell.fs;

import java.util.Arrays;

public final class AddressSpace {
    private final Inode host;
    private byte[] data = new byte[0];

    public AddressSpace(Inode inode) {
        this.host = inode;
    }

    public int read(int pos, byte[] buffer, int offset, int length) {
        if (pos >= this.data.length) return 0;
        int next = Math.min(length, this.data.length - pos);
        System.arraycopy(this.data, pos, buffer, offset, next);
        return next;
    }

    public int write(int pos, byte[] buffer, int offset, int length) {
        if (pos + length > this.data.length) {
            this.data = Arrays.copyOf(this.data, pos + length);
        }
        System.arraycopy(buffer, offset, this.data, pos, length);
        this.host.setSize(Math.max(this.host.getSize(), pos + length));
        this.host.touch();
        return length;
    }

    public void truncate(int newSize) {
        this.data = Arrays.copyOf(this.data, newSize);
        this.host.setSize(newSize);
        this.host.touch();
    }
}
