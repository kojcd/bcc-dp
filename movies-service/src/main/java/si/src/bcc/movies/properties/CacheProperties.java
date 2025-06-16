package si.src.bcc.movies.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.cache")
public class CacheProperties {

    private Caffeine caffeine = new Caffeine();

    @Data
    public static class Caffeine {
        private String spec = "maximumSize=1000,expireAfterWrite=30m";
    }
}