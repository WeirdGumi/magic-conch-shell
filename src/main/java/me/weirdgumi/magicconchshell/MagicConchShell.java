package me.weirdgumi.magicconchshell;

import me.weirdgumi.magicconchshell.command.CommandRegistry;
import me.weirdgumi.magicconchshell.fs.SuperBlock;

public final class MagicConchShell {
    public static void main(String[] args) {
        SuperBlock sb = new SuperBlock();
        Shell shell = new Shell(sb);
        CommandRegistry.register();
        shell.run();
    }
}
