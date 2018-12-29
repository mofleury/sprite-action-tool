package org.mofleury.sprite.tool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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

        Path rootPath = root.toPath();
        List<SpriteActionData> actionData = Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)
                .filter(p -> p.toFile().isFile())
                .sorted(Comparator.comparing(Path::toString))
                .map(f -> extractActionData(rootPath, f))
                .collect(Collectors.toList());

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        try (FileWriter writer = new FileWriter(output)) {
            gson.toJson(actionData, writer);
        }

        return null;
    }

    private static SpriteActionData extractActionData(Path root, Path frame) {
        String filename = root.relativize(frame).toString();

        File file = frame.toFile();
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read frame file " + frame, e);
        }
        Point anchor = findAnchor(image);
        Area attackbox = findAttackBox(image);

        return new SpriteActionData(filename, anchor, attackbox);
    }

    private static Area findAttackBox(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int clr = image.getRGB(x, y);
                int red = extractRed(clr);
                if (red > 0) {
                    int topX = searchLastRedHorizontally(image, x, y);
                    int bottomY = searchLastRedVertically(image, y, x);
                    return new Area(x, image.getHeight() - bottomY, topX - x, bottomY - y);
                }
            }
        }
        return null;
    }

    private static int searchLastRedHorizontally(BufferedImage image, int from, int y) {
        for (int searchX = from; searchX < image.getWidth(); searchX++) {
            int searchRed = extractRed(image.getRGB(searchX, y));
            if (searchRed == 0) {
                return searchX;
            }
        }
        return image.getWidth();
    }


    private static int searchLastRedVertically(BufferedImage image, int from, int x) {
        for (int searchY = from; searchY < image.getHeight(); searchY++) {
            int searchRed = extractRed(image.getRGB(x, searchY));
            if (searchRed == 0) {
                return searchY;
            }
        }
        return image.getHeight();
    }

    private static Point findAnchor(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                int blue = extractBlue(rgb);
                if (blue > 0) {
                    return new Point(x, image.getHeight() - y);
                }
            }
        }
        return null;
    }

    private static int extractBlue(int rgb) {
        return rgb & 0x000000ff;
    }

    private static int extractGreen(int rgb) {
        return (rgb & 0x0000ff00) >> 8;
    }

    private static int extractRed(int rgb) {
        return (rgb & 0x00ff0000) >> 16;
    }
}
