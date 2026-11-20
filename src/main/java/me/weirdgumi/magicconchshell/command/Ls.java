package me.weirdgumi.magicconchshell.command;

import me.weirdgumi.magicconchshell.fs.Dentry;
import me.weirdgumi.magicconchshell.fs.Stat;

import java.io.InputStream;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class Ls implements Command {
    private static final int WIDTH = 80;
    private static final DateTimeFormatter TIME =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private static void list(Dentry dir, boolean longFormat, PrintWriter out) {
        if (longFormat) {
            for (Dentry child : dir.getChildren()) {
                Ls.printEntry(child, true, out);
            }
            return;
        }
        List<String> names = new ArrayList<>();
        for (Dentry child : dir.getChildren()) {
            names.add(child.getName());
        }
        Ls.printColumns(names, out);
    }

    private static void printColumns(List<String> names, PrintWriter out) {
        if (names.isEmpty()) return;
        int max = 0;
        for (String name : names) {
            max = Math.max(max, name.length());
        }
        int columns = Math.max(1, (Ls.WIDTH + 2) / (max + 2));
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            out.print(name);
            if ((i + 1) % columns == 0 || i == names.size() - 1) {
                out.println();
            } else {
                for (int pad = name.length(); pad < max + 2; pad++) {
                    out.print(' ');
                }
            }
        }
    }

    private static void printEntry(Dentry dentry, boolean longFormat, PrintWriter out) {
        if (longFormat) {
            Stat stat = dentry.getInode().stat();
            out.println((stat.isDirectory() ? "d" : "-") + " " + stat.nlink() + " " + stat.size()
                + " " + Ls.TIME.format(stat.mtime()) + " " + dentry.getName());
        } else {
            out.println(dentry.getName());
        }
    }

    @Override
    public void execute(CommandContext context, String[] args, InputStream stdin, PrintWriter out) {
        boolean longFormat = args.length > 0 && args[0].equals("-l");
        int start = longFormat ? 1 : 0;
        if (start >= args.length) {
            Ls.list(context.cwd(), longFormat, out);
            return;
        }
        for (int i = start; i < args.length; i++) {
            Dentry dentry = CommandSupport.resolve(context, args[i]);
            if (dentry == null) throw new IllegalArgumentException(args[i] + ": No such file or directory");
            if (dentry.isDirectory()) {
                Ls.list(dentry, longFormat, out);
            } else {
                Ls.printEntry(dentry, longFormat, out);
            }
        }
    }
}
