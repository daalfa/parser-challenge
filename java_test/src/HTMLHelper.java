import au.com.bytecode.opencsv.CSVReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class to aggregate methods related to HTML
 */
public class HTMLHelper extends BaseHelper {

    public HTMLHelper(String idColumn, String emptyCell) {
        super(idColumn, emptyCell);
    }

    public Map<String, Map<String, String>> parseHTML(String htmlFile, String tableName) {
        Document doc = null;
        try {
            InputStream is = Files.newInputStream(Paths.get(htmlFile));
            doc = Jsoup.parse(is, "UTF-8", "");
        } catch (IOException e) {
            // TODO: customize exception message
            throw new RuntimeException(e);
        }
        Map<String, Map<String, String>> idMapOfColumnMap = new HashMap<>();

        Element table = doc.select(String.format("table#%s", tableName)).first();
        Iterator<Element> rowItr = table.select("tr").iterator();

        this.columnNamesList = rowItr.next().select("th").stream()
                .map(x -> x.text())
                .collect(Collectors.toList());


        while (rowItr.hasNext()) {
            List<String> row = rowItr.next().select("td").stream()
                    .map(x -> x.text())
                    .collect(Collectors.toList());

            idMapOfColumnMap.put(
                    row.get(columnNamesList.indexOf(this.idColumn)),
                    parseLine(row, columnNamesList));
        }

        return idMapOfColumnMap;
    }


}
