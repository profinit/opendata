package eu.profinit.opendata.common;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.sql.Timestamp;
import java.time.Duration;

/**
 * Static utility methods.
 */
public class Util {
    /**
     * @param s A string
     * @return True if the parameter is null or an empty string
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Tests if a workbook row is empty.
     * @param row An Excel woorkbook row
     * @return True if all cells in all columns of the given row are blank.
     */
    public static boolean isRowEmpty(Row row) {
        if(row == null) return true;

        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK)
                return false;
        }
        return true;
    }

    /**
     * Checks if at least half of a given duration has already elapsed since a fixed point in time.
     * @param from A fixed point in the past
     * @param targetDuration A duration
     * @return True if at least half of <code>targetDuration</code> has elapsed since <code>from</code>.
     */
    public static boolean hasEnoughTimeElapsed(Timestamp from, Duration targetDuration) {
        Duration elapsed = Duration.ofMillis(System.currentTimeMillis())
                .minus(Duration.ofMillis(from.getTime()));

        return elapsed.compareTo(targetDuration.dividedBy(2)) > 0;
    }
}
