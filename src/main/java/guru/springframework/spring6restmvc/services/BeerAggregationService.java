package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class BeerAggregationService {

    private final BeerAsyncService beerAsyncService;

    public CompletableFuture<Map<String, Object>> listAndGetAsync(String beerName, BeerStyle style, boolean showInventory,
                                                                  int page, int size, UUID featuredId) {
        var pageFuture = beerAsyncService.listBeersAsync(beerName, style, showInventory, page, size);
        var featuredFuture = beerAsyncService.getBeerByIdAsync(featuredId)
                .thenApply(opt -> opt.orElse(null));

        return pageFuture.thenCombine(featuredFuture, (Page<BeerDTO> p, BeerDTO featured) -> Map.of(
                "page", p,
                "featured", featured
        ));
    }
}
