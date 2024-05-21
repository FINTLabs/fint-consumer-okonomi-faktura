package no.fint.consumer.models.fakturagrunnlag;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

import no.fint.cache.CacheService;
import no.fint.cache.model.CacheObject;
import no.fint.consumer.config.Constants;
import no.fint.consumer.config.ConsumerProps;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.relations.FintResourceCompatibility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.fint.model.okonomi.faktura.Fakturagrunnlag;
import no.fint.model.resource.okonomi.faktura.FakturagrunnlagResource;
import no.fint.model.okonomi.faktura.FakturaActions;
import no.fint.model.felles.kompleksedatatyper.Identifikator;

@Slf4j
@Service
@ConditionalOnProperty(name = "fint.consumer.cache.disabled.fakturagrunnlag", havingValue = "false", matchIfMissing = true)
public class FakturagrunnlagCacheService extends CacheService<FakturagrunnlagResource> {

    public static final String MODEL = Fakturagrunnlag.class.getSimpleName().toLowerCase();

    @Value("${fint.consumer.compatibility.fintresource:true}")
    private boolean checkFintResourceCompatibility;

    @Autowired
    private FintResourceCompatibility fintResourceCompatibility;

    @Autowired
    private ConsumerEventUtil consumerEventUtil;

    @Autowired
    private ConsumerProps props;

    @Autowired
    private FakturagrunnlagLinker linker;

    private JavaType javaType;

    private ObjectMapper objectMapper;

    public FakturagrunnlagCacheService() {
        super(MODEL, FakturaActions.GET_ALL_FAKTURAGRUNNLAG, FakturaActions.UPDATE_FAKTURAGRUNNLAG);
        objectMapper = new ObjectMapper();
        javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, FakturagrunnlagResource.class);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @PostConstruct
    public void init() {
        props.getAssets().forEach(this::createCache);
    }

    @Scheduled(initialDelayString = Constants.CACHE_INITIALDELAY_FAKTURAGRUNNLAG, fixedRateString = Constants.CACHE_FIXEDRATE_FAKTURAGRUNNLAG)
    public void populateCacheAll() {
        props.getAssets().forEach(this::populateCache);
    }

    public void rebuildCache(String orgId) {
		flush(orgId);
		populateCache(orgId);
	}

    @Override
    public void populateCache(String orgId) {
		log.info("Populating Fakturagrunnlag cache for {}", orgId);
        Event event = new Event(orgId, Constants.COMPONENT, FakturaActions.GET_ALL_FAKTURAGRUNNLAG, Constants.CACHE_SERVICE);
        consumerEventUtil.send(event);
    }


    public Optional<FakturagrunnlagResource> getFakturagrunnlagByOrdrenummer(String orgId, String ordrenummer) {
        return getOne(orgId, ordrenummer.hashCode(),
            (resource) -> Optional
                .ofNullable(resource)
                .map(FakturagrunnlagResource::getOrdrenummer)
                .map(Identifikator::getIdentifikatorverdi)
                .map(ordrenummer::equals)
                .orElse(false));
    }


	@Override
    public void onAction(Event event) {
        List<FakturagrunnlagResource> data;
        if (checkFintResourceCompatibility && fintResourceCompatibility.isFintResourceData(event.getData())) {
            log.info("Compatibility: Converting FintResource<FakturagrunnlagResource> to FakturagrunnlagResource ...");
            data = fintResourceCompatibility.convertResourceData(event.getData(), FakturagrunnlagResource.class);
        } else {
            data = objectMapper.convertValue(event.getData(), javaType);
        }
        data.forEach(resource -> {
            linker.mapLinks(resource);
            linker.resetSelfLinks(resource);
        });
        if (FakturaActions.valueOf(event.getAction()) == FakturaActions.UPDATE_FAKTURAGRUNNLAG) {
            if (event.getResponseStatus() == ResponseStatus.ACCEPTED || event.getResponseStatus() == ResponseStatus.CONFLICT) {
                List<CacheObject<FakturagrunnlagResource>> cacheObjects = data
                    .stream()
                    .map(i -> new CacheObject<>(i, linker.hashCodes(i)))
                    .collect(Collectors.toList());
                addCache(event.getOrgId(), cacheObjects);
                log.info("Added {} cache objects to cache for {}", cacheObjects.size(), event.getOrgId());
            } else {
                log.debug("Ignoring payload for {} with response status {}", event.getOrgId(), event.getResponseStatus());
            }
        } else {
            List<CacheObject<FakturagrunnlagResource>> cacheObjects = data
                    .stream()
                    .map(i -> new CacheObject<>(i, linker.hashCodes(i)))
                    .collect(Collectors.toList());
            updateCache(event.getOrgId(), cacheObjects);
            log.info("Updated cache for {} with {} cache objects", event.getOrgId(), cacheObjects.size());
        }
    }
}
