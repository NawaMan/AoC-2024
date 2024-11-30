package common;

import java.nio.file.Files;
import java.nio.file.Path;

import functionalj.list.FuncList;

public interface AocCommon {
    
    static final String dataPath = "data";
    
    public static enum KIND {
        test,
        prod;
    }
    
    static final KIND test = KIND.test;
    static final KIND prod = KIND.prod;
    
    default String challengeName() {
        return  this.getClass().getSimpleName().replaceFirst("Test$", "");
    }
    
    default FuncList<String> readAllLines(KIND kind, String challenge) {
        return readAllLines(dataPath, kind, challenge);
    }
    
    default FuncList<String> readAllLines(String inputBase , KIND kind, String challengeName) {
        try {
            var inputFolder = challengeName.replaceAll("^Day([0-9]+).*$", "day$1");
            var challenge   = challengeName.replaceAll("^Day([0-9]+)Part([0-9]+)$", "day$1-part$2");
            var inputFile   = challenge + "-" + kind + ".txt";
            var lines       = Files.readAllLines(Path.of(inputBase, inputFolder, inputFile));
            return FuncList.from(lines);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
