package com.example;

import org.treesitter.TSParser;
import org.treesitter.TSLanguage;
import org.treesitter.TSTree;
import org.treesitter.TSNode;
import org.treesitter.TSPoint;
import org.treesitter.TreeSitterMatlab;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import io.usethesource.capsule.Map;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.INode;
import io.usethesource.vallang.impl.fields.NodeWithKeywordParametersFacade;

// imports are omitted
public class Parser {

    private final IValueFactory values;

    public Parser(IValueFactory values){
        this.values = values;
    }

   public INode runParser() {
        TSParser parser = new TSParser();
        // Use `TSLanguage.load` instead if you would like to load parsers as shared object(.so, .dylib, or .dll).
        // TSLanguage.load("path/to/languane/shared/object", "tree_sitter_some_lang");
        TSLanguage json = new TreeSitterMatlab();
        parser.setLanguage(json);

        File dir = new File("src/main/resources/examples");
        File[] filesInDir = dir.listFiles();
        TSTree[] asts = new TSTree[filesInDir.length];
        String[] strs = new String[filesInDir.length];
        String[] locs = new String[filesInDir.length];
        if(filesInDir != null) {
            for(int i = 0; i < filesInDir.length; i++) {
                String pathString = "src/main/resources/examples/" + filesInDir[i].getName();
                Path path = Path.of(pathString);
                locs[i] = pathString;
                String str = "";
                try {
                    str = Files.readString(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                asts[i] = parser.parseString(null, str);
                strs[i] = str;
            }
        }
        /*for(int i = 0; i < asts.length; i++) {
            TSTreeCursor cursor = new TSTreeCursor(asts[i].getRootNode());
            boolean reachedRoot = false;
            while (!reachedRoot) {
                TSNode current = cursor.currentNode();
                if(current.isNamed()) {
                    String type = current.getType();

                    if(type.equals("identifier")) {
                        String test = strs[i].substring(current.getStartByte(),current.getEndByte());
                        System.out.println(test);
                    }
                }

                if (cursor.gotoFirstChild()) {
                    continue;
                }

                if (cursor.gotoNextSibling()) {
                    continue;
                }

                boolean retracing = true;
                while (retracing) {
                    if (!cursor.gotoParent()) {
                        retracing = false;
                        reachedRoot = true;
                    }

                    if (cursor.gotoNextSibling()) {
                        retracing = false;
                    }
                }
            }
        }*/
        /*String string = "";
        try {
            string = Files.readString(Path.of("src/main/resources/plotlyfig.m"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        TSTree tree = parser.parseString(null, string);
        TSNode rootNode = tree.getRootNode();
        System.out.println(rootNode);
        System.out.println(string);*/
        INode[] trees = new INode[asts.length];
        for(int i = 0; i < asts.length; i++) {
            trees[i] = convertToNode(asts[i].getRootNode(), strs[i], locs[i]);
        }
        System.out.println(asts[0].getRootNode());
        System.out.println(asts.length);
        return trees[0];
    }

    public INode convertToNode(TSNode node, String code, String location) {
        String type = node.getType();
        int childCount = node.getChildCount();
        ArrayList<IValue> children = new ArrayList<IValue>();
        TSPoint startPoint = node.getStartPoint();
        TSPoint endPoint = node.getEndPoint();
        Map.Immutable<String, IValue> fields = Map.Immutable.of("location", values.string(location));
        fields = fields.__put("startByte", values.integer(node.getStartByte()));
        fields = fields.__put("endByte", values.integer(node.getEndByte()));
        fields = fields.__put("startPoint", values.tuple(values.integer(startPoint.getRow()), values.integer(startPoint.getColumn())));
        fields = fields.__put("endPoint", values.tuple(values.integer(endPoint.getRow()), values.integer(endPoint.getColumn())));
        for(int i = 0; i < childCount; i++) {
            TSNode child = node.getChild(i);
            if(!child.isNamed()) continue;
            IValue convertedChild = convertToNode(child, code, location);
            String fieldName = node.getFieldNameForChild(i);
            if (fieldName != null) {
                fields = fields.__put(fieldName, convertedChild);
                //System.out.println(fieldName);
            }
            else {
                children.add(convertedChild);
            }
        }
        if(type.equals("identifier")) {
            children.add(values.string(code.substring(node.getStartByte(),node.getEndByte())));
        }
        //System.out.println(fields);
        INode nodeWithoutFields = values.node(type, children.toArray(new IValue[children.size()]));
        return new NodeWithKeywordParametersFacade(nodeWithoutFields, fields);
    }
}