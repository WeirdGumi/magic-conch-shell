package me.weirdgumi.magicconchshell.command;

import me.weirdgumi.magicconchshell.fs.Dentry;
import me.weirdgumi.magicconchshell.fs.Namei;
import me.weirdgumi.magicconchshell.fs.Path;

import java.io.InputStream;
import java.io.PrintWriter;

public final class Mv implements Command {
    @Override
    public void execute(CommandContext context, String[] args, InputStream stdin, PrintWriter out) {
        CommandSupport.requireArgs(args, 2);
        Dentry source = CommandSupport.resolve(context, args[0]);
        if (source == null) throw new IllegalArgumentException(args[0] + ": No such file or directory");
        if (source.getParent() == null) throw new IllegalArgumentException("cannot move root directory");

        Dentry targetDentry = CommandSupport.resolve(context, args[1]);
        Dentry targetParent;
        String targetName;

        if (targetDentry != null && targetDentry.isDirectory()) {
            targetParent = targetDentry;
            targetName = source.getName();
        } else if (targetDentry != null) {
            if (source.isDirectory()) {
                throw new IllegalArgumentException(args[1] + ": Cannot overwrite non-directory with directory");
            }
            targetParent = targetDentry.getParent();
            targetName = targetDentry.getName();
        } else {
            targetParent = CommandSupport.parentOf(context, args[1]);
            targetName = Namei.name(Path.of(args[1]));
            if (CommandSupport.isReserved(targetName)) throw new IllegalArgumentException(args[1] + ": Invalid path");
        }

        for (Dentry d = targetParent; d != null; d = d.getParent()) {
            if (d == source) throw new IllegalArgumentException("cannot move a directory into itself");
        }

        Dentry existing = targetParent.getChild(targetName);
        if (existing == source) {
            throw new IllegalArgumentException("source and destination are the same file");
        }
        if (existing != null) {
            if (existing.isDirectory() || source.isDirectory()) {
                throw new IllegalStateException(args[1] + ": File exists");
            }
            targetParent.unlink(existing.getName());
        }

        Dentry parent = source.getParent();
        if (source.isDirectory()) {
            if (parent != null) parent.getInode().decrementNlink();
            targetParent.getInode().incrementNlink();
        }
        if (parent != null) parent.unlink(source.getName());
        targetParent.link(targetName, source);
    }
}
