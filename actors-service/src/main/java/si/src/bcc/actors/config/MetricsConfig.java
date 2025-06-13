package si.src.bcc.actors.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class MetricsConfig {

    @Bean
    public OncePerRequestFilter metricsFilter(MeterRegistry registry) {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                String path = request.getRequestURI();
                String method = request.getMethod();

                // Record request count
                registry.counter("http_requests_total",
                        "path", path,
                        "method", method,
                        "service", "movies-service"
                ).increment();

                // Record request duration
                long startTime = System.currentTimeMillis();
                try {
                    filterChain.doFilter(request, response);
                } finally {
                    long duration = System.currentTimeMillis() - startTime;
                    registry.timer("http_request_duration_seconds",
                            "path", path,
                            "method", method,
                            "service", "movies-service"
                    ).record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
                }
            }
        };
    }
}