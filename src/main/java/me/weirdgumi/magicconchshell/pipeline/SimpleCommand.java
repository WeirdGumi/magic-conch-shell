package me.weirdgumi.magicconchshell.pipeline;

import com.github.bsideup.jabel.Desugar;

import java.util.List;

@Desugar
public record SimpleCommand(
    String name,
    List<String> args,
    String inputPath,
    String outputPath,
    boolean append
) {
}
