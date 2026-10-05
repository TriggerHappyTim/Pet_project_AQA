package com.bft.service.grpc;

/**
 * Registry of EVS backend gRPC endpoints.
 *
 * <p>Defaults match the kubectl port-forwards from CI
 * (see .gitlab-ci.yml: grpc-metadata:12099, sequence:9090,
 * noble-registry-service:12089, transact-manager:13000).
 *
 * <p>Each endpoint can be overridden:
 * <ul>
 *   <li>Host: {@code grpc.<name>.host} / {@code GRPC_<NAME>_HOST}</li>
 *   <li>Port: {@code grpc.<name>.port} / {@code GRPC_<NAME>_PORT}</li>
 *   <li>TLS:  {@code grpc.<name>.tls} / {@code GRPC_<NAME>_TLS} ("true"/"false")</li>
 * </ul>
 *
 * <p>Example: {@code -Dgrpc.metadata.host=172.18.32.153 -Dgrpc.metadata.port=12099}
 */
public final class GrpcConfig {

    private static volatile GrpcConfig instance;

    private static final String DEFAULT_HOST = "localhost";

    /** Known EVS backend services. */
    public enum Endpoint {
        METADATA("metadata", 12099),
        SEQUENCE("sequence", 9090),
        NOBLE_REGISTRY("noble-registry", 12089),
        TRANSACT_MANAGER("transact-manager", 13000);

        private final String name;
        private final int defaultPort;

        Endpoint(String name, int defaultPort) {
            this.name = name;
            this.defaultPort = defaultPort;
        }

        public String getConfigKey() {
            return name;
        }
    }

    private GrpcConfig() {
    }

    /**
     * Resolves the endpoint address for a known service.
     */
    public String host(Endpoint endpoint) {
        return resolve("grpc." + endpoint.getConfigKey() + ".host",
                envName(endpoint, "HOST"), DEFAULT_HOST);
    }

    /**
     * Resolves the endpoint port for a known service.
     */
    public int port(Endpoint endpoint) {
        return resolveInt("grpc." + endpoint.getConfigKey() + ".port",
                envName(endpoint, "PORT"), endpoint.defaultPort);
    }

    /**
     * Resolves whether TLS is required for the endpoint (default false — plaintext inside cluster).
     */
    public boolean tls(Endpoint endpoint) {
        return resolveBoolean("grpc." + endpoint.getConfigKey() + ".tls",
                envName(endpoint, "TLS"), false);
    }

    /**
     * Resolves an arbitrary named endpoint not present in {@link Endpoint}.
     *
     * @param name       logical name, e.g. "my-service"
     * @param defaultPort fallback port when nothing configured
     */
    public NamedEndpoint namedEndpoint(String name, int defaultPort) {
        String prefix = "grpc." + name;
        return new NamedEndpoint(
                resolve(prefix + ".host", envOf(name, "HOST"), DEFAULT_HOST),
                resolveInt(prefix + ".port", envOf(name, "PORT"), defaultPort),
                resolveBoolean(prefix + ".tls", envOf(name, "TLS"), false));
    }

    /** Simple holder for arbitrary endpoints. */
    public static class NamedEndpoint {
        public final String host;
        public final int port;
        public final boolean tls;

        NamedEndpoint(String host, int port, boolean tls) {
            this.host = host;
            this.port = port;
            this.tls = tls;
        }
    }

    public static GrpcConfig getInstance() {
        if (instance == null) {
            synchronized (GrpcConfig.class) {
                if (instance == null) {
                    instance = new GrpcConfig();
                }
            }
        }
        return instance;
    }

    /** Resets cached config (for tests). */
    static void reset() {
        instance = null;
    }

    private String envName(Endpoint endpoint, String suffix) {
        return "GRPC_" + endpoint.getConfigKey().replace('-', '_').toUpperCase() + "_" + suffix;
    }

    private String envOf(String name, String suffix) {
        return "GRPC_" + name.replace('-', '_').toUpperCase() + "_" + suffix;
    }

    private String resolve(String sysProp, String envVar, String defaultValue) {
        String value = System.getProperty(sysProp);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        value = System.getenv(envVar);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return defaultValue;
    }

    private int resolveInt(String sysProp, String envVar, int defaultValue) {
        String value = resolve(sysProp, envVar, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean resolveBoolean(String sysProp, String envVar, boolean defaultValue) {
        String value = resolve(sysProp, envVar, null);
        return value == null ? defaultValue : Boolean.parseBoolean(value.trim());
    }
}
