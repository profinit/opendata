package eu.profinit.opendata.handler.mfcr;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import eu.profinit.opendata.business.GenericDataSourceHandler;
import eu.profinit.opendata.model.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by dm on 11/28/15.
 */
@Component
@PropertySource("classpath:mfcr.properties")
public class MFCRHandler extends GenericDataSourceHandler {

    @Autowired
    private JSONClient jsonClient;

    @Value("${mfcr.json.orders.identifier}")
    private String orders_identifier;

    @Override
    protected void checkForNewDataInstance(DataSource ds) {
        switch(ds.getRecordType()) {
            case ORDER: updateOrdersDataInstance(ds); break;
            default: break;
        }
    }

    @Override
    protected void processXLSFile() {

    }

    private void updateOrdersDataInstance(DataSource ds) {
        List<JSONPackageListResource> resourceList = jsonClient.getPackageList(orders_identifier).getResult().getResources();
        for(JSONPackageListResource resource : resourceList) {
            if(resource.getFormat().equals("xls")) {
                //we got 'im
            }
        }
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}