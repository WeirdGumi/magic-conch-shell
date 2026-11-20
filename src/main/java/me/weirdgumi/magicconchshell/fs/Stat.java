package me.weirdgumi.magicconchshell.fs;

import com.github.bsideup.jabel.Desugar;

import java.time.Instant;

@Desugar
public record Stat(
    int ino,
    int mode,
    int nlink,
    int size,
    Instant atime,
    Instant mtime,
    Instant ctime
) {
    public static final int IFMT = 0xf000;
    public static final int IFREG = 0x8000;
    public static final int IFDIR = 0x4000;

    public static boolean isRegularFile(int mode) {
        return (mode & IFMT) == IFREG;
    }

    public static boolean isDirectory(int mode) {
        return (mode & IFMT) == IFDIR;
    }

    public boolean isRegularFile() {
        return Stat.isRegularFile(this.mode);
    }

    public boolean isDirectory() {
        return Stat.isDirectory(this.mode);
    }
}
