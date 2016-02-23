package eu.profinit.opendata.common;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Duration;

/**
 * Static utility methods.
 */
public class Util {
    /**
     * @param s A string
     * @return True if the parameter is null, an empty string or contains only whitespace characters
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty() || s.matches("^\\s+$");
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

    /**
     * Checks whether an XLS(X) file resides at the specified address. Uses a HEAD request so may not work for all hosts.
     * @param url The URL to check
     * @return True if a publicly accessible Excel spreadsheet can be downloaded at the given address.
     */
    public static boolean isXLSFileAtURL(String url) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con =
                    (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK
                    && (con.getHeaderField("Content-Type").toLowerCase().contains("xls")
                        || con.getHeaderField("Content-Type").toLowerCase().contains("excel")));
        }
        catch (Exception e) {
            return false;
        }
    }
}
