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
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
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
        Map<String, SpriteActionData> actionData = Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)
                .filter(p -> p.toFile().isFile())
                .filter(p -> p.toString().endsWith("png"))
                .sorted(Comparator.comparing(Path::toString))
                .map(f -> extractActionData(rootPath, f))
                .filter(a -> a.getAnchor() != null || a.getAttackbox() != null)
                .collect(Collectors.toMap(SpriteActionData::getFilename, Function.identity()));

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
            if (image == null) {
                throw new IOException("Not an image file, image is null : " + frame);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read frame file " + frame, e);
        }
        Point anchor = findAnchor(image);
        Area attackbox = findArea(image, Color.RED);
        Area lifebox = findArea(image, Color.GREEN);

        return new SpriteActionData(filename, anchor, attackbox, lifebox);
    }

    private static Area findArea(BufferedImage image, Color color) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int clr = image.getRGB(x, y);
                int red = color.extractValue(clr);
                if (red > 0) {
                    int topX = searchLastPresentHorizontally(image, x, y, color);
                    int bottomY = searchLastPresentVertically(image, y, x, color);
                    return new Area(x, image.getHeight() - bottomY, topX - x, bottomY - y);
                }
            }
        }
        return null;
    }

    private static int searchLastPresentHorizontally(BufferedImage image, int from, int y, Color color) {
        for (int searchX = from; searchX < image.getWidth(); searchX++) {
            int searchRed = color.extractValue(image.getRGB(searchX, y));
            if (searchRed == 0) {
                return searchX;
            }
        }
        return image.getWidth();
    }


    private static int searchLastPresentVertically(BufferedImage image, int from, int x, Color color) {
        for (int searchY = from; searchY < image.getHeight(); searchY++) {
            int searchRed = color.extractValue(image.getRGB(x, searchY));
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
                int blue = Color.BLUE.extractValue(rgb);
                if (blue > 0) {
                    return new Point(x, image.getHeight() - y);
                }
            }
        }
        return null;
    }

    private enum Color {
        RED {
            @Override
            int extractValue(int rgb) {
                return (rgb & 0x00ff0000) >> 16;
            }
        }, GREEN {
            @Override
            int extractValue(int rgb) {
                return (rgb & 0x0000ff00) >> 8;
            }

        }, BLUE {
            @Override
            int extractValue(int rgb) {
                return rgb & 0x000000ff;
            }

        };

        abstract int extractValue(int rgb);
    }


}
