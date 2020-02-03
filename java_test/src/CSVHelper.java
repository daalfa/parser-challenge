import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class CSVHelper extends BaseHelper {

    public CSVHelper(String idColumn, String emptyCell) {
        super(idColumn, emptyCell);
    }



    public Map<String, Map<String, String>> parseAllCSV(String csvFile) {
        try (
                Reader reader = Files.newBufferedReader(Paths.get(csvFile));
                CSVReader csvReader = new CSVReader(reader);
        ) {
            Map<String, Map<String, String>> idMapOfColumnMap = new HashMap<>();
            String[] columnNamesArray = csvReader.readNext();

            this.columnNamesList = Arrays.asList(columnNamesArray);

            int indexOfId = columnNamesList.indexOf(this.idColumn);

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                idMapOfColumnMap.put(line[indexOfId], parseLine(line, columnNamesArray));
            }

            return idMapOfColumnMap;
        } catch (IOException e) {
            // TODO: customize exception message
            throw new RuntimeException(e);
        }
    }



    public void generateCSV(Map<String, Map<String, String>> idMapOfColumnMap, List<String> columnNamesList, String outputName) {
        try (
                Writer writer = Files.newBufferedWriter(Paths.get(outputName));

                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.DEFAULT_QUOTE_CHARACTER,
                        CSVWriter.NO_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);
        ) {
            csvWriter.writeNext(columnNamesList.toArray(new String[columnNamesList.size()]));

            idMapOfColumnMap.forEach((K, V) -> {
                List<String> newLine = generateLine(columnNamesList, V, K);
                csvWriter.writeNext(newLine.toArray(new String[newLine.size()]));
            });
        } catch (IOException e) {
            // TODO: customize exception message
            throw new RuntimeException(e);
        }
    }

    private List<String> generateLine(List<String> columnNamesList, Map<String, String> columnMap, String id) {
        List<String> newLine = new ArrayList<>();
        for(String uniqueColumn : columnNamesList) {
            if(uniqueColumn.equals(this.idColumn)) {
                newLine.add(id);
            } else if(columnMap.containsKey(uniqueColumn)) {
                newLine.add(columnMap.get(uniqueColumn));
            } else {
                newLine.add(this.emptyCell);
            }
        }
        return newLine;
    }
}
