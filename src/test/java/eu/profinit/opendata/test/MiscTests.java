package eu.profinit.opendata.test;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.query.PartnerQueryService;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.HashMap;

/**
 * Created by dm on 2/15/16.
 */
public class MiscTests extends TestCase {

    @Test
    public void testNormalizeEntityNames() {
        PartnerQueryService partnerQueryService = new PartnerQueryService();
        HashMap<String, String> expectations = new HashMap<>();

        expectations.put("123 Jan Novák ", "JAN NOVÁK");
        expectations.put("GTS Czech s.r.o.", "GTS CZECH, S. R. O.");
        expectations.put("JUNIOR centrum, a.s. - \"v likvidaci\"", "JUNIOR CENTRUM, A. S.");
        expectations.put("Kaiser+Kraft,spol. s r.o.", "KAISER+KRAFT, S. R. O.");

        for(String key : expectations.keySet()) {
            String result = partnerQueryService.normalizeEntityName(key);
            assertEquals(expectations.get(key), result);
        }
    }

    @Test
    public void testCheckForXLSFileAtURL() {
        boolean res = Util.isXLSFileAtURL("http://nothing.here.com");
        assertFalse(res);
        res = Util.isXLSFileAtURL("http://data.justice.cz/Faktury%20Ministerstva%20spravedlnosti/Faktury%20MSp%202013.xls");
        assertTrue(res);
        res = Util.isXLSFileAtURL("http://google.com");
        assertFalse(res);

    }
}
