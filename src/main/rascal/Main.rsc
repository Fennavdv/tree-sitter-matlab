module Main

import IO;
import DateTime;

int main(int testArgument=0) {
    datetime before = now();
    list[node] asts = runParser();
    datetime after = now();
    print(createDuration(before, after));
    
    return 0;
}

@javaClass{com.example.Parser}
public java list[node] runParser();
