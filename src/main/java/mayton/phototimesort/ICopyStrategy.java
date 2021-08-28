package mayton.phototimesort;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Set;

public interface ICopyStrategy {

    /**
     *
     * @return null - means "all" operating systems are applicable
     */
    @Nullable Set<String> applicableToOs();

    /**
     *
     * @return null - means "all" file systems are applicable
     */
    @Nullable Set<String> applicableToFileSystems();

    void copy(@Nonnull String sourcePath,@Nonnull String destPath) throws IOException;

}
