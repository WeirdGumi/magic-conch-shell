package me.weirdgumi.magicconchshell.command;

import me.weirdgumi.magicconchshell.fs.Dentry;
import me.weirdgumi.magicconchshell.fs.Namei;
import me.weirdgumi.magicconchshell.fs.Path;
import me.weirdgumi.magicconchshell.fs.Stat;

import java.io.InputStream;
import java.io.PrintWriter;

public final class Mkdir implements Command {
    private static void mkdir(CommandContext context, String path) {
        Dentry parent = CommandSupport.parentOf(context, path);
        String name = Namei.name(Path.of(path));
        if (CommandSupport.isReserved(name)) throw new IllegalArgumentException(path + ": Invalid path");
        if (parent.getChild(name) != null) throw new IllegalStateException(path + ": File exists");
        context.sb().create(parent, name, Stat.IFDIR);
    }

    private static void mkdirs(CommandContext context, String path) {
        String normalized = Path.of(path).normalize().toString();
        if (normalized.isEmpty()) return;

        Dentry current = normalized.charAt(0) == '/' ? context.sb().getRoot() : context.cwd();
        for (String name : normalized.split("/")) {
            if (name.isEmpty()) continue;
            if (name.equals("..")) {
                if (current.getParent() != null) current = current.getParent();
                continue;
            }
            if (!current.isDirectory()) throw new IllegalArgumentException(path + ": Not a directory");
            Dentry next = current.getChild(name);
            if (next == null) {
                next = context.sb().create(current, name, Stat.IFDIR);
            } else if (!next.isDirectory()) {
                throw new IllegalStateException(path + ": File exists");
            }
            current = next;
        }
    }

    @Override
    public void execute(CommandContext context, String[] args, InputStream stdin, PrintWriter out) {
        boolean parents = args.length > 0 && args[0].equals("-p");
        int start = parents ? 1 : 0;
        if (start >= args.length) throw new IllegalArgumentException("missing operand");
        for (int i = start; i < args.length; i++) {
            if (parents) {
                Mkdir.mkdirs(context, args[i]);
            } else {
                Mkdir.mkdir(context, args[i]);
            }
        }
    }
}
