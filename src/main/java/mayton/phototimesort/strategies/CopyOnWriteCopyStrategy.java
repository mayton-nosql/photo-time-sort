package mayton.phototimesort.strategies;

import mayton.phototimesort.ICopyStrategy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Set;

public class CopyOnWriteCopyStrategy implements ICopyStrategy {

    @Nullable
    @Override
    public Set<String> applicableToOs() {
        return Set.of("linux");
    }

    @Nullable
    @Override
    public Set<String> applicableToFileSystems() {
        return Set.of("xfs", "btrfs");
    }

    @Override
    public void copy(@Nonnull String sourcePath, @Nonnull String destPath) throws IOException {
        // TODO: $ cp --reflink=auto sourcePath destPath
        throw new RuntimeException("Not implemented yet!");
    }
}
