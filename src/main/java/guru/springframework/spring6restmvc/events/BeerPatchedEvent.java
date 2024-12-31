package guru.springframework.spring6restmvc.events;

import guru.springframework.spring6restmvc.entities.Beer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BeerPatchedEvent implements BeerEvent{
    private Beer beer;

    private Authentication authentication;
}