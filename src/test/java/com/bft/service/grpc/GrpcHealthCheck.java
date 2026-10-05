package com.bft.service.grpc;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Connectivity checks for EVS gRPC backend services.
 *
 * <p>Works without generated stubs — uses {@link ManagedChannel#getState()}
 * transport-level state. Useful for pre-flight infrastructure checks
 * (integration with {@code @DisabledByInfrastructure}) and for diagnosing
 * CI port-forward problems.
 *
 * <p>Usage:
 * <pre>{@code
 * GrpcHealthCheck health = new GrpcHealthCheck();
 * boolean ok = health.isReachable(GrpcConfig.Endpoint.METADATA, Duration.ofSeconds(5));
 * health.checkAll(Duration.ofSeconds(5),
 *     GrpcConfig.Endpoint.METADATA,
 *     GrpcConfig.Endpoint.SEQUENCE); // throws when any endpoint is unreachable
 * }</pre>
 */
public class GrpcHealthCheck {

    private static final Logger log = LoggerFactory.getLogger(GrpcHealthCheck.class);

    private final List<String> failures = new ArrayList<>();

    /**
     * Returns true when the endpoint channel reaches READY state within timeout.
     */
    public boolean isReachable(GrpcConfig.Endpoint endpoint, Duration timeout) {
        try {
            ManagedChannel channel = GrpcChannelFactory.channel(endpoint);
            boolean ready = GrpcChannelFactory.awaitReady(channel, timeout);
            if (!ready) {
                String message = String.format("gRPC '%s' not reachable at %s:%d within %s (state=%s)",
                        endpoint.getConfigKey(),
                        GrpcConfig.getInstance().host(endpoint),
                        GrpcConfig.getInstance().port(endpoint),
                        timeout,
                        channel.getState(false));
                log.warn(message);
                failures.add(message);
            } else {
                log.info("gRPC '{}' is reachable", endpoint.getConfigKey());
            }
            return ready;
        } catch (Exception e) {
            String message = String.format("gRPC '%s' check failed: %s",
                    endpoint.getConfigKey(), e.getMessage());
            log.warn(message);
            failures.add(message);
            return false;
        }
    }

    /**
     * Checks multiple endpoints; throws AssertionError listing all unreachable ones.
     */
    public void checkAll(Duration timeout, GrpcConfig.Endpoint... endpoints) {
        failures.clear();
        for (GrpcConfig.Endpoint endpoint : endpoints) {
            isReachable(endpoint, timeout);
        }
        if (!failures.isEmpty()) {
            throw new AssertionError(failures.size() + " gRPC endpoint(s) unreachable:\n  - "
                    + String.join("\n  - ", failures));
        }
    }

    /**
     * Maps a {@link ConnectivityState} to a human-readable string.
     */
    public static String describe(ConnectivityState state) {
        return state == null ? "UNKNOWN" : state.name();
    }
}
