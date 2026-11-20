package me.weirdgumi.magicconchshell.command;

import java.io.InputStream;
import java.io.PrintWriter;

public interface CommandHandler {
    void execute(CommandContext context, String name, String[] args, InputStream stdin, PrintWriter out);
}
