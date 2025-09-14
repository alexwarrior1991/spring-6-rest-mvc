package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.events.BeerCreatedEvent;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerAsyncServiceImpl implements BeerAsyncService {

    private final BeerService beerService;

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PlatformTransactionManager transactionManager;

    private TransactionTemplate txTemplate() {
        return new TransactionTemplate((transactionManager));
    }

    @Async
    @Override
    public CompletableFuture<Page<BeerDTO>> listBeersAsync(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize) {
        var page = beerService.listBeers(beerName, beerStyle, showInventory, pageNumber, pageSize);
        return CompletableFuture.completedFuture(page);
    }

    @Async
    @Override
    public CompletableFuture<Optional<BeerDTO>> getBeerByIdAsync(UUID id) {
        return CompletableFuture.completedFuture(beerService.getBeerById(id));
    }

    @Async
    @Override
    public CompletableFuture<BeerDTO> saveNewBeerAsync(BeerDTO beer) {
        return CompletableFuture.completedFuture(beerService.saveNewBeer(beer));
    }

    @Async
    @Override
    public CompletableFuture<Optional<BeerDTO>> updateBeerByIdAsync(UUID beerId, BeerDTO beer) {
        return CompletableFuture.completedFuture(beerService.updateBeerById(beerId, beer));
    }

    @Async
    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(UUID beerId) {
        return CompletableFuture.completedFuture(beerService.deleteById(beerId));
    }

    @Async
    @Override
    public CompletableFuture<Optional<BeerDTO>> patchBeerByIdAsync(UUID beerId, BeerDTO beer) {
        return CompletableFuture.completedFuture(beerService.patchBeerById(beerId, beer));
    }

    @Async
    @Override
    public CompletableFuture<List<BeerDTO>> saveAllBulkAsync(List<BeerDTO> beers) {
        if (beers == null || beers.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        List<BeerDTO> result = txTemplate().execute(status -> {
            // 1) Invalidar caché de lista una sola vez
            var beerListCache = cacheManager.getCache("beerListCache");
            if (beerListCache != null) beerListCache.clear();

            // 2) Mapear DTO -> Entity en bloque
            List<Beer> entities = beers.stream().map(beerMapper::beerDtoToBeer).toList();

            // 3) Persistir en DB (saveAll)
            List<Beer> saved = beerRepository.saveAll(entities);

            // 4) Publicar evento por item con el principal actual
            var auth = SecurityContextHolder.getContext().getAuthentication();
            saved.forEach(b -> applicationEventPublisher.publishEvent(new BeerCreatedEvent(b, auth)));

            return saved.stream().map(beerMapper::beerToBeerDto).toList();
        });

        return CompletableFuture.completedFuture(result);
    }

    @Async
    public CompletableFuture<List<BeerDTO>> saveAllBulkOptimizedAsync(List<BeerDTO> beers, int chunkSize) {
        if (beers == null || beers.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }
        if (chunkSize <= 0) chunkSize = 1000; // valor por defecto

        var all = new ArrayList<BeerDTO>(beers.size());

        for (int i = 0; i < beers.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, beers.size());
            List<BeerDTO> chunk = beers.subList(i, end);

            List<BeerDTO> savedChunk = txTemplate().execute(status -> {
                var beerListCache = cacheManager.getCache("beerListCache");
                if (beerListCache != null) beerListCache.clear();

                var entities = chunk.stream().map(beerMapper::beerDtoToBeer).toList();
                var saved = beerRepository.saveAll(entities);

                var auth = SecurityContextHolder.getContext().getAuthentication();
                saved.forEach(b -> applicationEventPublisher.publishEvent(new BeerCreatedEvent(b, auth)));

                return saved.stream().map(beerMapper::beerToBeerDto).toList();
            });

            all.addAll(savedChunk);
        }

        return CompletableFuture.completedFuture(all);
    }
}
