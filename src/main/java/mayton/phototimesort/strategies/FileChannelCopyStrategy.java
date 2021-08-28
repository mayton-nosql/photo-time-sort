package mayton.phototimesort.strategies;

import mayton.phototimesort.ICopyStrategy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Set;

public class FileChannelCopyStrategy implements ICopyStrategy {

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
        try(FileInputStream is = new FileInputStream(sourcePath);
            FileOutputStream os = new FileOutputStream(destPath);
            FileChannel sourceChannel = is.getChannel();
            FileChannel destChannel = os.getChannel()) {
            sourceChannel.transferTo(0, sourceChannel.size(), destChannel);
        } catch (IOException ex) {
            throw ex;
        }
    }
}
