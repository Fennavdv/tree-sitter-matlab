module Main

import IO;
import DateTime;
import util::ShellExec;
import util::SystemAPI;
import util::FileSystem;
import lang::xml::IO;
import IO;
import Type;
import String;
import List;
import Location;
import Set;
import Node;

node main(int testArgument=0) {
    datetime before = now();
    list[loc] files = toList(files(|cwd:///src/main/resources/exampleFiles|));
    list[str] paths = getPaths(files);
    writeFileLines(|cwd:///src/main/resources/paths.txt|, paths);
    println(paths);
    str x = exec(|PATH:///tree-sitter.cmd|, args=["parse", "--paths", "src\\main\\resources\\paths.txt", "-p", "..\\tree-sitter-matlab", "-x"]);
    x = substring(x, 22);
    value ast = readXML(x);
    datetime after = now();
    print(createDuration(before, after));
    if(node n := ast) {
        return n;
    }
    return arbNode();
}

@javaClass{com.example.Parser}
public java list[node] runParser();

list[str] getPaths(list[loc] locations) {
    list[str] paths = [];
    for(int i <- [0 .. size(locations)]) {
        str path = locations[i].path[1..];
        if(endsWith(path, ".m")) {
            paths = push(path, paths);
        } 
    }
    return paths;
}

list[value] getASTs(list[str] paths) {
    list[value] asts = [];
    for(int i <- [0 .. size(paths)]) {
        str x = exec(|PATH:///tree-sitter.cmd|, args=["parse", paths[i], "..\\tree-sitter-matlab", "-x"]);
        x = substring(x, 22);
        value ast = readXML(x);
        asts = push(ast, asts);
    }
    return asts;
}
