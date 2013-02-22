package org.hhs.sccgov.log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.hhs.sccgov.CRTableSearcher;

/**
 * This is a custom Log Handler that pushes all the log messages to Main UI form for displaying in the UI.
 *
 * @author Kesav.Kolla
 */
public class LogHandler extends Handler {

	private final CRTableSearcher cRTableSearcher;

	public LogHandler(final CRTableSearcher cRTableSearcher) {
		this.cRTableSearcher = cRTableSearcher;
		setLevel(Level.ALL);
		setFormatter(new LogFormatter());

	}

	@Override
	public void publish(final LogRecord record) {
		try {
			final String msg = getFormatter().format(record);
			cRTableSearcher.publishLog(msg, record.getLevel());
		} catch (final Throwable th) {
			th.printStackTrace();
		}
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}
}
