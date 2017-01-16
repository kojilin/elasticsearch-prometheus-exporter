package org.compuscene.metrics.prometheus;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;

public class SlowLogAppender extends AbstractAppender {

    private static final String METRIC_NAME = "indices_slowlog_count";
    private final String node;
    private final PrometheusMetricsCatalog catalog;

    SlowLogAppender(String node,
                    PrometheusMetricsCatalog catalog) {
        super("SlowLogAppender", null, null);
        this.node = node;
        this.catalog = catalog;
    }

    @Override
    public void append(LogEvent logEvent) {
        catalog.incCounter("indices_slowlog_count", 1, node, logEvent.getLoggerName(),
                           logEvent.getLevel().name());
    }

    @Override
    public void start() {
        super.start();
        catalog.registerCounter(METRIC_NAME, "Count of slowlog", "node", "logger_name", "level");

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        addAppenderToLoggerConfig(config, "index.search.slowlog.query");
        addAppenderToLoggerConfig(config, "index.search.slowlog.fetch");
        addAppenderToLoggerConfig(config, "index.indexing.slowlog.index");
        context.updateLoggers(config);
    }

    private void addAppenderToLoggerConfig(Configuration configuration, String loggerName) {
        configuration.getLoggerConfig(loggerName).addAppender(this, null, null);
        catalog.setCounter(METRIC_NAME, 0, node, loggerName, Level.WARN.toString());
        catalog.setCounter(METRIC_NAME, 0, node, loggerName, Level.INFO.toString());
        catalog.setCounter(METRIC_NAME, 0, node, loggerName, Level.DEBUG.toString());
        catalog.setCounter(METRIC_NAME, 0, node, loggerName, Level.TRACE.toString());
    }
}
