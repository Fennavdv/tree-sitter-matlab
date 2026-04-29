
package org.treesitter;

import org.treesitter.utils.NativeUtils;

public class TreeSitterMatlab extends TSLanguage {

    static {
        NativeUtils.loadLib("lib/tree-sitter-matlab");
    }
    private native static long tree_sitter_matlab();

    public TreeSitterMatlab() {
        super(tree_sitter_matlab());
    }

    private TreeSitterMatlab(long ptr) {
        super(ptr);
    }

    @Override
    public TSLanguage copy() {
        return new TreeSitterMatlab(copyPtr());
    }
}
