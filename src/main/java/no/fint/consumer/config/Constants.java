package no.fint.consumer.config;

public enum Constants {
;

    public static final String COMPONENT = "okonomi-faktura";
    public static final String COMPONENT_CONSUMER = COMPONENT + " consumer";
    public static final String CACHE_SERVICE = "CACHE_SERVICE";

    
    public static final String CACHE_INITIALDELAY_FAKTURA = "${fint.consumer.cache.initialDelay.faktura:900000}";
    public static final String CACHE_FIXEDRATE_FAKTURA = "${fint.consumer.cache.fixedRate.faktura:900000}";
    
    public static final String CACHE_INITIALDELAY_FAKTURAGRUNNLAG = "${fint.consumer.cache.initialDelay.fakturagrunnlag:1000000}";
    public static final String CACHE_FIXEDRATE_FAKTURAGRUNNLAG = "${fint.consumer.cache.fixedRate.fakturagrunnlag:900000}";
    
    public static final String CACHE_INITIALDELAY_FAKTURAUTSTEDER = "${fint.consumer.cache.initialDelay.fakturautsteder:1100000}";
    public static final String CACHE_FIXEDRATE_FAKTURAUTSTEDER = "${fint.consumer.cache.fixedRate.fakturautsteder:900000}";
    

}
