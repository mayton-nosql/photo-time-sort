package mayton.phototimesort.strategies;

import mayton.phototimesort.ICopyStrategy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class HardLinkCopyStrategy implements ICopyStrategy {
    @Nullable
    @Override
    public Set<String> applicableToOs() {
        return Set.of("linux");
    }

    @Nullable
    @Override
    public Set<String> applicableToFileSystems() {
        return Set.of("ext2", "ext3", "ext4", "xfs", "btrfs");
    }

    @Override
    public void copy(@Nonnull String sourcePath, @Nonnull String destPath) throws IOException {
        Files.createLink(Path.of(destPath), Path.of(sourcePath));
    }
}
