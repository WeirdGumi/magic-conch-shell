package me.weirdgumi.magicconchshell;

import me.weirdgumi.magicconchshell.command.Command;
import me.weirdgumi.magicconchshell.command.CommandContext;
import me.weirdgumi.magicconchshell.command.CommandHandler;
import me.weirdgumi.magicconchshell.command.CommandRegistry;
import me.weirdgumi.magicconchshell.command.CommandSupport;
import me.weirdgumi.magicconchshell.fs.Dentry;
import me.weirdgumi.magicconchshell.fs.Namei;
import me.weirdgumi.magicconchshell.fs.Path;
import me.weirdgumi.magicconchshell.fs.SuperBlock;
import me.weirdgumi.magicconchshell.pipeline.Pipeline;
import me.weirdgumi.magicconchshell.pipeline.PipelineExecutor;
import me.weirdgumi.magicconchshell.pipeline.PipelineParser;
import me.weirdgumi.magicconchshell.pipeline.Tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

public class Shell implements Runnable, CommandHandler {
    private final SuperBlock sb;
    private final BufferedReader in;
    private final PrintWriter out, err;
    private Dentry cwd;
    private boolean running;

    public Shell(SuperBlock sb) {
        this(sb,
            new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
            CommandSupport.writer(System.out),
            CommandSupport.writer(System.err));
    }

    public Shell(SuperBlock sb, BufferedReader in, PrintWriter out, PrintWriter err) {
        this.sb = sb;
        this.cwd = this.sb.getRoot();
        this.in = in;
        this.out = out;
        this.err = err;
    }

    @Override
    public void run() {
        this.running = true;
        while (this.running) {
            this.out.print("java-linux:" + this.cwd.getAbsolutePath() + "> ");
            this.out.flush();

            String line;
            try {
                line = this.in.readLine();
            } catch (IOException e) {
                this.err.println(CommandSupport.errorText(e));
                this.err.flush();
                return;
            }
            if (line == null) break;
            if (line.trim().isEmpty()) continue;

            try {
                Pipeline pipeline = PipelineParser.parse(Tokenizer.tokenize(line));
                boolean subshell = pipeline.stages().size() > 1;
                Dentry savedCwd = this.cwd;
                boolean savedRunning = this.running;
                try {
                    CommandContext context = new CommandContext(this.sb, this.cwd);
                    new PipelineExecutor(context, this.out, this.err, this).execute(pipeline);
                } finally {
                    if (subshell) {
                        this.cwd = savedCwd;
                        this.running = savedRunning;
                    }
                }
            } catch (RuntimeException e) {
                this.err.println(CommandSupport.errorText(e));
                this.err.flush();
            }
        }
    }

    @Override
    public void execute(CommandContext context, String name, String[] args, InputStream stdin, PrintWriter stdout) {
        switch (name) {
            case "pwd":
                this.pwd(args, stdout, context);
                return;
            case "cd":
                this.cd(args);
                return;
            case "echo":
                this.echo(args, stdout);
                return;
            case "exit":
                this.exit(args);
                return;
            case "help":
                this.help(args, stdout);
                return;
            default:
                break;
        }
        Command command = CommandRegistry.get(name);
        if (command == null) throw new NoSuchElementException("command not found");
        command.execute(context, args, stdin, stdout);
    }

    private void pwd(String[] args, PrintWriter stdout, CommandContext context) {
        CommandSupport.requireArgs(args, 0);
        stdout.println(context.cwd().getAbsolutePath());
    }

    private void cd(String[] args) {
        CommandSupport.requireArgs(args, 0, 1);
        String target = args.length == 0 ? "/" : args[0];
        Dentry dentry = Namei.lookup(this.sb, this.cwd, Path.of(target));
        if (dentry == null) throw new IllegalArgumentException(target + ": No such file or directory");
        if (!dentry.isDirectory()) throw new IllegalArgumentException(target + ": Not a directory");
        this.cwd = dentry;
    }

    private void echo(String[] args, PrintWriter stdout) {
        stdout.println(String.join(" ", args));
    }

    private void exit(String[] args) {
        CommandSupport.requireArgs(args, 0);
        this.running = false;
    }

    private void help(String[] args, PrintWriter stdout) {
        CommandSupport.requireArgs(args, 0);
        stdout.println("pwd               show the current directory");
        stdout.println("cd [path]         change directory (no argument: /)");
        stdout.println("mkdir [-p] <dir>... create directories (-p: create parents)");
        stdout.println("ls [-l] [path]... list directory contents");
        stdout.println("touch <file>...     create empty files / update timestamps");
        stdout.println("cat [file]...     print files (no file: standard input)");
        stdout.println("echo [text...]    print text");
        stdout.println("mv <src> <dst>        move or rename");
        stdout.println("rm [-r] <path>...   remove files/directories (-r: recursive)");
        stdout.println("date              show current date and time");
        stdout.println("help              show this help");
        stdout.println("exit              quit the shell");
    }
}
