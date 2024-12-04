package main;

import static functionalj.function.Func.f;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.write;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import functionalj.function.Func;
import functionalj.function.Func1;

public class FilesCopier {
    
    private final Func1<String, String> pathMapper;
    private final Func1<String, String> nameMapper;
    private final Func1<String, String> contentMapper;
    
    public FilesCopier(Func1<String, String> mapper) {
        this(mapper, mapper, mapper);
    }
    
    public FilesCopier(
            Func1<String, String> pathMapper,
            Func1<String, String> nameMapper,
            Func1<String, String> contentMapper) {
        this.pathMapper    = (pathMapper    != null) ? pathMapper    : Func.it();
        this.nameMapper    = (nameMapper    != null) ? nameMapper    : Func.it();
        this.contentMapper = (contentMapper != null) ? contentMapper : Func.it();
    }
    
    public boolean copyFiles(String path) throws IOException {
        var srcDir = Paths.get(path);
        var dstDir = Paths.get(pathMapper.apply(path));
        
        if (exists(dstDir)) {
            return false;
        }
        
        createDirectories(dstDir);
        
        Files.list(srcDir)
        .forEach(f(srcPath -> {
            var srcFileName = srcPath.getFileName().toString();
            var dstFileName = nameMapper.apply(srcFileName);
            
            var dstPath = dstDir.resolve(dstFileName);
            Files.copy(srcPath, dstPath, REPLACE_EXISTING);
            
            var srcContent = readString(srcPath);
            var dstContent = contentMapper.apply(srcContent);
            write(dstPath, dstContent.getBytes());
        }));
        return true;
    }
    
}
