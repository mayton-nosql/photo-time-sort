package mayton.phototimesort;

import mayton.phototimesort.strategies.DummyCopyStrategy;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static mayton.FileUtils.replaceDot;

public class PhotoTimeSort {

    static Logger logger = LoggerFactory.getLogger(JpegFileVisitor.class);

    private final CommandLine line;

    private String pathFormat = "yyyy/MM/dd/HH-mm-ss";

    private List<String> exiftags = Arrays.asList("DateTime", "DateTimeOriginal", "DateTimeDigitized");

    private List<String> timeFormats = Arrays.asList("yyyy:MM:dd HH:mm:ss");

    private ICopyStrategy copyStrategy = new DummyCopyStrategy();

    public static Options createOptions() {
        return new Options()
                .addRequiredOption("s", "source", true, "Source jpeg files folder")
                .addRequiredOption("d", "dest", true, "Dest folder")
                .addOption("f", "timeformat", true, "Comma-separated local date-time format for exif tag. Default = 'yyyy:MM:dd HH:mm:ss'")
                .addOption("o", "outformat", true, "Output folder format. Default = 'yyyy/MM/dd/HH-mm-ss'")
                .addOption("x", "exiftags", true, "Comma-separated exif-tags list. Default = 'DateTime,DateTimeOriginal,DateTimeDigitized'")
                .addOption("t", "trash", true, "Unrecognized files")
                .addOption("c", "copystrategy", true, "Copy-strategy = { DUMMY | FILE_CHANNEL | HARD_LINK | SYM_LINK }. Default = DUMMY");
    }

    public static List<String> csvToStringArray(String csv) {
        return Arrays.stream(csv.split(Pattern.quote(","))).collect(Collectors.toList());
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(160);
        formatter.printHelp("java -jar photo-time-sort.jar", createOptions(), true);
    }

    public PhotoTimeSort(CommandLine line) {
        this.line = line;
    }

    public void process() throws IOException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        File sourceDir = new File(replaceDot(line.getOptionValue("s")));
        File trash = null;
        File destDir = new File(replaceDot(line.getOptionValue("d")));
        if (line.hasOption("f")) {
            timeFormats = csvToStringArray(line.getOptionValue("f"));
        }
        if (line.hasOption("o")) {
            pathFormat = line.getOptionValue("o");
        }
        if (line.hasOption("x")) {
            exiftags = csvToStringArray(line.getOptionValue("x"));
        }
        if (line.hasOption("t")) {
            trash = new File(replaceDot(line.getOptionValue("t")));
        }
        if (line.hasOption("c")) {
            copyStrategy = StrategySelector.directSelectStrategy(line.getOptionValue("c"));
        }
        routePhotos(sourceDir, destDir, trash, exiftags, timeFormats, copyStrategy);
    }

    private void routePhotos(File sourceDir, File destDir, File trash, List<String> exiftags, List<String> timeFormats, ICopyStrategy copyStrategy) throws IOException {
        if (sourceDir.equals(destDir) || sourceDir.equals(trash) || destDir.equals(trash)) {
            logger.error("This is strongly not recommended to peek the same folder for source and dest or trush in combinations");
            return;
        }
        if (trash != null) {
            new File(trash.getAbsolutePath()).mkdirs();
        }
        JpegFileVisitor metaVisitor = new JpegFileVisitor(sourceDir, destDir, trash, pathFormat, exiftags, timeFormats, copyStrategy);
        StopWatch stopWatch = StopWatch.createStarted();
        Files.walkFileTree(Path.of(sourceDir.getAbsolutePath()), metaVisitor);
        stopWatch.stop();
        logger.info("==============================================");
        logger.info("Copy strategy                      : {}",   copyStrategy.getClass().getCanonicalName());
        long seconds = stopWatch.getTime(TimeUnit.SECONDS);
        logger.info("Elapsed time                       : {}",   seconds + "s");
        logger.info("Observed files                     : {}",   metaVisitor.getObserved());
        logger.info("Processed files                    : {}",   metaVisitor.getProcessed());
        logger.info("Processed size                     : {}",   FileUtils.byteCountToDisplaySize(metaVisitor.getProcessedBytes()));
        logger.info("AVG speed                          : {}/s", FileUtils.byteCountToDisplaySize(metaVisitor.getProcessedBytes() / seconds));
        logger.info("Files without date-time attributes : {}",   metaVisitor.getWithoutDateInformation());
    }

    public static void main(String[] args) throws ParseException, IOException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        CommandLineParser parser = new DefaultParser();
        Options options = createOptions();
        if (args.length == 0) {
            printHelp();
        } else {
            CommandLine line = parser.parse(options, args);
            PhotoTimeSort photoTimeSort = new PhotoTimeSort(line);
            photoTimeSort.process();
        }
    }

}
