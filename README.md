# Java EXIF Watermarker

A simple command-line tool written in Java to add a date watermark to your images. The tool reads the original shooting date from the image's EXIF metadata and draws it onto the picture.

## Features

- Extracts the shooting date (YYYY-MM-DD) from image EXIF data.
- Falls back to the file's last modified date if EXIF data is not available.
- Customizable watermark options:
  - Font size
  - Font color (RGB)
  - Position (Top-Left, Center, Bottom-Right)
- Processes all supported images in a specified directory.
- Saves new watermarked images in a `_watermark` sub-directory, leaving original images untouched.
- Supported formats: `.jpg`, `.jpeg`, `.png`.

## Requirements

- Java 11 or higher
- Apache Maven 3.6 or higher

## Build

To build the project and create an executable JAR file, run the following command from the project root directory:

```shell
mvn clean package
```

This will generate a file named `watermarker-1.0-SNAPSHOT-jar-with-dependencies.jar` in the `target` directory.

## Usage

Run the application from the command line, providing the path to the directory containing your images.

### Basic Usage

```shell
java -jar target/watermarker-1.0-SNAPSHOT-jar-with-dependencies.jar C:\path\to\your\images
```

### Advanced Usage with Options

You can customize the appearance and position of the watermark using the following options:

- `-s`, `--font-size`: Sets the font size. (Default: `48`)
- `-c`, `--color`: Sets the font color in `R,G,B` format. (Default: `255,255,255` for white)
- `-p`, `--position`: Sets the watermark position. Valid values: `TOP_LEFT`, `CENTER`, `BOTTOM_RIGHT`. (Default: `BOTTOM_RIGHT`)

**Example:**

To apply a yellow watermark, with font size 36, at the bottom-right corner:

```shell
java -jar target/watermarker-1.0-SNAPSHOT-jar-with-dependencies.jar C:\path\to\your\images -s 36 -c "255,255,0" -p BOTTOM_RIGHT
```
