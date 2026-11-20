package me.weirdgumi.magicconchshell.pipeline;

import java.util.ArrayList;
import java.util.List;

public final class Tokenizer {
    private Tokenizer() {
    }

    public static List<Token> tokenize(String line) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inToken = false;
        char quote = 0;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (quote != 0) {
                if (c == quote) {
                    quote = 0;
                } else if (c == '\\' && quote == '"' && i + 1 < line.length()) {
                    char next = line.charAt(++i);
                    if (next == '"' || next == '\\') {
                        current.append(next);
                    } else {
                        current.append('\\');
                        current.append(next);
                    }
                } else {
                    current.append(c);
                }
                continue;
            }

            if (Character.isWhitespace(c)) {
                inToken = Tokenizer.flushWord(tokens, current, inToken);
            } else if (c == '"' || c == '\'') {
                quote = c;
                inToken = true;
            } else if (c == '|') {
                inToken = Tokenizer.flushWord(tokens, current, inToken);
                tokens.add(new Token(TokenType.PIPE, "|"));
            } else if (c == '<') {
                inToken = Tokenizer.flushWord(tokens, current, inToken);
                tokens.add(new Token(TokenType.REDIRECT_IN, "<"));
            } else if (c == '>') {
                inToken = Tokenizer.flushWord(tokens, current, inToken);
                if (i + 1 < line.length() && line.charAt(i + 1) == '>') {
                    tokens.add(new Token(TokenType.REDIRECT_APPEND, ">>"));
                    i++;
                } else {
                    tokens.add(new Token(TokenType.REDIRECT_OUT, ">"));
                }
            } else if (c == '\\' && i + 1 < line.length()) {
                current.append(line.charAt(++i));
                inToken = true;
            } else {
                current.append(c);
                inToken = true;
            }
        }

        if (inToken) {
            tokens.add(new Token(TokenType.WORD, current.toString()));
        }
        return tokens;
    }

    private static boolean flushWord(List<Token> tokens, StringBuilder current, boolean inToken) {
        if (!inToken) return false;
        tokens.add(new Token(TokenType.WORD, current.toString()));
        current.setLength(0);
        return false;
    }
}
