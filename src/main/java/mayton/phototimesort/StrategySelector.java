package mayton.phototimesort;

import org.apache.commons.text.CaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class StrategySelector {

    static Logger logger = LoggerFactory.getLogger(StrategySelector.class);

    public static String strategyToClassName(String strategyName) {
        return CaseUtils.toCamelCase(strategyName, true, '_') + "CopyStrategy";
    }

    public static ICopyStrategy directSelectStrategy(String strategyName) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        logger.debug("strategy = {}", strategyName);
        String className = "mayton.phototimesort.strategies." + strategyToClassName(strategyName);
        logger.debug("className = {}", className);
        Class clazz = Class.forName(className);
        logger.debug("classObject = {}", clazz.getCanonicalName());
        Constructor constructor = clazz.getConstructors()[0];
        logger.debug("constructor detected");
        ICopyStrategy copyStrategy = (ICopyStrategy) constructor.newInstance();
        logger.debug("copyStrategy object = {}", copyStrategy.getClass().getCanonicalName());
        return copyStrategy;
    }

    public static ICopyStrategy autoDetectStrategy(String sourceFolderRoot, String destFolderRoot) {
        String os = System.getProperty("os.name");
        if (os.equals("windows")) {
            return null;
        } else if (os.equals("linux")) {
            return null;
        } else {
            return null;
        }
    }

}
