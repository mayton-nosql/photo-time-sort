package mayton.phototimesort;

import mayton.phototimesort.strategies.DummyCopyStrategy;
import org.apache.commons.text.CaseUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

public class StrategySelector {

    public static Logger logger = Logger.getLogger("StrategySelector");

    public static String strategyToClassName(String strategyName) {
        return CaseUtils.toCamelCase(strategyName, true, '_') + "CopyStrategy";
    }

    public static ICopyStrategy directSelectStrategy(String strategyName) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        logger.finest("strategy = " + strategyName);
        String className = "mayton.phototimesort.strategies." + strategyToClassName(strategyName);
        logger.finest("className = " + className);
        Class clazz = Class.forName(className);
        logger.finest("classObject = " + clazz.getCanonicalName());
        Constructor constructor = clazz.getConstructors()[0];
        logger.finest("constructor detected");
        ICopyStrategy copyStrategy = (ICopyStrategy) constructor.newInstance();
        logger.finest("copyStrategy object = " + copyStrategy.getClass().getCanonicalName());
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
