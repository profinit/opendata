package eu.profinit.opendata.common;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.sql.Timestamp;
import java.time.Duration;

/**
 * Created by dm on 1/31/16.
 */
public class Util {
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
    public static boolean isRowEmpty(Row row) {
        if(row == null) return true;

        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK)
                return false;
        }
        return true;
    }

    public static boolean hasEnoughTimeElapsed(Timestamp from, Duration targetDuration) {
        Duration elapsed = Duration.ofMillis(System.currentTimeMillis())
                .minus(Duration.ofMillis(from.getTime()));

        return elapsed.compareTo(targetDuration.dividedBy(2)) > 0;
    }
}
