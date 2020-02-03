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
		System.out.println("STARTING...");
		System.out.println(Arrays.asList(args));

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

	public RecordMerger(List<String> inputFiles) {
		this.inputFiles = inputFiles;
		this.htmlFiles = filterFilesByExtension(inputFiles, ".html");
		this.csvFiles = filterFilesByExtension(inputFiles, ".csv");
		this.uniqueColumnList = new ArrayList<>();
		this.idMapOfColumnMap = new HashMap<>();

	}

	private List<String> filterFilesByExtension(List<String> inputFiles, String extension) {
		return inputFiles.stream()
				.filter(ext -> ext.toLowerCase().endsWith(extension.toLowerCase()))
				.collect(Collectors.toList());
	}

	public void parseRecords() {
		parseHTML();
		parseCSV();
	}

	public void parseHTML() {
		HTMLHelper htmlHelper = new HTMLHelper(ID_COLUMN, EMPTY_CELL);
		htmlFiles.forEach(filename -> {
			// does not handle duplicate IDs
			idMapOfColumnMap.putAll(htmlHelper.parseHTML(filename, HTML_TABLE_ID));
			updateUniqueColumns(htmlHelper.getColumnNamesList());
		});
	}

	public void parseCSV() {
		CSVHelper csvHelper = new CSVHelper(ID_COLUMN, EMPTY_CELL);
		csvFiles.forEach(filename -> {
			// does not handle duplicate IDs
				idMapOfColumnMap.putAll(csvHelper.parseAllCSV(filename));
				updateUniqueColumns(csvHelper.getColumnNamesList());
			});
	}

	public void generateCSV(String outputName) throws IOException {
		CSVHelper csvHelper = new CSVHelper(ID_COLUMN, EMPTY_CELL);
		csvHelper.generateCSV(
				sortMapById(idMapOfColumnMap),
				uniqueColumnList,
				outputName);
	}

	private Map<String, Map<String, String>> sortMapById(Map<String, Map<String, String>> idMap) {
		return idMap.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(
						Map.Entry::getKey, Map.Entry::getValue,	(oldValue, newValue) -> oldValue, LinkedHashMap::new));
	}

	private void updateUniqueColumns(List<String> newColumns) {
		uniqueColumnList = Stream
				.concat(uniqueColumnList.stream(), newColumns.stream())
				.distinct()
				.collect(Collectors.toList());
	}
}
