package MyLang;

import java.util.*;
import java.nio.file.*;
import java.io.IOException;

import static MyLang.MyLangAST.*;

public class MyLangRunner {
    String mainFile;
    String workingDirectoryPath;
    Map<String, MyLangFile> compiledFiles = new HashMap<>();
    Map<String, MyLangModule> interpretedFiles = new HashMap<>();
    Map<String, MyLangAST.Module> typecheckedFiles = new HashMap<>();
    MyLangFile mainModule;
    Set<String> waitingFiles = new HashSet<>();

    public MyLangRunner(String file) {
        mainFile = file;
        var parentPath = Paths.get(file).getParent();
        workingDirectoryPath = parentPath == null ? "" : parentPath.toString();
    }

    MyLangInterpreter interpreter = new MyLangInterpreter();

    String resolvePath(MyLangPath path) {
        return path.names().stream()
            .map(Token::lexeme)
            .reduce(workingDirectoryPath, (String untilNow, String newFile) -> untilNow+"/"+newFile)
            + ".myl";
    }

    String unresolve(String path) {
        return Arrays.stream(path.substring(0, path.length()-4).split("/"))
            .reduce("", (var untilNow, var name) -> untilNow.equals("") ? name : untilNow + "." + name); 
    }

    public boolean gatherAllImports() throws IOException {
        return gatherImportsIn(mainFile, true);
    }

    private MyLangPath getPath(Import i) {
        return ((ImportDeclaration) i).Name();
    }

    private boolean gatherImportsIn(String fileName, boolean isMainModule) throws IOException {
        //System.out.println("Opening file "+fileName);
        var fileContent = Files.readString(Paths.get(fileName));
        Optional<MyLangFile> optionalFile = MyLangParser.parseFile(fileContent);
        if(!optionalFile.isPresent()) {
            return false;
        }
        MyLangFile file = optionalFile.get();

        // get imports
        List<Import> neededImports = file.imports();
        List<MyLangPath> paths = neededImports.stream()
            .map(this::getPath)
            .toList();
        // no circular references
        if(paths.stream()
            .anyMatch(path -> waitingFiles.contains(resolvePath(path)))) {
            System.out.println("Circular reference from file "+fileName+" to "+
                    paths.stream()
                        .filter(path -> waitingFiles.contains(resolvePath(path)))
                        .findFirst()
                        .get());
            return false;
        }
        waitingFiles.add(fileName);
        // compile imported files
        if(paths.stream()
            .filter(path -> !compiledFiles.containsKey(resolvePath(path)))
            .map(path -> { try {
                    return gatherImportsIn(resolvePath(path), false); 
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }})
            .anyMatch(result -> !result)) { // any import was not successfull
            return false;
        }

        if(!Typechecker.typechecks(this, file, fileName)) {
            return false;
        } else {
            if(isMainModule) this.mainModule = file;
            compiledFiles.put(fileName, file);
            waitingFiles.remove(fileName);
            return true;
        }
    }

    public void run() throws IOException {
        if(gatherAllImports())
            interpreter.interpretFile(this, mainModule, true);
    }
}
