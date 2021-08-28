package mayton.phototimesort.strategies;

import mayton.phototimesort.ICopyStrategy;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.Set;

public class DummyCopyStrategy implements ICopyStrategy {

    @Nullable
    @Override
    public Set<String> applicableToOs() {
        return null;
    }

    @Nullable
    @Override
    public Set<String> applicableToFileSystems() {
        return null;
    }

    @Override
    public void copy(@Nonnull String sourcePath, @Nonnull String destPath) throws IOException {
        InputStream inputStream = new FileInputStream(sourcePath);
        OutputStream outputStream = new FileOutputStream(destPath);
        IOUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
    }
}
