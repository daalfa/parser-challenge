import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
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

		recordMerger.parseCSV("data/second.csv");
		recordMerger.parseCSV("data/third.csv");

		recordMerger.generateCSV(FILENAME_COMBINED);
	}




	private static final String ID_COLUMN = "ID";
	private static final String EMPTY_CELL = "";

	private List<String> inputFiles;
	private List<String> htmlFiles;
	private List<String> csvFiles;

	private List<String> uniqueColumnList;
	private Map<String, Map<String, String>> idsMapOfColumnsMap;

	public RecordMerger(List<String> inputFiles) {
		this.inputFiles = inputFiles;
		this.htmlFiles = filterFilesByExtension(inputFiles, ".html");
		this.csvFiles = filterFilesByExtension(inputFiles, ".csv");
		this.uniqueColumnList = new ArrayList<>();
		this.idsMapOfColumnsMap = new HashMap<>();
	}

	private List<String> filterFilesByExtension(List<String> inputFiles, String extension) {
		return inputFiles.stream()
				.filter(ext -> ext.toLowerCase().endsWith(extension.toLowerCase()))
				.collect(Collectors.toList());
	}

	public void parseCSV(String csvFile) throws IOException {
		try (
				Reader reader = Files.newBufferedReader(Paths.get(csvFile));
				CSVReader csvReader = new CSVReader(reader);
		) {

			String[] columnNamesArray = csvReader.readNext();

			List<String> columns = Arrays.asList(columnNamesArray);
			this.updateUniqueColumns(columns);
			int indexOfId = columns.indexOf(this.ID_COLUMN);

			String[] line;
			while ((line = csvReader.readNext()) != null) {
				idsMapOfColumnsMap.put(line[indexOfId], parseLine(line, columnNamesArray));
			}
		}
	}

	public void generateCSV(String outputName) throws IOException {
		try (
				Writer writer = Files.newBufferedWriter(Paths.get(outputName));

				CSVWriter csvWriter = new CSVWriter(writer,
						CSVWriter.DEFAULT_SEPARATOR,
						CSVWriter.DEFAULT_QUOTE_CHARACTER,
						CSVWriter.NO_ESCAPE_CHARACTER,
						CSVWriter.DEFAULT_LINE_END);
		) {
			csvWriter.writeNext(this.uniqueColumnList.toArray(new String[uniqueColumnList.size()]));

			Map sortedIdMap = sortMapById(idsMapOfColumnsMap);

			DEBUG_PRINT(sortedIdMap);

			sortedIdMap.forEach((K, V) -> {
				List<String> newLine = generateLine(this.uniqueColumnList, (Map<String, String>)V, (String)K);
				csvWriter.writeNext(newLine.toArray(new String[newLine.size()]));
			});
		}
	}

	private Map<String, Map<String, String>> sortMapById(Map<String, Map<String, String>> idMap) {
		return idMap.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(
						Map.Entry::getKey, Map.Entry::getValue,	(oldValue, newValue) -> oldValue, LinkedHashMap::new));
	}

	private List<String> generateLine(List<String> uniqueColumnsList, Map<String, String> columnMap, String id) {
		List<String> newLine = new ArrayList<>();
		for(String uniqueColumn : uniqueColumnsList) {
			if(uniqueColumn.equals(ID_COLUMN)) {
				newLine.add(id);
			} else if(columnMap.containsKey(uniqueColumn)) {
				newLine.add(columnMap.get(uniqueColumn));
			} else {
				newLine.add(EMPTY_CELL);
			}
		}
		return newLine;
	}

	private Map<String, String> parseLine(String[] line, String[] columns) {
		Map<String, String> column_map = new HashMap<>();
		for(int i=0; i<line.length; i++) {
			if(!columns[i].equals(ID_COLUMN)) {
				column_map.put(columns[i], line[i]);
			}
		}
		return column_map;
	}

	private void updateUniqueColumns(List<String> newColumns) {
		this.uniqueColumnList = Stream
				.concat(this.uniqueColumnList.stream(), newColumns.stream())
				.distinct()
				.collect(Collectors.toList());
	}

	private void DEBUG_PRINT(Map<String, Map<String, String>> map) {
		map.forEach((K1, V1) -> {
			V1.forEach((K2, V2) -> {
				System.out.println(
						String.format("{%s: {%s: %s}}", K1, K2, V2));
			});
		});
	}
}
