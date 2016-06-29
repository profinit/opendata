package eu.profinit.opendata.institution.mfcr;

import eu.profinit.opendata.model.DataSource;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;

/**
 * A component that reads the MF list of partners and saves partner Entities into the database. This
 * is necessary because older invoice files (up to 2014, inclusive) don't contain the ICO of partner
 * entities, only a reference code to the partner file.
 * @see <a href="http://data.mfcr.cz/cs/dataset/prehled-faktur-ministerstva-financi-cr">Faktury MFČR</a>
 */
public interface PartnerListProcessor {
    /**
     * Reads the file containing the list of partners and creates corresponding Entities. Uses a process similar to
     * the main extraction procedure in WorkbookProcessor and <em>always creates a new database transaction.</em>
     * @param ds The MF invoices DataSource.
     * @param inputStream Downloaded list of partners from MFCR, in XLSX format.
     * @throws IOException There was an error opening the workbook
     * @see eu.profinit.opendata.transform.WorkbookProcessor
     */
    void processListOfPartners(DataSource ds, InputStream inputStream) throws IOException;

    /**
     * Used for mocking.
     * @param em
     */
    void setEm(EntityManager em);
}
