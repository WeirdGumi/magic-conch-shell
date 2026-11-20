package me.weirdgumi.magicconchshell.command;

import me.weirdgumi.magicconchshell.fs.Dentry;

import java.io.InputStream;
import java.io.PrintWriter;

public final class Rm implements Command {
    private static boolean isAncestor(Dentry ancestor, Dentry dentry) {
        for (Dentry d = dentry; d != null; d = d.getParent()) {
            if (d == ancestor) return true;
        }
        return false;
    }

    private static void remove(CommandContext context, Dentry dentry) {
        if (dentry.isDirectory()) {
            for (Dentry child : dentry.getChildren()) {
                Rm.remove(context, child);
            }
        }
        Dentry parent = dentry.getParent();
        if (parent != null) {
            parent.unlink(dentry.getName());
            if (dentry.isDirectory()) parent.getInode().decrementNlink();
        }
    }

    @Override
    public void execute(CommandContext context, String[] args, InputStream stdin, PrintWriter out) {
        boolean recursive = args.length > 0 && args[0].equals("-r");
        int start = recursive ? 1 : 0;
        if (start >= args.length) throw new IllegalArgumentException("missing operand");

        for (int i = start; i < args.length; i++) {
            Dentry dentry = CommandSupport.resolve(context, args[i]);
            if (dentry == null) throw new IllegalArgumentException(args[i] + ": No such file or directory");
            if (dentry.getParent() == null) throw new IllegalArgumentException("cannot remove root directory");
            if (Rm.isAncestor(dentry, context.cwd())) {
                throw new IllegalArgumentException(args[i] + ": Cannot remove current directory or its ancestor");
            }
            if (dentry.isDirectory() && !dentry.getChildren().isEmpty() && !recursive) {
                throw new IllegalStateException(args[i] + ": Directory not empty");
            }
            Rm.remove(context, dentry);
        }
    }
}
