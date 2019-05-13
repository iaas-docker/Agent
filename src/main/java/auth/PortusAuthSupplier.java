package auth;

import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;

@Deprecated
public class PortusAuthSupplier implements RegistryAuthSupplier {

    @Override
    public RegistryAuth authFor(String imageName) throws DockerException {
        return RegistryAuth.builder()
                .email("")
                .username("")
                .identityToken("")
                .build();
    }

    @Override
    public RegistryAuth authForSwarm() throws DockerException {
        return null;
    }

    @Override
    public RegistryConfigs authForBuild() throws DockerException {
        return null;
    }
}
