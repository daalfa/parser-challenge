import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecordMerger {

	public static final String FILENAME_COMBINED = "combined.csv";

	/**
	 * Entry point of this test.
	 *
	 * @param args command line arguments: first.html and second.csv.
	 * @throws Exception bad things had happened.
	 */
	public static void main(final String[] args) throws Exception {

		if (args.length == 0) {
			System.err.println("Usage: java RecordMerger file1 [ file2 [...] ]");
			System.exit(1);
		}

		// your code starts here.
		RecordMerger recordMerger = new RecordMerger(Arrays.asList(args));
		recordMerger.parseRecords();
		recordMerger.generateCSV(FILENAME_COMBINED);
	}

	private static final String ID_COLUMN = "ID";
	private static final String EMPTY_CELL = "";
	private static final String HTML_TABLE_ID = "directory";

	private List<String> inputFiles;
	private List<String> htmlFiles;
	private List<String> csvFiles;

	private List<String> uniqueColumnList;
	private Map<String, Map<String, String>> idMapOfColumnMap;

	/**
	 * Main constructor. Initializes the list of files with different extensions
	 * Processing methods must be called manually.
	 * Step 1 - RecordMerger.parseRecords()
	 * Step 2 - RecordMerger.generateCSV(...)
	 * RecordMerger will accumulate unique columns each new parse operation and output all columns on generateCSV()
	 */
	public RecordMerger(List<String> inputFiles) {
		this.inputFiles = inputFiles;
		this.htmlFiles = filterFilesByExtension(inputFiles, ".html");
		this.csvFiles = filterFilesByExtension(inputFiles, ".csv");
		this.uniqueColumnList = new ArrayList<>();
		this.idMapOfColumnMap = new HashMap<>();
	}

	/**
	 * Simple stream filter to return a list of files with same extension.
	 * @param inputFiles list of input files with different extensions
	 * @param extension case insensitive extension example ".csv" or ".HTML"
	 * @return filtered list of input files with same extension.
	 */
	private List<String> filterFilesByExtension(List<String> inputFiles, String extension) {
		return inputFiles.stream()
				.filter(ext -> ext.toLowerCase().endsWith(extension.toLowerCase()))
				.collect(Collectors.toList());
	}

	public void parseRecords() {
		parseHTML();
		parseCSV();
	}

	/**
	 * Creates HTMLHelper to handle HTML file parser. HTMLHelper is stateless except for getColumnNamesList() method.
	 * Each new parse will reset getColumnNamesList() with new values.
	 * Collect getColumnNamesList() every execution is important to accumulate all different columns from different sources.
	 * HTMLHelper.parseHTML(...) returns a Map of IDs and a Map of Columns, considering each ID unique.
	 * A Map of Columns and Values for each ID. Essentially each row has its own map of column_name:column_value.
	 *
	 * The Strategy is to generate a expandable row that can be appended with more columns and values as it go.
	 * The consolidate will add new IDs in the Map, if this ID already exists, then it merges the columns.
	 */
	public void parseHTML() {
		HTMLHelper htmlHelper = new HTMLHelper(ID_COLUMN, EMPTY_CELL);
		htmlFiles.forEach(filename -> {
			// does not handle duplicate IDs
			idMapOfColumnMap = consolidate(
					idMapOfColumnMap,
					htmlHelper.parseHTML(filename, HTML_TABLE_ID));
			updateUniqueColumns(htmlHelper.getColumnNamesList());
		});
	}

	/**
	 * Creates CSVHelper to handle CSV file parser. CSVHelper is stateless except for getColumnNamesList() method.
	 * Each new parse will reset getColumnNamesList() with new values.
	 * Collect getColumnNamesList() every execution is important to accumulate all different columns from different sources.
	 * CSVHelper.parseAllCSV(...) returns a Map of IDs and a Map of Columns, considering each ID unique.
	 * A Map of Columns and Values for each ID. Essentially each row has its own map of column_name:column_value.
	 *
	 * The Strategy is to generate a expandable row that can be appended with more columns and values as it go.
	 * The consolidate will add new IDs in the Map, if this ID already exists, then it merges the columns.
	 */
	public void parseCSV() {
		CSVHelper csvHelper = new CSVHelper(ID_COLUMN, EMPTY_CELL);
		csvFiles.forEach(filename -> {
			idMapOfColumnMap = consolidate(
					idMapOfColumnMap,
					csvHelper.parseAllCSV(filename));
			updateUniqueColumns(csvHelper.getColumnNamesList());
		});
	}

	/**
	 * Uses the same CSVHelper to output to a File.
	 * It reads uniqueColumnList to generate a column set found in all previously parsed files.
	 * Sorts the ID Map by ascending order. For each ID, it matches the Columns with uniqueColumnList.
	 * If no value for a column is found then uses a default value EMPTY_CELL.
	 * @param outputName output file name.
	 * @throws IOException
	 */
	public void generateCSV(String outputName) throws IOException {
		CSVHelper csvHelper = new CSVHelper(ID_COLUMN, EMPTY_CELL);
		csvHelper.generateCSV(
				sortMapById(idMapOfColumnMap),
				uniqueColumnList,
				outputName);
	}

	/**
	 * Java stream operations to sort a HashMap and generates a LinkedHashMap.
	 */
	private Map<String, Map<String, String>> sortMapById(Map<String, Map<String, String>> idMap) {
		return idMap.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(
						Map.Entry::getKey, Map.Entry::getValue,	(oldValue, newValue) -> oldValue, LinkedHashMap::new));
	}

	/**
	 * Java stream operations to concatenate and generate a list of unique values.
	 * @param newColumns
	 */
	private void updateUniqueColumns(List<String> newColumns) {
		uniqueColumnList = Stream
				.concat(uniqueColumnList.stream(), newColumns.stream())
				.distinct()
				.collect(Collectors.toList());
	}

	/**
	 * Consolidates the sub Map of a and b. Since a is a Map(ID, Map(Column, Value)) we need to consolidate the values only.
	 * Consolidate means if Map a has same ID than Map B and Map B has different Columns than Map a. Map a will get the new columns.
	 * @param a Original Map of IDs, will receive Map B if new info is available
	 * @param b Map of new IDs
	 * @return Consolidated  Map(Column, Value) for each matching ID between A and B
	 */
	private Map<String, Map<String, String>> consolidate(Map<String, Map<String, String>> a, Map<String, Map<String, String>> b) {
		a.forEach((aK, aV) -> {
			if(b.containsKey(aK)) {
				aV.putAll(b.get(aK));
			}
		});
		b.forEach((bK, bV) -> {
			if(!a.containsKey(bK)) {
				a.put(bK, bV);
			}
		});
		return a;
	}
}
