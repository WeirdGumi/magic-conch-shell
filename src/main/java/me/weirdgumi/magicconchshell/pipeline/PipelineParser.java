package me.weirdgumi.magicconchshell.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PipelineParser {
    private PipelineParser() {
    }

    public static Pipeline parse(List<Token> tokens) {
        List<SimpleCommand> stages = new ArrayList<>();
        List<String> argv = new ArrayList<>();
        String inputPath = null;
        String outputPath = null;
        boolean append = false;

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            switch (token.type()) {
                case WORD:
                    argv.add(token.text());
                    break;
                case PIPE:
                    stages.add(PipelineParser.newStage(argv, inputPath, outputPath, append));
                    argv = new ArrayList<>();
                    inputPath = null;
                    outputPath = null;
                    append = false;
                    break;
                case REDIRECT_IN:
                    inputPath = PipelineParser.nextWord(tokens, i, "input");
                    i++;
                    break;
                case REDIRECT_OUT:
                    outputPath = PipelineParser.nextWord(tokens, i, "output");
                    append = false;
                    i++;
                    break;
                case REDIRECT_APPEND:
                    outputPath = PipelineParser.nextWord(tokens, i, "output");
                    append = true;
                    i++;
                    break;
                default:
                    throw new IllegalArgumentException("syntax error: unknown token");
            }
        }

        stages.add(PipelineParser.newStage(argv, inputPath, outputPath, append));
        return new Pipeline(Collections.unmodifiableList(stages));
    }

    private static String nextWord(List<Token> tokens, int index, String kind) {
        if (index + 1 >= tokens.size() || tokens.get(index + 1).type() != TokenType.WORD) {
            throw new IllegalArgumentException("syntax error: missing " + kind + " file");
        }
        return tokens.get(index + 1).text();
    }

    private static SimpleCommand newStage(List<String> argv, String inputPath, String outputPath, boolean append) {
        if (argv.isEmpty()) throw new IllegalArgumentException("syntax error: missing command");
        return new SimpleCommand(
            argv.get(0),
            Collections.unmodifiableList(new ArrayList<>(argv.subList(1, argv.size()))),
            inputPath,
            outputPath,
            append
        );
    }
}
