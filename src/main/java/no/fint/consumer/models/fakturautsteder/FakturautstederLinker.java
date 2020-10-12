package no.fint.consumer.models.fakturautsteder;

import no.fint.model.resource.okonomi.faktura.FakturautstederResource;
import no.fint.model.resource.okonomi.faktura.FakturautstederResources;
import no.fint.relations.FintLinker;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.springframework.util.StringUtils.isEmpty;

@Component
public class FakturautstederLinker extends FintLinker<FakturautstederResource> {

    public FakturautstederLinker() {
        super(FakturautstederResource.class);
    }

    public void mapLinks(FakturautstederResource resource) {
        super.mapLinks(resource);
    }

    @Override
    public FakturautstederResources toResources(Collection<FakturautstederResource> collection) {
        return toResources(collection.stream(), 0, 0, collection.size());
    }

    @Override
    public FakturautstederResources toResources(Stream<FakturautstederResource> stream, int offset, int size, int totalItems) {
        FakturautstederResources resources = new FakturautstederResources();
        stream.map(this::toResource).forEach(resources::addResource);
        addPagination(resources, offset, size, totalItems);
        return resources;
    }

    @Override
    public String getSelfHref(FakturautstederResource fakturautsteder) {
        return getAllSelfHrefs(fakturautsteder).findFirst().orElse(null);
    }

    @Override
    public Stream<String> getAllSelfHrefs(FakturautstederResource fakturautsteder) {
        Stream.Builder<String> builder = Stream.builder();
        if (!isNull(fakturautsteder.getSystemId()) && !isEmpty(fakturautsteder.getSystemId().getIdentifikatorverdi())) {
            builder.add(createHrefWithId(fakturautsteder.getSystemId().getIdentifikatorverdi(), "systemid"));
        }
        
        return builder.build();
    }

    int[] hashCodes(FakturautstederResource fakturautsteder) {
        IntStream.Builder builder = IntStream.builder();
        if (!isNull(fakturautsteder.getSystemId()) && !isEmpty(fakturautsteder.getSystemId().getIdentifikatorverdi())) {
            builder.add(fakturautsteder.getSystemId().getIdentifikatorverdi().hashCode());
        }
        
        return builder.build().toArray();
    }

}

