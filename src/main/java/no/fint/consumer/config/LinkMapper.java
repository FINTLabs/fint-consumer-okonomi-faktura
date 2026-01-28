package no.fint.consumer.config;

import no.fint.consumer.utils.RestEndpoints;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import no.novari.fint.model.okonomi.faktura.Faktura;
import no.novari.fint.model.okonomi.faktura.Fakturagrunnlag;
import no.novari.fint.model.okonomi.faktura.Fakturautsteder;

public class LinkMapper {

    public static Map<String, String> linkMapper(String contextPath) {
        return ImmutableMap.<String,String>builder()
            .put(Faktura.class.getName(), contextPath + RestEndpoints.FAKTURA)
            .put(Fakturagrunnlag.class.getName(), contextPath + RestEndpoints.FAKTURAGRUNNLAG)
            .put(Fakturautsteder.class.getName(), contextPath + RestEndpoints.FAKTURAUTSTEDER)
            .put("no.novari.fint.model.felles.kodeverk.iso.Landkode", "/felles/kodeverk/iso/landkode")
            .put("no.novari.fint.model.okonomi.kodeverk.Vare", "/okonomi/kodeverk/vare")
            .put("no.novari.fint.model.felles.Person", "/felles/person")
            .put("no.novari.fint.model.administrasjon.organisasjon.Organisasjonselement", "/administrasjon/organisasjon/organisasjonselement")
            /* .put(TODO,TODO) */
            .build();
    }

}
