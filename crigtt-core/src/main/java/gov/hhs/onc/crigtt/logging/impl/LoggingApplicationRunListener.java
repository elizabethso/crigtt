package gov.hhs.onc.crigtt.logging.impl;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.gaffer.GafferConfigurator;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.util.StatusPrinter;
import gov.hhs.onc.crigtt.context.CrigttProperties;
import gov.hhs.onc.crigtt.context.impl.AbstractCrigttApplicationRunListener;
import gov.hhs.onc.crigtt.io.CrigttFileExtensions;
import gov.hhs.onc.crigtt.logging.CrigttLoggingInitializer;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.ResourceUtils;

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class LoggingApplicationRunListener extends AbstractCrigttApplicationRunListener {
    private static class DefaultLoggingInitializer extends AbstractCrigttLoggingInitializer {
        public DefaultLoggingInitializer(LoggerContext loggerContext, String defaultLogFileName) {
            super(loggerContext, defaultLogFileName);
        }
    }

    private final static String LOGBACK_CONFIG_FILE_URL_PATH_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crigtt/logback/logback-";

    public LoggingApplicationRunListener(SpringApplication app, String[] args) {
        super(app, args);
    }

    @Override
    public void started() {
        LoggerContext loggerContext = ContextSelectorStaticBinder.getSingleton().getContextSelector().getLoggerContext();

        loggerContext.stop();
        loggerContext.reset();

        String appName = System.getProperty(CrigttProperties.APP_NAME_NAME), consoleTty = System.getProperty(CrigttProperties.LOGGING_CONSOLE_TTY_NAME);
        CrigttLoggingInitializer loggingInit =
            buildComponent(CrigttLoggingInitializer.class, () -> new DefaultLoggingInitializer(loggerContext, appName), loggerContext, appName);

        loggerContext.putProperty(CrigttProperties.APP_NAME_NAME, appName);
        loggerContext
            .putProperty(CrigttProperties.LOGGING_CONSOLE_TTY_NAME, ((consoleTty != null) ? consoleTty : Boolean.toString((System.console() != null))));
        loggerContext.putProperty(CrigttProperties.LOGGING_FILE_DIR_NAME, loggingInit.buildLogDirectory().getPath());
        loggerContext.putProperty(CrigttProperties.LOGGING_FILE_NAME_NAME, loggingInit.buildLogFileName());

        String configFileUrlPath = LOGBACK_CONFIG_FILE_URL_PATH_PREFIX + appName + FilenameUtils.EXTENSION_SEPARATOR + CrigttFileExtensions.GROOVY;
        URL configFileUrl;

        try {
            GafferConfigurator configurator = new GafferConfigurator(loggerContext);

            loggerContext.putObject(ClassicConstants.GAFFER_CONFIGURATOR_FQCN, configurator);

            configurator.run(IOUtils.toString((configFileUrl = ResourceUtils.getURL(configFileUrlPath))));
        } catch (IOException e) {
            throw new ApplicationContextException(String.format("Unable to process Logback configuration file (path=%s).", configFileUrlPath), e);
        }

        StatusManager statusManager = loggerContext.getStatusManager();
        StatusUtil statusUtil = new StatusUtil(statusManager);
        long lastResetTime = statusUtil.timeOfLastReset();

        if (statusUtil.getHighestLevel(lastResetTime) >= Status.WARN) {
            StatusPrinter.print(statusManager, lastResetTime);

            throw new ApplicationContextException(String.format("Unable to initialize Logback using configuration file (path=%s).", configFileUrlPath));
        }

        loggingInit.postProcessContext(loggerContext);

        loggerContext.getLogger(LoggingApplicationRunListener.class).info(
            String.format("Logging initialized (initializerClass=%s, configFileUrl=%s).", loggingInit.getClass().getName(), configFileUrl.toString()));
    }
}
