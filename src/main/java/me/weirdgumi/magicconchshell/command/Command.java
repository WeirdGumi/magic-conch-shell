package me.weirdgumi.magicconchshell.command;

import java.io.InputStream;
import java.io.PrintWriter;

public interface Command {
    void execute(CommandContext context, String[] args, InputStream stdin, PrintWriter out);
}
