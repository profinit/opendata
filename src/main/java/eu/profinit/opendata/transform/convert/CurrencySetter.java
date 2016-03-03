package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Sets the currency property. Expects a string cell with argumentName "inputCurrencyCode". Ignores the fieldName
 * attribute. Checks that the currency is a valid three-letter ISO4217 code.
 */
@Component
public class CurrencySetter implements RecordPropertyConverter{
    // Currencies shouldn't be removed, even if a currency disappears, because of historical records
    private static final List<String> iso4217Currencies = Arrays.asList(
            "SHP", "TTD", "UYI", "KGS", "DJF", "BTN", "XBA", "HTG", "BBD", "XAU",
            "FKP", "MWK", "PGK", "XCD", "COU", "RWF", "NGN", "BSD", "XTS", "TMT",
            "GEL", "VUV", "FJD", "MVR", "AZN", "MNT", "MGA", "WST", "KMF", "GNF",
            "SBD", "BDT", "MMK", "TJS", "CVE", "MDL", "KES", "SRD", "LRD", "MUR",
            "CDF", "BMD", "USN", "CUP", "USS", "GMD", "UZS", "CUC", "ZMK", "NPR",
            "NAD", "LAK", "SZL", "XDR", "BND", "TZS", "MXV", "LSL", "KYD", "LKR",
            "ANG", "PKR", "SLL", "SCR", "GHS", "ERN", "BOV", "GIP", "IRR", "XPT",
            "BWP", "XFU", "CLF", "ETB", "STD", "XXX", "XPD", "AMD", "XPF", "JMD",
            "MRO", "BIF", "CHW", "ZWL", "AWG", "MZN", "CHE", "XOF", "KZT", "BZD",
            "XAG", "KHR", "XAF", "GYD", "AFN", "SOS", "TOP", "AOA", "KPW", "JPY",
            "CNY", "SDG", "RON", "MKD", "MXN", "CAD", "XBB", "XBC", "XBD", "UGX",
            "ZAR", "AUD", "NOK", "ILS", "ISK", "SYP", "LYD", "UYU", "YER", "CSD",
            "EEK", "THB", "IDR", "LBP", "AED", "BOB", "QAR", "BHD", "HNL", "HRK",
            "COP", "ALL", "DKK", "MYR", "SEK", "RSD", "BGN", "DOP", "KRW", "LVL",
            "VEF", "CZK", "TND", "KWD", "VND", "JOD", "NZD", "PAB", "CLP", "PEN",
            "GBP", "DZD", "CHF", "RUB", "UAH", "ARS", "SAR", "EGP", "INR", "PYG",
            "TWD", "TRY", "BAM", "OMR", "SGD", "MAD", "BYR", "NIO", "HKD", "LTL",
            "SKK", "GTQ", "BRL", "EUR", "HUF", "IQD", "CRC", "PHP", "SVC", "PLN",
            "USD", "MOP", "XEU");

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger) throws TransformException {
        String sourceCurrencyString = sourceValues.get("inputCurrencyCode").getStringCellValue();
        if(iso4217Currencies.contains(sourceCurrencyString)) {
            record.setCurrency(sourceCurrencyString);
        }
        else if(Util.isNullOrEmpty(sourceCurrencyString) || sourceCurrencyString.toUpperCase().equals("KČ")
                || sourceCurrencyString.toUpperCase().equals("KC")) {
            record.setCurrency("CZK");
        }
        else {
            throw new TransformException("Unrecognized currency code: " + sourceCurrencyString, TransformException.Severity.PROPERTY_LOCAL);
        }
    }
}
