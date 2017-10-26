package eu.profinit.opendata.institution.mfcr.impl;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.institution.mfcr.PartnerListProcessor;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.Entity;
import eu.profinit.opendata.query.PartnerQueryService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dm on 2/12/16.
 */
@Component
public class PartnerListProcessorImpl implements PartnerListProcessor {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PartnerQueryService partnerQueryService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,
            rollbackFor = {IOException.class, RuntimeException.class})
    public void processListOfPartners(DataSource ds, InputStream inputStream) throws IOException {
        try(Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for(int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if(Util.isRowEmpty(row)) continue;
    
                String partnerCode = row.getCell(0).getStringCellValue();
                String ico = row.getCell(1).getStringCellValue();
                String name = row.getCell(2).getStringCellValue();
    
                Entity partner = partnerQueryService.findOrCreateEntity(name, ico, null);
                partnerQueryService.findOrCreatePartnerListEntry(ds.getEntity(), partner, partnerCode);
            }
        }
    }

    @Override
    public void setEm(EntityManager em) {
        this.em = em;
    }
}
