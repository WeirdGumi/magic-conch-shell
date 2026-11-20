package me.weirdgumi.magicconchshell.fs;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class Path {
    private final byte[] path;

    private Path(byte[] path) {
        this.path = path;
    }

    public static Path of(String first, String... more) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int last = Path.writeMerged(out, first.getBytes(StandardCharsets.UTF_8), -1);
        for (String part : more) {
            if (part.isEmpty()) continue;
            if (last != -1 && last != '/') {
                out.write('/');
                last = '/';
            }
            last = Path.writeMerged(out, part.getBytes(StandardCharsets.UTF_8), last);
        }
        byte[] bytes = out.toByteArray();
        if (bytes.length > 1 && bytes[bytes.length - 1] == '/') {
            bytes = Arrays.copyOf(bytes, bytes.length - 1);
        }
        return new Path(bytes);
    }

    private static int writeMerged(ByteArrayOutputStream out, byte[] bytes, int last) {
        for (byte b : bytes) {
            if (b == '/') {
                if (last != '/') {
                    out.write('/');
                    last = '/';
                }
            } else {
                out.write(b);
                last = b;
            }
        }
        return last;
    }

    public Path normalize() {
        boolean absolute = this.isAbsolute();
        int len = this.path.length;
        int[] starts = new int[len];
        int[] ends = new int[len];
        int top = -1;

        int i = absolute ? 1 : 0;
        while (i < len) {
            while (i < len && this.path[i] == '/') i++;
            if (i >= len) break;

            int start = i;
            while (i < len && this.path[i] != '/') i++;
            int end = i;

            int segLen = end - start;
            if (segLen == 1 && this.path[start] == '.') continue;
            if (segLen == 2 && this.path[start] == '.' && this.path[start + 1] == '.') {
                if (top >= 0 && !this.isDotDot(starts[top], ends[top])) {
                    top--;
                } else if (!absolute) {
                    top++;
                    starts[top] = start;
                    ends[top] = end;
                }
            } else {
                top++;
                starts[top] = start;
                ends[top] = end;
            }
        }

        int count = top + 1;
        int total = absolute ? 1 : 0;
        for (int k = 0; k < count; k++) {
            total += ends[k] - starts[k];
            if (k > 0) total++;
        }
        byte[] result = new byte[total];
        int pos = 0;
        if (absolute) result[pos++] = '/';
        for (int k = 0; k < count; k++) {
            if (k > 0) result[pos++] = '/';
            int segLen = ends[k] - starts[k];
            System.arraycopy(this.path, starts[k], result, pos, segLen);
            pos += segLen;
        }
        return new Path(result);
    }

    private boolean isDotDot(int start, int end) {
        return end - start == 2 && this.path[start] == '.' && this.path[start + 1] == '.';
    }

    public boolean isAbsolute() {
        return this.path.length > 0 && this.path[0] == '/';
    }

    @Override
    public String toString() {
        return new String(this.path, StandardCharsets.UTF_8);
    }
}
