package io.mediahub.catalog;

import org.springframework.boot.SpringApplication;

/** Run from the IDE to start the service against a throwaway MongoDB container: no local install needed. */
public class LocalCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.from(CatalogApplication::main)
                .with(TestcontainersConfig.class)
                .withAdditionalProfiles("sample-data")
                .run(args);
    }
}
