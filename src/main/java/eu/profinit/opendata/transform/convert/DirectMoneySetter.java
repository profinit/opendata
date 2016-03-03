package eu.profinit.opendata.transform.convert;

import org.springframework.stereotype.Component;

/**
 * Qualifier class for an unmodified MoneySetter. Spring can't handle a superclass name.
 */
@Component
public class DirectMoneySetter extends MoneySetter {}
