package me.weirdgumi.magicconchshell.command;

import me.weirdgumi.magicconchshell.fs.Dentry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public final class Cat implements Command {
    private static void copy(InputStream in, PrintWriter out) {
        ByteArrayOutputStream collected = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        try {
            int read;
            while ((read = in.read(buffer)) != -1) {
                collected.write(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        out.write(new String(collected.toByteArray(), StandardCharsets.UTF_8));
    }

    @Override
    public void execute(CommandContext context, String[] args, InputStream stdin, PrintWriter out) {
        if (args.length == 0) {
            Cat.copy(stdin, out);
            return;
        }
        for (String path : args) {
            Dentry dentry = CommandSupport.resolve(context, path);
            if (dentry == null) throw new IllegalArgumentException(path + ": No such file or directory");
            if (!dentry.getInode().isRegularFile()) throw new IllegalArgumentException(path + ": Is a directory");
            out.write(new String(CommandSupport.readAllBytes(dentry), StandardCharsets.UTF_8));
        }
    }
}
