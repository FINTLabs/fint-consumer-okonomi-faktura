package no.fint.consumer.models.faktura;

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

import no.fint.model.okonomi.faktura.Faktura;
import no.fint.model.resource.okonomi.faktura.FakturaResource;
import no.fint.model.okonomi.faktura.FakturaActions;
import no.fint.model.felles.kompleksedatatyper.Identifikator;

@Slf4j
@Service
@ConditionalOnProperty(name = "fint.consumer.cache.disabled.faktura", havingValue = "false", matchIfMissing = true)
public class FakturaCacheService extends CacheService<FakturaResource> {

    public static final String MODEL = Faktura.class.getSimpleName().toLowerCase();

    @Value("${fint.consumer.compatibility.fintresource:true}")
    private boolean checkFintResourceCompatibility;

    @Autowired
    private FintResourceCompatibility fintResourceCompatibility;

    @Autowired
    private ConsumerEventUtil consumerEventUtil;

    @Autowired
    private ConsumerProps props;

    @Autowired
    private FakturaLinker linker;

    private JavaType javaType;

    private ObjectMapper objectMapper;

    public FakturaCacheService() {
        super(MODEL, FakturaActions.GET_ALL_FAKTURA, FakturaActions.UPDATE_FAKTURA);
        objectMapper = new ObjectMapper();
        javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, FakturaResource.class);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @PostConstruct
    public void init() {
        props.getAssets().forEach(this::createCache);
    }

    @Scheduled(initialDelayString = Constants.CACHE_INITIALDELAY_FAKTURA, fixedRateString = Constants.CACHE_FIXEDRATE_FAKTURA)
    public void populateCacheAll() {
        props.getAssets().forEach(this::populateCache);
    }

    public void rebuildCache(String orgId) {
		flush(orgId);
		populateCache(orgId);
	}

    @Override
    public void populateCache(String orgId) {
		log.info("Populating Faktura cache for {}", orgId);
        Event event = new Event(orgId, Constants.COMPONENT, FakturaActions.GET_ALL_FAKTURA, Constants.CACHE_SERVICE);
        consumerEventUtil.send(event);
    }


    public Optional<FakturaResource> getFakturaByFakturanummer(String orgId, String fakturanummer) {
        return getOne(orgId, fakturanummer.hashCode(),
            (resource) -> Optional
                .ofNullable(resource)
                .map(FakturaResource::getFakturanummer)
                .map(Identifikator::getIdentifikatorverdi)
                .map(fakturanummer::equals)
                .orElse(false));
    }


	@Override
    public void onAction(Event event) {
        List<FakturaResource> data;
        if (checkFintResourceCompatibility && fintResourceCompatibility.isFintResourceData(event.getData())) {
            log.info("Compatibility: Converting FintResource<FakturaResource> to FakturaResource ...");
            data = fintResourceCompatibility.convertResourceData(event.getData(), FakturaResource.class);
        } else {
            data = objectMapper.convertValue(event.getData(), javaType);
        }
        data.forEach(resource -> {
            linker.mapLinks(resource);
            linker.resetSelfLinks(resource);
        });
        if (FakturaActions.valueOf(event.getAction()) == FakturaActions.UPDATE_FAKTURA) {
            if (event.getResponseStatus() == ResponseStatus.ACCEPTED || event.getResponseStatus() == ResponseStatus.CONFLICT) {
                List<CacheObject<FakturaResource>> cacheObjects = data
                    .stream()
                    .map(i -> new CacheObject<>(i, linker.hashCodes(i)))
                    .collect(Collectors.toList());
                addCache(event.getOrgId(), cacheObjects);
                log.info("Added {} cache objects to cache for {}", cacheObjects.size(), event.getOrgId());
            } else {
                log.debug("Ignoring payload for {} with response status {}", event.getOrgId(), event.getResponseStatus());
            }
        } else {
            List<CacheObject<FakturaResource>> cacheObjects = data
                    .stream()
                    .map(i -> new CacheObject<>(i, linker.hashCodes(i)))
                    .collect(Collectors.toList());
            updateCache(event.getOrgId(), cacheObjects);
            log.info("Updated cache for {} with {} cache objects", event.getOrgId(), cacheObjects.size());
        }
    }
}
