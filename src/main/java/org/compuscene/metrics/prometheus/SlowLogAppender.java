package org.compuscene.metrics.prometheus;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.logging.log4j.Log4jESLogger;

public class SlowLogAppender extends AppenderSkeleton {

    private static final String METRIC_NAME = "indices_slowlog_count";
    private final String node;
    private final PrometheusMetricsCatalog catalog;

    public static void initialize(String node, PrometheusMetricsCatalog catalog) {
        catalog.registerCounter(METRIC_NAME, "Count of slowlog", "node", "logger_name",
                                "logger_level");

        SlowLogAppender slowLogAppender = new SlowLogAppender(node, catalog);
        addAppenderToLogger("index.indexing.slowlog.index", catalog, node, slowLogAppender);
        addAppenderToLogger("index.search.slowlog.query", catalog, node, slowLogAppender);
        addAppenderToLogger("index.search.slowlog.fetch", catalog, node, slowLogAppender);
    }

    private static void addAppenderToLogger(String loggerName, PrometheusMetricsCatalog catalog,
                                            String node, SlowLogAppender slowLogAppender) {
        ((Log4jESLogger) ESLoggerFactory.getLogger(loggerName)).logger().addAppender(slowLogAppender);
        catalog.setCounter(METRIC_NAME, 0, node, loggerName, Level.WARN.toString());
        catalog.setCounter(METRIC_NAME, 0, node, loggerName, Level.INFO.toString());
        catalog.setCounter(METRIC_NAME, 0, node, loggerName, Level.DEBUG.toString());
        catalog.setCounter(METRIC_NAME, 0, node, loggerName, Level.TRACE.toString());
    }

    private SlowLogAppender(String node,
                            PrometheusMetricsCatalog catalog) {
        this.node = node;
        this.catalog = catalog;
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        catalog.incCounter(METRIC_NAME, 1, node, loggingEvent.getLoggerName(),
                           loggingEvent.getLevel().toString());
    }


    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

}
