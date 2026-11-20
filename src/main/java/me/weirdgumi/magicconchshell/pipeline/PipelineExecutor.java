package me.weirdgumi.magicconchshell.pipeline;

import me.weirdgumi.magicconchshell.command.CommandContext;
import me.weirdgumi.magicconchshell.command.CommandHandler;
import me.weirdgumi.magicconchshell.command.CommandSupport;
import me.weirdgumi.magicconchshell.fs.Dentry;
import me.weirdgumi.magicconchshell.fs.File;
import me.weirdgumi.magicconchshell.fs.Namei;
import me.weirdgumi.magicconchshell.fs.Path;
import me.weirdgumi.magicconchshell.fs.Stat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

public final class PipelineExecutor {
    private final CommandContext context;
    private final PrintWriter console;
    private final PrintWriter err;
    private final CommandHandler handler;

    public PipelineExecutor(CommandContext context, PrintWriter console, PrintWriter err, CommandHandler handler) {
        this.context = context;
        this.console = console;
        this.err = err;
        this.handler = handler;
    }

    public void execute(Pipeline pipeline) {
        byte[] pipe = null;
        List<SimpleCommand> stages = pipeline.stages();
        for (int i = 0; i < stages.size(); i++) {
            SimpleCommand stage = stages.get(i);
            ByteArrayOutputStream buffer = null;
            try {
                InputStream stdin;
                if (stage.inputPath() != null) {
                    stdin = this.openInput(stage.inputPath());
                } else if (pipe != null) {
                    stdin = new ByteArrayInputStream(pipe);
                } else {
                    stdin = new ByteArrayInputStream(new byte[0]);
                }

                PrintWriter target;
                boolean closeTarget = false;
                if (stage.outputPath() != null) {
                    target = this.openOutput(stage.outputPath(), stage.append());
                    closeTarget = true;
                } else if (i < stages.size() - 1) {
                    buffer = new ByteArrayOutputStream();
                    target = CommandSupport.writer(buffer);
                    closeTarget = true;
                } else {
                    target = this.console;
                }

                try {
                    this.handler.execute(this.context, stage.name(), stage.args().toArray(new String[0]), stdin, target);
                } finally {
                    target.flush();
                    if (closeTarget) target.close();
                }

                pipe = buffer == null ? null : buffer.toByteArray();
            } catch (RuntimeException e) {
                this.err.println(stage.name() + ": " + CommandSupport.errorText(e));
                this.err.flush();
                return;
            }
        }
    }

    private InputStream openInput(String path) {
        Dentry file = CommandSupport.resolve(this.context, path);
        if (file == null) throw new IllegalArgumentException(path + ": No such file or directory");
        if (!file.getInode().isRegularFile()) throw new IllegalArgumentException(path + ": Not a regular file");
        return new ByteArrayInputStream(CommandSupport.readAllBytes(file));
    }

    private PrintWriter openOutput(String path, boolean append) {
        Dentry parent = CommandSupport.parentOf(this.context, path);
        String name = Namei.name(Path.of(path));
        if (CommandSupport.isReserved(name)) throw new IllegalArgumentException(path + ": Invalid path");

        Dentry dentry = parent.getChild(name);
        if (dentry == null) {
            dentry = this.context.sb().create(parent, name, Stat.IFREG);
        } else if (!dentry.getInode().isRegularFile()) {
            throw new IllegalArgumentException(path + ": Not a regular file");
        }

        File file = new File(dentry);
        if (append) {
            file.seek(file.getSize());
        } else {
            file.truncate();
        }
        return CommandSupport.writer(new DentryOutputStream(file));
    }

    private static final class DentryOutputStream extends OutputStream {
        private final File file;

        DentryOutputStream(File file) {
            this.file = file;
        }

        @Override
        public void write(int b) {
            this.file.write(new byte[]{(byte) b}, 0, 1);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            this.file.write(b, off, len);
        }
    }
}
