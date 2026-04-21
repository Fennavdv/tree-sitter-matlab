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
import java.util.List;
import io.usethesource.capsule.Map;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.INode;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.IString;
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

        ArrayList<Path> filesInDirList = getMatlabFiles("src/main/resources/exampleFiles");
        Path[] filesInDir = filesInDirList.toArray(new Path[filesInDirList.size()]);
        TSTree[] asts = new TSTree[filesInDir.length];
        List<String[]> strs = new ArrayList<String[]>();
        String[] locs = new String[filesInDir.length];
        if(filesInDir != null) {
            for(int i = 0; i < filesInDir.length; i++) {
                Path path = filesInDir[i];
                locs[i] = filesInDir[i].toString();
                String str = "";
                try {
                    str = Files.readString(path) + "\n";
                    List<String> lines = Files.readAllLines(filesInDir[i]);
                    strs.add(lines.toArray(new String[lines.size()]));
                } catch (IOException e) {
                    System.out.println(path);
                    e.printStackTrace();
                }
                asts[i] = parser.parseString(null, str);
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
            trees[i] = convertToNode(asts[i].getRootNode(), strs.get(i), locs[i]);
        }
        System.out.println(asts[0].getRootNode());
        System.out.println(asts.length);
        return trees[0];
    }

    public INode convertToNode(TSNode node, String[] code, String location) {
        String type = node.getType();
        if(node.isError()) {
            System.out.println("Error in: " + location + " at startpoint: " + " x: " + node.getStartPoint().getRow() + " y: " + node.getStartPoint().getColumn() + " endpoint: " + " x: " + node.getEndPoint().getRow() + " y: " + node.getEndPoint().getColumn());
        }
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
        if(type.equals("identifier") || type.equals("comment")) {
            children.add(values.list(getCodeText(node.getStartPoint(), node.getEndPoint(), code)));
        }
        //System.out.println(fields);
        INode nodeWithoutFields = values.node(type, children.toArray(new IValue[children.size()]));
        return new NodeWithKeywordParametersFacade(nodeWithoutFields, fields);
    }

    public ArrayList<Path> getMatlabFiles(String filePath) {
        ArrayList<Path> files = new ArrayList<Path>();
        File file = new File(filePath);
        if(file.isDirectory()) {
            File[] containedFiles = file.listFiles();
            for(int i = 0; i < containedFiles.length; i++) {
                files.addAll(getMatlabFiles(containedFiles[i].toString()));
                //System.out.println(filePath + "/" + containedFiles[i]);
            }
            return files;
        }
        else if(file.isFile() && filePath.endsWith(".m")) {
            files.add(Path.of(filePath));
            return files;
        }
        return files;
    }

    public IString[] getCodeText(TSPoint startPoint, TSPoint endPoint, String[] code) {
        int startLine = startPoint.getRow();
        int startColumn = startPoint.getColumn();
        int endLine = endPoint.getRow();
        int endColumn = endPoint.getColumn();
        if(endColumn + 1 > code[endLine].length()) {
            endColumn = code[endLine].length() - 1;
        }
        if (startLine == endLine) {
            IString[] result = new IString[1];
            result[0] = values.string(code[startLine].substring(startColumn, endColumn));
            return result;
        }

        IString[] result = new IString[endLine - startLine + 1];
        result[0] = values.string(code[startLine].substring(startColumn));
        int i = 1;
        for(int j = startLine + 1; j < endLine; j++) {
            result[i] = values.string(code[j]);
            i++;
        }
        result[i] = values.string(code[endLine].substring(0, endColumn));
        return result;
    }
}