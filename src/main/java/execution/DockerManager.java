package execution;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.*;
import models.Instance;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Conf;
import java.util.*;

import static java.util.Collections.singletonList;

/**
 * Implementation of platform abstract class to give support for VirtualBox
 * platform.
 */
public class DockerManager {

    final static Logger logger = LoggerFactory.getLogger(DockerManager.class);

    /**
     * The grace period (the amount of seconds to wait between SIGTERM and
     * SIGKILL) that is used when stopping a container
     */
    private final static int STOP_GRACE_PERIOD_SECONDS = 10;

    private final static String REGISTRY_FQDN = Conf.REGISTRY_FQDN;

    /**
     * Docker client that will communicate with the daemon. It is built from
     * the DOCKER_HOST environment variable
     */
    private DockerClient docker;

    /**
     * Class constructor
     */
    public DockerManager() {
        try {
            docker = DefaultDockerClient
                    .fromEnv()
                    .build();
            logger.info("{}",docker.info());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error connecting to local Docker daemon.");
            System.exit(1);
        }
    }

    /**
     * Builds the image and creates a container with it.
     * Sets the execution ID of image
     * @param imageId
     * @param basePort
     * @return
     * @throws Exception
     */
    public String createContainer(String imageId, Integer basePort) throws Exception{
        ContainerConfig containerCfg = createContainerConfig(imageId, basePort);
        final ContainerCreation container = docker.createContainer(containerCfg);
        return container.id();
    }

    private ContainerConfig createContainerConfig(String imageId, Integer basePort){
        Map<String, List<PortBinding>> map = new HashMap<>();

        List<String> exposedPorts = new ArrayList<>();

        //Exposing SSH
        exposedPorts.add(String.valueOf("22"));
        map.put("22", Arrays.asList( PortBinding.of( "", basePort ) ) );

        for (int i = 1; i < Conf.AMOUNT_EXPOSED_PORTS; i++) {
            String port = String.valueOf(basePort + i);
            map.put(port, Arrays.asList( PortBinding.of( "", port ) ) );
            exposedPorts.add(port);
        }

        HostConfig hostCfg = HostConfig.builder()
                .portBindings(map)
//                .cpuQuota().memory()
                .build();

        ContainerConfig containerCfg = ContainerConfig.builder()
                .image(imageId)
                .exposedPorts( exposedPorts.stream().toArray(String[]::new) )
                .hostConfig(hostCfg)
                .build();
        return containerCfg;
    }

    public void startExecution(String imageId) throws Exception {
        docker.startContainer(imageId);
        logger.info("Started instance: {}", imageId);
    }

    public void pullImage(String imageTag, User user) throws Exception{
        final RegistryAuth registryAuth = RegistryAuth.builder()
                .email(user.getEmail())
                .username(user.getEmail())
                .password(user.getPortusToken())
                .build();
        docker.pull(imageTag, registryAuth);
        logger.info("Pulled instance: {}", imageTag);
    }

    /**
     * Sends a stop command to the platform
     * @param containerId
     * @throws Exception
     */
    public void stopExecution(String containerId) throws Exception {
        docker.stopContainer(containerId, Conf.STOP_GRACE_PERIOD_SECONDS);
    }


    /**
     * Removes the container. The container must be stopped before it can be
     * removed.
     * @param containerId
     * @throws Exception
     */
    public void removeContainer(String containerId) throws Exception {
        docker.removeContainer(containerId);
    }

    /**
     *
     * @param containerId
     * @throws Exception
     */
    public void restartContainer(String containerId) throws Exception {
        docker.restartContainer(containerId, Conf.STOP_GRACE_PERIOD_SECONDS);
    }

    public String commitContainer(Instance instance, User user) throws Exception {
        String namespace = user.getEmail().replace('@', '_');
        String repository = Conf.REGISTRY_FQDN;
        String containerFullTag = (repository+ "/" +namespace+ "/" +instance.getId()).toLowerCase();
        docker.commitContainer(instance.getContainerId(),
                containerFullTag,
                "paused",
                ContainerConfig.builder().build(),
                "",
                "");

        return containerFullTag;
    }

    public void pushContainer(String image, User user) throws Exception {
        final RegistryAuth registryAuth = RegistryAuth.builder()
                .email(user.getEmail())
                .username(user.getEmail())
                .password(user.getPortusToken())
                .build();
        docker.push(image, registryAuth);
    }

    public void testPrivateRegistryConnection() throws Exception{
        docker.pull(REGISTRY_FQDN + "/" + Conf.EMPTY_IMAGE_NAME);
    }


//
//    /**
//     * Executes a command to the VM itself
//     * @param image copy in which command will be executed
//     * @param command command to be executed
//     * @param args command arguments
//     */
//    public void executeCommandOnExecution(ImageCopy image,String command, String... args) throws PlatformOperationException {
//        System.out.println("DockerExecute: " + command);
//        String[] cmd = new String[args.length + 1];
//        cmd[0] = command;
//        for(int i = 1; i < cmd.length; i++) {
//            System.out.println("DockerExecute-arg"+(i-1)+": " + args[i-1]);
//            cmd[i] = args[i-1];
//        }
//
//        try {
//            ExecCreation exec = docker.execCreate(image.getPlatformExecutionID(), cmd);
//            docker.execStart(exec.id(), ExecStartParameter.DETACH);
//        } catch (DockerException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

//
//    public List<Execution> checkExecutions(Collection<Execution> executions) {
//        List<Container> containers;
//        try {
//            // Returns only the running containers
//            containers = docker.listContainers();
//        } catch (DockerException | InterruptedException e) {
//            e.printStackTrace();
//            return null;
//        }
//
//        List<Execution> executionsToDelete = new ArrayList<Execution>();
//
//        Outer: for(Execution execution : executions) {
//            for(Container c : containers) {
//                if(c.id().equals(execution.getImage().getPlatformExecutionID())) {
//                    continue Outer;
//                }
//            }
//            executionsToDelete.add(execution);
//        }
//        return executionsToDelete;
//    }
//
//
}
