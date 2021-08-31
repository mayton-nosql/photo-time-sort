package mayton.phototimesort;

import mayton.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
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

    static Logger logger = LoggerFactory.getLogger(JpegFileVisitor.class);

    public static final Pattern JPEG_EXTENSION = Pattern.compile(".+\\.(?<extension>jpg|jfif|jpe|jpeg)$", Pattern.CASE_INSENSITIVE);

    private final List<String> exiftags;

    private List<DateTimeFormatter> dateTimeFormatter;

    private DateTimeFormatter pathFormatter;

    private JpegDateTimeExtractor jpegDateTimeExtractor = JpegDateTimeExtractor.instance();

    private File destDir;
    private File trash;

    private int observed = 0;

    private int processed = 0;

    private long processedBytes = 0;

    private int withoutDateInformation = 0;

    private ICopyStrategy copyStrategy;

    public JpegFileVisitor(File sourceDir, File destDir, File trash, String pathFormat, List<String> exiftags, List<String> timeFormats, ICopyStrategy copyStrategy) {
        this.destDir = destDir;
        this.trash = trash;
        this.pathFormatter = DateTimeFormatter.ofPattern(pathFormat);
        this.dateTimeFormatter = timeFormats.stream().map(x -> DateTimeFormatter.ofPattern(x)).collect(Collectors.toList());
        this.exiftags = exiftags;
        this.copyStrategy = copyStrategy;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String path = file.toAbsolutePath().toString();
        logger.debug("Vizit file path = {}", path);
        Matcher matcher = JPEG_EXTENSION.matcher(path);
        if (matcher.matches()) {
            logger.info("Processing JPEG file {}",  path);
            observed++;
            String extension = matcher.group("extension");
            Optional<LocalDateTime> dateTime = jpegDateTimeExtractor.fromFile(new FileInputStream(path), exiftags, dateTimeFormatter);
            if (dateTime.isPresent()) {
                logger.info("Recognized exif tag like a date format {}", dateTime.get());
                String syntheticPath = pathFormatter.format(dateTime.get());
                // TODO: This it not a good idea to create folder every time. Should be fixed.
                new File(destDir, FileUtils.cropLastPathElement(syntheticPath)).mkdirs();
                int suffix = 0;
                File jpegFileObj;
                String jpegPath = "";
                do {
                    jpegPath = destDir.getAbsolutePath() + FileUtils.SEPARATOR + syntheticPath + (suffix == 0 ? "" : "(" + suffix + ")") + "." + extension;
                    suffix++;
                    logger.debug("Checking path {}", jpegPath);
                    jpegFileObj = new File(jpegPath);
                } while(jpegFileObj.exists());
                try {
                    copyStrategy.copy(path, jpegPath);
                    processed++;
                    processedBytes += file.toFile().length();
                } catch (Exception ex) {
                    logger.warn("Exception during copy {}", ex.getMessage());
                }
            } else {
                logger.warn("Unable to detect exif-date attribute set in file {}", path);
                withoutDateInformation++;
                if (trash != null) {
                    logger.debug("Saving into trash {}", trash.getAbsolutePath());
                    try {
                        copyStrategy.copy(path, trash.getAbsolutePath() + FileUtils.SEPARATOR + UUID.randomUUID().toString() + "." + extension);
                        processed++;
                    } catch (Exception ex) {
                        logger.warn("Exception during saving trash {}", ex.getMessage());
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

    public long getProcessedBytes() {
        return processedBytes;
    }
}
