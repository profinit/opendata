package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.jaxb.Mapping;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;

import javax.persistence.EntityManager;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dm on 1/16/16.
 */
public interface WorkbookProcessor {
    void processWorkbook(Workbook workbook, Mapping mapping, Retrieval retrieval, Logger log) throws TransformException;

    void checkRecordIntegrity(Record record) throws TransformException;

    void setEm(EntityManager em);
    EntityManager getEm();
}
