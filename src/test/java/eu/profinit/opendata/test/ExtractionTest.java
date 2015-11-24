package eu.profinit.opendata.test;

import eu.profinit.opendata.business.DataSourceHandlerFactory;
import eu.profinit.opendata.business.ExtractionService;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.DataSourceHandler;
import eu.profinit.opendata.model.Entity;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static eu.profinit.opendata.test.DataGenerator.*;
import static org.mockito.Mockito.*;

public class ExtractionTest {

    @Test
    public void testInvokeHandlingClass() throws Exception {
        ExtractionService extractionService = new ExtractionService();

        List<DataSource> dataSourceList = new ArrayList<>();
        Entity entity = getTestMinistry();
        DataSource dataSource = getDataSource(entity);
        dataSource.setHandlingClass(MockHandler.class);
        dataSourceList.add(dataSource);

        EntityManager em = mock(EntityManager.class);
        extractionService.setEm(em);
        TypedQuery<DataSource> queryMock = (TypedQuery<DataSource>) mock(TypedQuery.class);
        when(em.createNamedQuery("findActiveDataSources", DataSource.class)).thenReturn(queryMock);
        when(queryMock.getResultList()).thenReturn(dataSourceList);

        DataSourceHandlerFactory mockFactory = mock(DataSourceHandlerFactory.class);
        extractionService.setDataSourceHandlerFactory(mockFactory);
        MockHandler mockHandler = mock(MockHandler.class);
        when(mockFactory.getHandlerFromClass(MockHandler.class)).thenReturn(mockHandler);

        extractionService.runExtraction();
        verify(mockHandler).processDataSource(dataSource);
    }

    class MockHandler implements DataSourceHandler {

        @Override
        public void processDataSource(DataSource ds) {

        }
    }
}