package me.weirdgumi.magicconchshell.fs;

public final class Namei {
    private Namei() {
    }

    public static Dentry lookup(SuperBlock sb, Dentry cwd, Path path) {
        String normalized = path.normalize().toString();
        if (normalized.isEmpty()) return cwd;

        Dentry current = normalized.charAt(0) == '/' ? sb.getRoot() : cwd;
        for (String name : normalized.split("/")) {
            if (name.isEmpty()) continue;
            if (name.equals("..")) {
                if (current.getParent() != null) current = current.getParent();
                continue;
            }
            if (!current.isDirectory()) return null;
            current = current.getChild(name);
            if (current == null) return null;
        }
        return current;
    }

    public static Dentry lookupParent(SuperBlock sb, Dentry cwd, Path path) {
        String normalized = path.normalize().toString();
        if (normalized.isEmpty() || normalized.equals("/")) return null;
        int slash = normalized.lastIndexOf('/');
        String parent = slash < 0 ? "" : (slash == 0 ? "/" : normalized.substring(0, slash));
        return lookup(sb, cwd, Path.of(parent));
    }

    public static String name(Path path) {
        String normalized = path.normalize().toString();
        int slash = normalized.lastIndexOf('/');
        return slash < 0 ? normalized : normalized.substring(slash + 1);
    }
}
