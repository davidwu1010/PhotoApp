package com.photoapp.api.users.data;

import com.photoapp.api.users.ui.models.AlbumResponseModel;
import feign.FeignException;
import feign.hystrix.FallbackFactory;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="albums-ws", fallbackFactory=AlbumsFallbackFactory.class)
public interface AlbumsServiceClient {

    @GetMapping("/users/{id}/albums")
    public List<AlbumResponseModel> getAlbums(@PathVariable String id);
}

@Component
class AlbumsFallbackFactory implements FallbackFactory<AlbumsServiceClient> {

    @Override
    public AlbumsServiceClient create(Throwable cause) {
        return new AlbumsServiceClientFallback(cause);
    }
}

class AlbumsServiceClientFallback implements AlbumsServiceClient {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Throwable cause;

    AlbumsServiceClientFallback(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public List<AlbumResponseModel> getAlbums(String id) {

        if (cause instanceof FeignException && ((FeignException) cause).status() == 404) {
            logger.error(cause.getLocalizedMessage());
        } else {
            logger.error(cause.getLocalizedMessage());
        }

        return new ArrayList<>();
    }
}
