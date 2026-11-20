package me.weirdgumi.magicconchshell.command;

import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Date implements Command {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void execute(CommandContext context, String[] args, InputStream stdin, PrintWriter out) {
        CommandSupport.requireArgs(args, 0);
        out.println(LocalDateTime.now().format(Date.FORMAT));
    }
}
