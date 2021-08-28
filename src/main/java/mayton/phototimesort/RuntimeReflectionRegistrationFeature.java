package mayton.phototimesort;

import mayton.phototimesort.strategies.*;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

public class RuntimeReflectionRegistrationFeature implements Feature {

    public void beforeAnalysis(BeforeAnalysisAccess access) {
        try {
            RuntimeReflection.register(CopyOnWriteCopyStrategy.class);
            RuntimeReflection.register(DummyCopyStrategy.class);
            RuntimeReflection.register(FileChannelCopyStrategy.class);
            RuntimeReflection.register(HardLinkCopyStrategy.class);
            RuntimeReflection.register(SymLinkCopyStrategy.class);
        } catch (Exception ex) {

        }
    }

}
