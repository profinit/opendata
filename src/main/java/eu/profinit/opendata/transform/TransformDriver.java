package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.jaxb.Mapping;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dm on 12/22/15.
 */
public interface TransformDriver {
    Retrieval doRetrieval(DataInstance dataInstance, String mappingFile);

    Retrieval doRetrieval(DataInstance dataInstance, String mappingFile, InputStream inputStream);

    @Transactional(propagation = Propagation.NESTED,
                   rollbackFor = {TransformException.class, RuntimeException.class})
    void processWorkbook(Workbook workbook, Mapping mapping, Retrieval retrieval) throws TransformException;

    void checkRecordIntegrity(Record record) throws TransformException;

    Workbook openXLSFile(InputStream inputStream, DataInstance dataInstance) throws IOException;

    Mapping loadMapping(String mappingFile) throws JAXBException, IOException;

    void setEm(EntityManager em);
    EntityManager getEm();
}
