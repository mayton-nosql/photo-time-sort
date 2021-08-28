package mayton.phototimesort;

import mayton.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JpegFileVisitor extends SimpleFileVisitor<Path> {

    public static Logger logger = LoggerFactory.getLogger(JpegFileVisitor.class);

    public static final Pattern JPEG_EXTENSION = Pattern.compile(".+\\.(?<extension>jpg|jfif|jpe|jpeg)$", Pattern.CASE_INSENSITIVE);

    private final List<String> exiftags;

    private List<DateTimeFormatter> dateTimeFormatter;

    private DateTimeFormatter pathFormatter;

    private JpegDateTimeExtractor jpegDateTimeExtractor = JpegDateTimeExtractor.instance();

    private File destDir;
    private File trash;

    private int observed = 0;

    private int processed = 0;

    private int withoutDateInformation = 0;


    public JpegFileVisitor(File sourceDir, File destDir, File trash, String pathFormat, List<String> exiftags, List<String> timeFormats) {
        this.destDir = destDir;
        this.trash = trash;
        this.pathFormatter = DateTimeFormatter.ofPattern(pathFormat);
        this.dateTimeFormatter = timeFormats.stream().map(x -> DateTimeFormatter.ofPattern(x)).collect(Collectors.toList());
        this.exiftags = exiftags;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String path = file.toAbsolutePath().toString();
        logger.debug("Vizit file path = {}", path);
        Matcher matcher = JPEG_EXTENSION.matcher(path);
        if (matcher.matches()) {
            logger.info("Processing JPEG file {}", path);
            observed++;
            String extension = matcher.group("extension");
            Optional<LocalDateTime> dateTime = jpegDateTimeExtractor.fromFile(new FileInputStream(path), exiftags, dateTimeFormatter);
            if (dateTime.isPresent()) {
                logger.info("Recognized exif tag like a date format '{}'", dateTime.get());
                String syntheticPath = pathFormatter.format(dateTime.get());
                // TODO: This it not a good idea to create folder every time. Should be fixed.
                new File(destDir, FileUtils.cropLastPathElement(syntheticPath)).mkdirs();
                String jpegPath = destDir.getAbsolutePath() + FileUtils.SEPARATOR + syntheticPath + "." + extension;
                logger.debug("Saving to {}",  jpegPath);
                try(InputStream i = new FileInputStream(path);
                    OutputStream o = new FileOutputStream(jpegPath)) {
                    // TODO: Windows: implement zero-copy option with
                    //      - hardlinks if possible
                    // TODO: Linux: implement zero-copy option with
                    //      - COW (cp --reflink) for Btrfs, XFS
                    //      - symlinks for all filesystems
                    // TODO: Java: is it possible to copy with File Channels?
                    IOUtils.copy(i, o);
                    processed++;
                }
            } else {
                logger.warn("Unable to detect exif-date attribute set in file {}", path);
                withoutDateInformation++;
                if (trash != null) {
                    logger.debug("Saving into trash path {}", trash.getAbsolutePath());
                    try(InputStream i = new FileInputStream(path);
                        OutputStream o = new FileOutputStream(trash.getAbsolutePath() + FileUtils.SEPARATOR + UUID.randomUUID().toString() + "." + extension)) {
                        IOUtils.copy(i, o);
                    }
                }
            }
        }
        return FileVisitResult.CONTINUE;
    }

    public int getProcessed() {
        return processed;
    }

    public int getObserved() {
        return observed;
    }

    public int getWithoutDateInformation() {
        return withoutDateInformation;
    }
}
