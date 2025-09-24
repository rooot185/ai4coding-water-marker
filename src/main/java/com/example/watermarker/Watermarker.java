package com.example.watermarker;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.Callable;

@Command(name = "watermarker", mixinStandardHelpOptions = true, version = "Watermarker 1.0",
        description = "Adds a date watermark to images based on EXIF data.")
public class Watermarker implements Callable<Integer> {

    private static final String[] SUPPORTED_EXTENSIONS = {".jpg", ".jpeg", ".png"};

    @Parameters(index = "0", description = "The directory containing images to watermark.")
    private File inputDirectory;

    @Option(names = {"-s", "--font-size"}, description = "Font size of the watermark text. Default: 48.", defaultValue = "48")
    private int fontSize;

    @Option(names = {"-c", "--color"}, description = "Color of the watermark text in R,G,B format. Default: 255,255,255.", defaultValue = "255,255,255")
    private String colorStr;

    @Option(names = {"-p", "--position"}, description = "Position of the watermark. Valid values: ${COMPLETION-CANDIDATES}. Default: BOTTOM_RIGHT.", defaultValue = "BOTTOM_RIGHT")
    private Position position;

    @Override
    public Integer call() throws Exception {
        if (!inputDirectory.isDirectory()) {
            System.err.println("Error: Provided path is not a directory.");
            return 1;
        }

        Path outputDirectory = Paths.get(inputDirectory.getAbsolutePath(), inputDirectory.getName() + "_watermark");
        try {
            Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            System.err.println("Error: Could not create output directory: " + outputDirectory);
            e.printStackTrace();
            return 1;
        }

        System.out.println("Output directory: " + outputDirectory);

        Color color = parseColor(colorStr);
        if (color == null) {
            System.err.println("Error: Invalid color format. Please use R,G,B (e.g., '255,0,0').");
            return 1;
        }

        System.out.println("--- Debug: All files found in directory ---");
        File[] allFiles = inputDirectory.listFiles();
        if (allFiles != null) {
            for (File f : allFiles) {
                System.out.println(f.getName());
            }
        }
        System.out.println("-----------------------------------------");

        File[] files = inputDirectory.listFiles((dir, name) ->
                Arrays.stream(SUPPORTED_EXTENSIONS).anyMatch(ext -> name.toLowerCase().endsWith(ext)));

        if (files == null || files.length == 0) {
            System.out.println("No supported image files found in the directory.");
            return 0;
        }

        for (File file : files) {
            try {
                addWatermark(file, outputDirectory, color);
                System.out.println("Watermarked: " + file.getName());
            } catch (Exception e) {
                System.err.println("Failed to watermark " + file.getName() + ": " + e.getMessage());
            }
        }
        return 0;
    }

    private void addWatermark(File imageFile, Path outputDir, Color color) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IOException("Could not read image file. Format may not be supported.");
        }

        String dateText = getExifDate(imageFile);

        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
        g.setColor(color);

        FontMetrics metrics = g.getFontMetrics();
        int x = 0;
        int y = 0;
        int margin = (int) (fontSize * 0.5);

        switch (position) {
            case TOP_LEFT:
                x = margin;
                y = metrics.getAscent();
                break;
            case CENTER:
                x = (image.getWidth() - metrics.stringWidth(dateText)) / 2;
                y = (image.getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                break;
            case BOTTOM_RIGHT:
                x = image.getWidth() - metrics.stringWidth(dateText) - margin;
                y = image.getHeight() - metrics.getDescent() - margin;
                break;
        }

        g.drawString(dateText, x, y);
        g.dispose();

        String outputFileName = imageFile.getName();
        File outputFile = new File(outputDir.toFile(), outputFileName);
        ImageIO.write(image, "png", outputFile); // Save as PNG to preserve quality
    }

    private String getExifDate(File imageFile) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory != null) {
                // Date/Time Original
                Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault());
                if (date != null) {
                    return new SimpleDateFormat("yyyy-MM-dd").format(date);
                }
            }
        } catch (Exception e) {
            // Fallback if EXIF data is missing or corrupt
            System.err.println("Warning: Could not read EXIF date for " + imageFile.getName() + ". Using file modification date.");
        }
        // Fallback to file's last modified date
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(imageFile.lastModified()));
    }

    private Color parseColor(String rgb) {
        try {
            String[] parts = rgb.split(",");
            if (parts.length != 3) return null;
            return new Color(
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim()),
                    Integer.parseInt(parts[2].trim())
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Watermarker()).execute(args);
        System.exit(exitCode);
    }
}