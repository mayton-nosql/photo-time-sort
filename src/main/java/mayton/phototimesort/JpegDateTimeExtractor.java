package mayton.phototimesort;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.fieldtypes.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

public class JpegDateTimeExtractor {

    public static Logger logger = LoggerFactory.getLogger(JpegDateTimeExtractor.class);

    private static class Singleton {
        public static final JpegDateTimeExtractor INSTANCE = new JpegDateTimeExtractor();
    }

    public static JpegDateTimeExtractor instance() {
        return Singleton.INSTANCE;
    }

    public Optional<String> safeGetValue(TiffField tiffField) {
        try {
            return Optional.of(tiffField.getStringValue());
        } catch (ImageReadException e) {
            return Optional.empty();
        }
    }

    public Optional<LocalDateTime> applyDateTimeFormatters(String dateTimeVal, List<DateTimeFormatter> dateTimeFormatters) {
        for(DateTimeFormatter dateTimeFormatter : dateTimeFormatters) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(dateTimeVal, dateTimeFormatter);
                return Optional.of(localDateTime);
            } catch (DateTimeParseException ex) {
                // Nothing to do
            }
        }
        return Optional.empty();
    }

    public Optional<LocalDateTime> fromFile(InputStream inputStream) {
        return fromFile(inputStream, asList("DateTime", "DateTimeOriginal", "DateTimeDigitized"), asList(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")));
    }

    public Optional<LocalDateTime> fromFile(InputStream inputStream, List<String> exifTags, List<DateTimeFormatter> dateTimeFormatters) {
        ImageMetadata metadata;
        Optional<String> value = Optional.empty();
        Optional<LocalDateTime> result = Optional.empty();
        try {
            metadata = Imaging.getMetadata(inputStream, null);
            if (metadata != null) {
                TiffImageMetadata items = ((JpegImageMetadata) metadata).getExif();
                if (items != null) {
                    if (logger.isDebugEnabled()) {
                        for (TiffField field : items.getAllFields()) {
                            logger.debug("..processing tag {}", field.toString());
                        }
                    }
                    for (TiffField field : items.getAllFields()) {
                        if (field.getFieldType() == FieldType.ASCII) {
                            String tagName = field.getTagName();
                            for (String tag : exifTags) {
                                if (tag.equals(tagName)) {
                                    value = safeGetValue(field);
                                    if (value.isPresent()) {
                                        return applyDateTimeFormatters(value.get(), dateTimeFormatters);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    logger.warn("No tiff fields detected!");
                }
            } else {
                logger.warn("No TiffImageMetadata detected!");
            }
        } catch (ClassCastException e) {
            logger.error("ClassCastException", e);
        } catch (NullPointerException e) {
            logger.error("NullPointerException", e);
        } catch (ImageReadException e) {
            logger.error("ImageReadException", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        } catch (DateTimeParseException e) {
            logger.warn("DateTimeParseException during parse " + value.get(), e);
        }
        return result;
    }

}
