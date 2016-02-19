package eu.profinit.opendata.institution.justice;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.control.GenericDataSourceHandler;
import eu.profinit.opendata.institution.mfcr.MFCRHandler;
import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.Periodicity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Commit;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;

/**
 * The handler for MSp. Only generates data instances for invoices according to a pre-determined URL scheme. Otherwise,
 * behavior is the same as for any other generic handler.
 */
@Component
public class JusticeHandler extends GenericDataSourceHandler {

    @Value("${justice.invoices.url.scheme}")
    private String url_scheme;

    @Value("${justice.invoices.mapping.file}")
    private String mapping_file;

    private Logger log = LogManager.getLogger(JusticeHandler.class);

    @Override
    public void updateDataInstances(DataSource ds) {
        switch(ds.getRecordType()) {
            case INVOICE: updateInvoicesDataInstance(ds); break;
            case CONTRACT: break;
            default: break;
        }
    }

    /**
     * Tries to create invoice data instances for all years from the present going back to 2009. If the data instances
     * are already present but aren't at least a year old, does nothing. If they are older and have already been
     * processed, they are expired.
     * @param ds The MSp invoice data source
     */
    private void updateInvoicesDataInstance(DataSource ds) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for(Integer i = currentYear; i >= 2009; i--) {
            String url = url_scheme.replace("{year}", i.toString());

            Optional<DataInstance> oldDataInstance = ds.getDataInstances().stream().filter(d -> d.getUrl().equals(url))
                    .findAny();

            if(oldDataInstance.isPresent()) {
                // Expire processed data instances where at least two years have elapsed since the file's year
                if(currentYear - i > 1 && oldDataInstance.get().getLastProcessedDate() != null) {
                    oldDataInstance.get().expire();
                    em.merge(oldDataInstance.get());
                    log.info("Expired MSp invoices data instance for year " + i.toString());
                }
            }
            else if(Util.isXLSFileAtURL(url)) {
                DataInstance di = new DataInstance();
                di.setDataSource(ds);
                ds.getDataInstances().add(di);

                di.setFormat("xls");
                di.setPeriodicity(Periodicity.QUARTERLY);
                di.setUrl(url);
                di.setDescription("Faktury MSp za rok " + i.toString());
                di.setMappingFile(mapping_file);
                di.setIncremental(false);

                log.debug("Adding new data instance for MSp invoices in " + i.toString());
                em.persist(di);
            }
            else {
                log.warn("Can't find an XLS document at the url " + url);
            }
        }
    }
}
