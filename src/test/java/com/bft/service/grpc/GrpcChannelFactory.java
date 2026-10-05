package com.bft.service.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Factory for managed gRPC channels to EVS backend services.
 *
 * <p>Channels are cached per endpoint and shared across tests. TLS channels
 * use an insecure trust manager (test environments use self-signed certificates),
 * mirroring the reference implementation.
 *
 * <p>This factory provides transport only. When .proto contracts for EVS services
 * become available, generate stubs (protobuf-maven-plugin) and build them on top
 * of these channels:
 * <pre>{@code
 * ManagedChannel channel = GrpcChannelFactory.channel(GrpcConfig.Endpoint.METADATA);
 * MyServiceGrpc.MyServiceBlockingStub stub = MyServiceGrpc.newBlockingStub(channel);
 * }</pre>
 *
 * <p>Usage:
 * <pre>{@code
 * try {
 *     ManagedChannel channel = GrpcChannelFactory.channel(GrpcConfig.Endpoint.SEQUENCE);
 *     // ... build a stub and call it
 * } finally {
 *     GrpcChannelFactory.shutdownAll();
 * }
 * }</pre>
 */
public final class GrpcChannelFactory {

    private static final Logger log = LoggerFactory.getLogger(GrpcChannelFactory.class);

    private static final int MAX_MESSAGE_SIZE_MB = 16;
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 10;

    private static final Map<String, ManagedChannel> CHANNELS = new ConcurrentHashMap<>();

    private static final GrpcConfig config = GrpcConfig.getInstance();

    private GrpcChannelFactory() {
    }

    /**
     * Returns a cached channel for a known endpoint (plaintext or TLS per config).
     */
    public static ManagedChannel channel(GrpcConfig.Endpoint endpoint) {
        return channel(endpoint.name(), config.host(endpoint), config.port(endpoint), config.tls(endpoint));
    }

    /**
     * Returns a cached channel for an arbitrary host/port.
     *
     * @param key   cache key (logical service name)
     * @param host  target host
     * @param port  target port
     * @param tls   true to create a TLS channel with insecure trust manager
     */
    public static ManagedChannel channel(String key, String host, int port, boolean tls) {
        return CHANNELS.computeIfAbsent(key, k -> create(key, host, port, tls));
    }

    /**
     * Performs a blocking connectivity wait: resolves when the channel becomes READY
     * or the timeout expires.
     *
     * @return true if the channel reached READY state within timeout
     */
    public static boolean awaitReady(ManagedChannel channel, java.time.Duration timeout) {
        try {
            return channel.getState(true) == io.grpc.ConnectivityState.READY
                    || isReadyAfterWait(channel, timeout);
        } catch (Exception e) {
            log.warn("gRPC: awaitReady failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Shuts down all cached channels. Call from a global teardown (@AfterAll / extension).
     */
    public static void shutdownAll() {
        CHANNELS.values().forEach(channel -> {
            try {
                channel.shutdown().awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.debug("gRPC: shutdown error (ignored): {}", e.getMessage());
            }
        });
        CHANNELS.clear();
        log.info("gRPC: all channels shut down");
    }

    private static boolean isReadyAfterWait(ManagedChannel channel, java.time.Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            io.grpc.ConnectivityState state = channel.getState(false);
            if (state == io.grpc.ConnectivityState.READY) {
                return true;
            }
            if (state == io.grpc.ConnectivityState.SHUTDOWN || state == io.grpc.ConnectivityState.TRANSIENT_FAILURE) {
                // give the channel a chance to reconnect before declaring failure
                channel.notifyWhenStateChanged(state, () -> { });
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return channel.getState(false) == io.grpc.ConnectivityState.READY;
    }

    private static ManagedChannel create(String key, String host, int port, boolean tls) {
        log.info("gRPC: creating {} channel '{}' -> {}:{}", tls ? "TLS" : "plaintext", key, host, port);
        try {
            if (tls) {
                NettyChannelBuilder builder = NettyChannelBuilder.forAddress(host, port)
                        .sslContext(GrpcSslContexts.forClient()
                                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                .build())
                        .maxInboundMessageSize(MAX_MESSAGE_SIZE_MB * 1024 * 1024);
                return builder.build();
            }
            return ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext()
                    .maxInboundMessageSize(MAX_MESSAGE_SIZE_MB * 1024 * 1024)
                    .build();
        } catch (SSLException e) {
            throw new IllegalStateException("Failed to create SSL context for '" + key + "'", e);
        }
    }
}
