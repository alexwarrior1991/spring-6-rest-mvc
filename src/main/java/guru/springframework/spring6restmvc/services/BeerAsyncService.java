package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BeerAsyncService {

    CompletableFuture<Page<BeerDTO>> listBeersAsync(String beerName, BeerStyle beerStyle, Boolean showInventory,
                                                    Integer pageNumber, Integer pageSize);

    CompletableFuture<Optional<BeerDTO>> getBeerByIdAsync(UUID id);

    CompletableFuture<BeerDTO> saveNewBeerAsync(BeerDTO beer);

    CompletableFuture<Optional<BeerDTO>> updateBeerByIdAsync(UUID beerId, BeerDTO beer);

    CompletableFuture<Boolean> deleteByIdAsync(UUID beerId);

    CompletableFuture<Optional<BeerDTO>> patchBeerByIdAsync(UUID beerId, BeerDTO beer);

    CompletableFuture<List<BeerDTO>> saveAllBulkAsync(List<BeerDTO> beers);

}
