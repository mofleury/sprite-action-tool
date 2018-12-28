package org.mofleury.sprite.tool;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.concurrent.Callable;

@Slf4j
@CommandLine.Command(description = "Extracts action metadata from action sprites",
        name = "sprite-tool", mixinStandardHelpOptions = true, version = "0.0.1")
class SpriteTool implements Callable<Void> {

    @CommandLine.Parameters(index = "0", description = "Root directory of action sprites")
    private File root;


    @CommandLine.Parameters(index = "1", description = "output JSON file")
    private File output;

//    @CommandLine.Option(names = {"-a", "--algorithm"}, description = "MD5, SHA-1, SHA-256, ...")
//    private String algorithm = "MD5";

    public static void main(String[] args) throws Exception {
        // SpriteTool implements Callable, so parsing, error handling and handling user
        // requests for usage help or version help can be done with one line of code.
        CommandLine.call(new SpriteTool(), args);
    }

    @Override
    public Void call() throws Exception {

        Files.walk(root.toPath(), FileVisitOption.FOLLOW_LINKS)
                .forEach(f -> log.info(f.toString()));

        return null;
    }
}
