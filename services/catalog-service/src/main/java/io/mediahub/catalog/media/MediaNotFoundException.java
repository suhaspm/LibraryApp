package io.mediahub.catalog.media;

public class MediaNotFoundException extends RuntimeException {

    public MediaNotFoundException(String id) {
        super("Media " + id + " does not exist");
    }
}
