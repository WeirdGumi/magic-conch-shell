package me.weirdgumi.magicconchshell.command;

import me.weirdgumi.magicconchshell.fs.Dentry;
import me.weirdgumi.magicconchshell.fs.File;
import me.weirdgumi.magicconchshell.fs.Namei;
import me.weirdgumi.magicconchshell.fs.Path;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class CommandSupport {
    private CommandSupport() {
    }

    public static Dentry resolve(CommandContext context, String path) {
        return Namei.lookup(context.sb(), context.cwd(), Path.of(path));
    }

    public static Dentry parentOf(CommandContext context, String path) {
        Dentry parent = Namei.lookupParent(context.sb(), context.cwd(), Path.of(path));
        if (parent == null) throw new IllegalArgumentException(path + ": No such file or directory");
        if (!parent.isDirectory()) throw new IllegalArgumentException(path + ": Not a directory");
        return parent;
    }

    public static byte[] readAllBytes(Dentry dentry) {
        File file = new File(dentry);
        int size = file.getSize();
        byte[] buffer = new byte[size];
        file.seek(0);
        int read = file.read(buffer, 0, size);
        if (read == size) return buffer;
        return Arrays.copyOf(buffer, read);
    }

    public static PrintWriter writer(OutputStream stream) {
        return new PrintWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8), true);
    }

    public static void requireArgs(String[] args, int expected) {
        if (args.length != expected) throw new IllegalArgumentException("invalid number of arguments");
    }

    public static void requireArgs(String[] args, int min, int max) {
        if (args.length < min || args.length > max) throw new IllegalArgumentException("invalid number of arguments");
    }

    public static boolean isReserved(String name) {
        return name.isEmpty() || name.equals(".") || name.equals("..");
    }

    public static String errorText(Throwable e) {
        String message = e.getMessage();
        if (message == null || message.isEmpty()) return e.getClass().getSimpleName();
        return message;
    }
}
