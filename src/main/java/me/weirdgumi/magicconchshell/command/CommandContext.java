package me.weirdgumi.magicconchshell.command;

import com.github.bsideup.jabel.Desugar;
import me.weirdgumi.magicconchshell.fs.Dentry;
import me.weirdgumi.magicconchshell.fs.SuperBlock;

@Desugar
public record CommandContext(
    SuperBlock sb,
    Dentry cwd
) {
}
