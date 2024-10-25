package io.quarkus.test.services.containers;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.ServiceLoader;

import io.quarkus.test.bootstrap.LocalhostManagedResource;
import io.quarkus.test.bootstrap.ManagedResource;
import io.quarkus.test.bootstrap.ManagedResourceBuilder;
import io.quarkus.test.bootstrap.ServiceContext;
import io.quarkus.test.services.Container;
import io.quarkus.test.utils.PropertiesUtils;

public class ContainerManagedResourceBuilder implements ManagedResourceBuilder {

    private final ServiceLoader<ContainerManagedResourceBinding> managedResourceBindingsRegistry = ServiceLoader
            .load(ContainerManagedResourceBinding.class);

    private ServiceContext context;
    private String image;
    private String expectedLog;
    private String[] command;
    private Integer port;
    private boolean portDockerHostToLocalhost;

    protected String getImage() {
        return image;
    }

    protected String getExpectedLog() {
        return expectedLog;
    }

    protected String[] getCommand() {
        return Optional.ofNullable(command).orElse(new String[] {});
    }

    protected Integer getPort() {
        return port;
    }

    protected ServiceContext getContext() {
        return context;
    }

    protected long getMemoryLimitMiB() {
        return -1;
    }

    @Override
    public void init(Annotation annotation) {
        Container metadata = (Container) annotation;
        init(metadata.image(), metadata.command(), metadata.expectedLog(), metadata.port(),
                metadata.portDockerHostToLocalhost());
    }

    protected void init(String image, String[] command, String expectedLog, int port, boolean portDockerHostToLocalhost) {
        this.image = PropertiesUtils.resolveProperty(image);
        this.command = command;
        this.expectedLog = PropertiesUtils.resolveProperty(expectedLog);
        this.port = port;
        this.portDockerHostToLocalhost = portDockerHostToLocalhost;
    }

    @Override
    public ManagedResource build(ServiceContext context) {
        this.context = context;
        for (ContainerManagedResourceBinding binding : managedResourceBindingsRegistry) {
            if (binding.appliesFor(context)) {
                if (portDockerHostToLocalhost) {
                    return new LocalhostManagedResource(binding.init(this));
                }
                return binding.init(this);
            }
        }

        if (portDockerHostToLocalhost) {
            return new LocalhostManagedResource(new GenericDockerContainerManagedResource(this));
        }
        return new GenericDockerContainerManagedResource(this);
    }
}
