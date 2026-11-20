package me.weirdgumi.magicconchshell.pipeline;

import com.github.bsideup.jabel.Desugar;

import java.util.List;

@Desugar
public record Pipeline(List<SimpleCommand> stages) {
}
