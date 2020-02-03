import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseHelper {

    protected List<String> columnNamesList;
    protected String idColumn;
    protected String emptyCell;

    public BaseHelper(String idColumn, String emptyCell) {
        this.idColumn = idColumn;
        this.emptyCell = emptyCell;
        this.columnNamesList = new ArrayList<>();
    }

    public List<String> getColumnNamesList() {
        return columnNamesList;
    }
    
    protected Map<String, String> parseLine(List<String> row, List<String> columns) {
        Map<String, String> columnMap = new HashMap<>();
        for(int i=0; i<row.size(); i++) {
            if(!columns.get(i).equals(this.idColumn)) {
                columnMap.put(columns.get(i), row.get(i));
            }
        }
        return columnMap;
    }

    protected void DEBUG_PRINT(Map<String, Map<String, String>> map) {
		map.forEach((K1, V1) -> {
			V1.forEach((K2, V2) -> {
				System.out.println(
						String.format("{%s: {%s: %s}}", K1, K2, V2));
			});
		});
	}
}
