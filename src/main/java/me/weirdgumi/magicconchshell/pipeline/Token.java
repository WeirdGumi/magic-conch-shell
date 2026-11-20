package me.weirdgumi.magicconchshell.pipeline;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record Token(TokenType type, String text) {
}
