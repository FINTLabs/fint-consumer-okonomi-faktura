package no.fint.consumer.models.fakturagrunnlag;

import no.fint.model.resource.okonomi.faktura.FakturagrunnlagResource;
import no.fint.model.resource.okonomi.faktura.FakturagrunnlagResources;
import no.fint.relations.FintLinker;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.springframework.util.StringUtils.isEmpty;

@Component
public class FakturagrunnlagLinker extends FintLinker<FakturagrunnlagResource> {

    public FakturagrunnlagLinker() {
        super(FakturagrunnlagResource.class);
    }

    public void mapLinks(FakturagrunnlagResource resource) {
        super.mapLinks(resource);
    }

    @Override
    public FakturagrunnlagResources toResources(Collection<FakturagrunnlagResource> collection) {
        return toResources(collection.stream(), 0, 0, collection.size());
    }

    @Override
    public FakturagrunnlagResources toResources(Stream<FakturagrunnlagResource> stream, int offset, int size, int totalItems) {
        FakturagrunnlagResources resources = new FakturagrunnlagResources();
        stream.map(this::toResource).forEach(resources::addResource);
        addPagination(resources, offset, size, totalItems);
        return resources;
    }

    @Override
    public String getSelfHref(FakturagrunnlagResource fakturagrunnlag) {
        return getAllSelfHrefs(fakturagrunnlag).findFirst().orElse(null);
    }

    @Override
    public Stream<String> getAllSelfHrefs(FakturagrunnlagResource fakturagrunnlag) {
        Stream.Builder<String> builder = Stream.builder();
        if (!isNull(fakturagrunnlag.getOrdrenummer()) && !isEmpty(fakturagrunnlag.getOrdrenummer().getIdentifikatorverdi())) {
            builder.add(createHrefWithId(fakturagrunnlag.getOrdrenummer().getIdentifikatorverdi(), "ordrenummer"));
        }
        
        return builder.build();
    }

    int[] hashCodes(FakturagrunnlagResource fakturagrunnlag) {
        IntStream.Builder builder = IntStream.builder();
        if (!isNull(fakturagrunnlag.getOrdrenummer()) && !isEmpty(fakturagrunnlag.getOrdrenummer().getIdentifikatorverdi())) {
            builder.add(fakturagrunnlag.getOrdrenummer().getIdentifikatorverdi().hashCode());
        }
        
        return builder.build().toArray();
    }

}

