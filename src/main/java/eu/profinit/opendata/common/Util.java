package eu.profinit.opendata.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    
    private static Logger log = LogManager.getLogger(Util.class);
    
    private Util() {}
    
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
            URL parsedUrl = new URL(url);
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con =
                    (HttpURLConnection) parsedUrl.openConnection();
            con.setRequestMethod("HEAD");
            
            int responseCode = con.getResponseCode();
            String contentType = con.getHeaderField("Content-Type");
            
            String protocol = parsedUrl.getProtocol();
            if(responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String protocolWarning = protocol.equals("http") ? " You are using http protocol. Try to use https instead!" : "";
                log.warn("Url {} moved to another location!{}", url, protocolWarning);
            }
            
            return (responseCode == HttpURLConnection.HTTP_OK
                    && (contentType.toLowerCase().contains("xls")
                        || contentType.toLowerCase().contains("excel")));
        }
        catch (Exception e) {
            log.error("Could not verify isXLSFileAtURL", e);
            return false;
        }
    }
}
