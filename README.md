CRTableSearcher
===============

## Crystal Reports Table Search tool

This is a Swing UI tool for searching table names in selected list of Crystal Reports.
This tool uses Crystal Reports Java Reporting Component [JRC](http://www.businessobjects.com/campaigns/forms/downloads/crystal/eclipse/datasave.asp) for parsing the reports.

### How to use
* Install Java 1.7+
* Navigate to dist folder
* java -jar CRTableSearcher.jar will launch the UI
* Select the folder where the reports are.  This will search for all folders recursively.
* Select the table pattern
* Multiple table names are separated by ,
		
		Wildcard * is used for character globbign in table name
		A sample pattern PAT*, HSP*  will search for all table name that start with PAT or HSP
		If report finds any of the table in the pattern the search stops and continue for the next report.
* Run the Search
* It will display the matched reports
