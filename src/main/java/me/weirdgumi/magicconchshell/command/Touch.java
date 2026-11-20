package me.weirdgumi.magicconchshell.command;

import me.weirdgumi.magicconchshell.fs.Dentry;
import me.weirdgumi.magicconchshell.fs.Namei;
import me.weirdgumi.magicconchshell.fs.Path;
import me.weirdgumi.magicconchshell.fs.Stat;

import java.io.InputStream;
import java.io.PrintWriter;

public final class Touch implements Command {
    private static void touch(CommandContext context, String path) {
        Dentry parent = CommandSupport.parentOf(context, path);
        String name = Namei.name(Path.of(path));
        if (CommandSupport.isReserved(name)) throw new IllegalArgumentException(path + ": Invalid path");

        Dentry dentry = parent.getChild(name);
        if (dentry == null) {
            context.sb().create(parent, name, Stat.IFREG);
        } else {
            dentry.getInode().touch();
        }
    }

    @Override
    public void execute(CommandContext context, String[] args, InputStream stdin, PrintWriter out) {
        if (args.length == 0) throw new IllegalArgumentException("missing operand");
        for (String path : args) {
            Touch.touch(context, path);
        }
    }
}
