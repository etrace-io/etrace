package io.etrace.collector.cluster.discovery;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInstance {
    @Builder.Default
    private final int weight = 1;
    /**
     * name of the service
     */
    @EqualsAndHashCode.Include
    private String cluster;
    /**
     * address of this instance
     */
    @EqualsAndHashCode.Include
    private String address;
    /**
     * the port for this instance
     */
    @EqualsAndHashCode.Include
    private int port;
    @EqualsAndHashCode.Include
    private String serverType;
    private int httpPort;
    private boolean enabled = true;
}
