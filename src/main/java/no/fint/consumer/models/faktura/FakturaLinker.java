package no.fint.consumer.models.faktura;

import no.fint.model.resource.okonomi.faktura.FakturaResource;
import no.fint.model.resource.okonomi.faktura.FakturaResources;
import no.fint.relations.FintLinker;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.springframework.util.StringUtils.isEmpty;

@Component
public class FakturaLinker extends FintLinker<FakturaResource> {

    public FakturaLinker() {
        super(FakturaResource.class);
    }

    public void mapLinks(FakturaResource resource) {
        super.mapLinks(resource);
    }

    @Override
    public FakturaResources toResources(Collection<FakturaResource> collection) {
        return toResources(collection.stream(), 0, 0, collection.size());
    }

    @Override
    public FakturaResources toResources(Stream<FakturaResource> stream, int offset, int size, int totalItems) {
        FakturaResources resources = new FakturaResources();
        stream.map(this::toResource).forEach(resources::addResource);
        addPagination(resources, offset, size, totalItems);
        return resources;
    }

    @Override
    public String getSelfHref(FakturaResource faktura) {
        return getAllSelfHrefs(faktura).findFirst().orElse(null);
    }

    @Override
    public Stream<String> getAllSelfHrefs(FakturaResource faktura) {
        Stream.Builder<String> builder = Stream.builder();
        if (!isNull(faktura.getFakturanummer()) && !isEmpty(faktura.getFakturanummer().getIdentifikatorverdi())) {
            builder.add(createHrefWithId(faktura.getFakturanummer().getIdentifikatorverdi(), "fakturanummer"));
        }
        
        return builder.build();
    }

    int[] hashCodes(FakturaResource faktura) {
        IntStream.Builder builder = IntStream.builder();
        if (!isNull(faktura.getFakturanummer()) && !isEmpty(faktura.getFakturanummer().getIdentifikatorverdi())) {
            builder.add(faktura.getFakturanummer().getIdentifikatorverdi().hashCode());
        }
        
        return builder.build().toArray();
    }

}

