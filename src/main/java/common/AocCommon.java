package common;

import java.nio.file.Files;
import java.nio.file.Path;

import functionalj.list.FuncList;

public interface AocCommon {
    
    static final String dataPath = "data";
    
    public static enum KIND {
        demo,
        prod;
    }
    
    static final KIND demo = KIND.demo;
    static final KIND prod = KIND.prod;
    
    default String challengeName() {
        return  this.getClass().getSimpleName().replaceFirst("Test$", "");
    }
    
    default FuncList<String> readAllLines(KIND kind, String challenge) {
        return readAllLines(dataPath, kind, challenge);
    }
    
    default FuncList<String> readAllLines(String path , KIND kind, String challenge) {
        try {
            var inputFile = challenge + "-" + kind + ".txt";
            var lines     = Files.readAllLines(Path.of(path, inputFile));
            return FuncList.from(lines);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
