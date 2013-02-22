package org.hhs.sccgov;

import ca.odell.glazedlists.EventList;

import com.crystaldecisions.reports.sdk.ReportClientDocument;
import com.crystaldecisions.sdk.occa.report.data.ITable;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class does the crystal report table search. This is an implementation of Callable so executor service can call
 * this task in parallel.
 *
 * @author Kesav.Kolla
 */
public class TableSearcherTask implements Callable<Void> {

	private final File rptFile;
	private final Pattern tablePattern;
	private final EventList<File> reportsList;
	private final CountDownLatch reportStateLatch;
	public static final String __CLASSNAME__ = TableSearcherTask.class.getName();
	private static final Logger LOGGER = Logger.getLogger(__CLASSNAME__);

	/**
	 * Creates new instance of this task object
	 *
	 * @param rptFile Crystal Report file that needs to be checked
	 * @param tablePattern Table search pattern.
	 * @param reportsList List model object. This will be updated if a table found in the crystal report.
	 * @param reportStateLatch countdown latch to notify once the task is finished.
	 */
	public TableSearcherTask(final File rptFile, final Pattern tablePattern,
			final EventList<File> reportsList, final CountDownLatch reportStateLatch) {
		this.rptFile = rptFile;
		this.tablePattern = tablePattern;
		this.reportsList = reportsList;
		this.reportStateLatch = reportStateLatch;
		LOGGER.setLevel(Level.ALL);
	}

	/**
	 * This method will be called from the executor service. This method opens the given crystal report and searches for
	 * the matching table name.
	 *
	 * @return Nothing to return.
	 */
	@Override
	public Void call() {
		LOGGER.entering(__CLASSNAME__, "call");
		final ReportClientDocument crystalReport = new ReportClientDocument();
		try {
			LOGGER.log(Level.INFO, "Opening file: {0}", rptFile.getAbsolutePath());
			crystalReport.open(rptFile.getAbsolutePath(), 0);
			if (checkForTable(crystalReport)) {
				reportsList.getReadWriteLock().writeLock().lock();
				try {
					reportsList.add(rptFile);
				} finally {
					reportsList.getReadWriteLock().writeLock().unlock();
				}
			} else {
				LOGGER.log(Level.INFO, "{0} doesn't have matched table", rptFile.getAbsolutePath());
			}
		} catch (final Throwable th) {
			LOGGER.log(Level.SEVERE, "Error in processing crystal report", th);
		} finally {
			try {
				if (crystalReport.isOpen()) {
					crystalReport.close();
				}
			} catch (final Throwable th) {
				LOGGER.log(Level.SEVERE, null, th);
			}
		}
		//Task is completed clean up the memory
		reportStateLatch.countDown();
		System.gc();
		try {
			Thread.sleep(400);
		} catch (InterruptedException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
		}
		LOGGER.exiting(__CLASSNAME__, "call");
		return null;
	}

	/**
	 * Get the database object from the crystal report and look for all the tables it has and match with the pattern
	 * that provided.
	 *
	 * @param crystalReport
	 * @return true if table match found false otherwise
	 * @throws Exception
	 */
	private boolean checkForTable(final ReportClientDocument crystalReport) throws Exception {
		LOGGER.entering(__CLASSNAME__, "checkForTable");

		//Loop through all the tables in the report and check for matching pattern.
		for (final ITable table : crystalReport.getDatabaseController().getDatabase().getTables()) {
			final Matcher matcher = tablePattern.matcher(table.getName());
			if (matcher.matches()) {
				LOGGER.log(Level.INFO, "Table name {0} matches with pattern", table.getName());
				LOGGER.exiting(__CLASSNAME__, "checkForTable", true);
				return true;
			}
		}
		for (final String subreport : crystalReport.getSubreportController().getSubreportNames()) {
			if (checkForSubReports(subreport, crystalReport)) {
				LOGGER.exiting(__CLASSNAME__, "checkForTable", true);
				return true;
			}
		}
		LOGGER.exiting(__CLASSNAME__, "checkForTable", false);
		return false;
	}

	/**
	 * Check for tables in the subreport for matching
	 *
	 * @param subreport Name of the subreport
	 * @param crystalReport crystal report document
	 * @return true if found match false otherwise
	 * @throws Exception
	 */
	private boolean checkForSubReports(final String subreport, final ReportClientDocument crystalReport) throws Exception {
		LOGGER.entering(__CLASSNAME__, "checkForSubReports");
		for (final ITable table : crystalReport.getSubreportController().getSubreport(subreport).
				getDatabaseController().getDatabase().getTables()) {
			final Matcher matcher = tablePattern.matcher(table.getName());
			if (matcher.matches()) {
				LOGGER.log(Level.INFO, "Table name {0} matches with pattern in the subreport {1}",
						new Object[]{table.getName(), subreport});
				LOGGER.exiting(__CLASSNAME__, "checkForSubReports", true);
				return true;
			}
		}
		LOGGER.exiting(__CLASSNAME__, "checkForSubReports", false);
		return false;
	}
}
