package execution;

import auth.PortusAuthSupplier;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.*;
import init.EntryPoint;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Conf;

import java.io.*;
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
                    .registryAuthSupplier(new PortusAuthSupplier())
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
     * @return
     * @throws Exception
     */
    public String createContainer(String imageId) throws Exception{
        Map<String, List<PortBinding>> map = new HashMap<>();

        //Port is host port
        Integer basePort = 10000;

        List<String> ports = new ArrayList<>();
        ports.add(String.valueOf("22"));

        map.put("22", Arrays.asList( PortBinding.of( "", basePort ) ) );
        for (int i = 1; i < Conf.AMOUNT_EXPOSED_PORTS; i++) {
            String port = String.valueOf(basePort + i);
            map.put(port, Arrays.asList( PortBinding.of( "", port ) ) );
            ports.add(port);
        }

        HostConfig hostCfg = HostConfig.builder()
                .portBindings(map)
//                .cpuQuota().memory()
                .build();

        ContainerConfig containerCfg = ContainerConfig.builder()
                .image(imageId)
                .exposedPorts( ports.stream().toArray(String[]::new) )
                .hostConfig(hostCfg)
                .build();
        final ContainerCreation container = docker.createContainer(containerCfg);
        return container.id();
    }

    public void startExecution(String imageId) throws Exception {
        docker.startContainer(imageId);
        logger.info("Started instance: {}", imageId);
    }

    public void pullImage(String imageTag) throws Exception{
        docker.pull(imageTag);
        logger.info("Pulled instance: {}", imageTag);
    }

    public void testPrivateRegistryConnection() throws Exception{
        docker.pull(REGISTRY_FQDN + "/" + Conf.EMPTY_IMAGE_NAME);
    }

    public String sup(String subnet, String ipRange, String gateway) throws Exception{
        Ipam ipam = Ipam.builder().config(singletonList( IpamConfig.create(subnet, ipRange, gateway) )).driver("").build();
        NetworkConfig networkCfg = NetworkConfig.builder().ipam(ipam).build();
        return docker.createNetwork(networkCfg).id();
    }

//    /**
//     * Sends a stop command to the platform
//     * @param image Image copy to be stopped
//     */
//    public void stopExecution(ImageCopy image){
//        try {
//            docker.stopContainer(image.getPlatformExecutionID(), Docker.STOP_GRACE_PERIOD_SECONDS);
//        } catch (DockerException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Builds the image and creates a container with it.
//     * Sets the execution ID of image
//     * @param image Image copy to be registered
//     */
//    public void registerImage(ImageCopy image){
//        // TODO handle network/ports
//        try {
//            // Loads any accompanying tar files that were created with the "docker save" command
//            File[] images = image.getMainFile().getAbsoluteFile().getParentFile().listFiles(new FileFilter() {
//
//                public boolean accept(File pathname) {
//                    return pathname.isFile() && pathname.getName().endsWith(".tar");
//                }
//            });
//
//            for(File savedImage : images) {
//                InputStream imagePayload = new BufferedInputStream(new FileInputStream(savedImage));
//                docker.load(imagePayload);
//            }
//
//            // Attemps to create image from Dockerfile, has no effect if daemon already has the image
//            String imageID = docker.build(image.getMainFile().toPath().getParent(), null, image.getMainFile().getName(),
//                    new ProgressHandler() {
//
//                        public void progress(ProgressMessage arg0) throws DockerException {
//                            // Empty on purpose
//                        }
//                    },
//                    new DockerClient.BuildParam[]{});
//
//            // Publish all exposed ports to host
//            HostConfig hostCfg = HostConfig.builder().publishAllPorts(true).build();
//
//            // Set the image id and host configs
//            ContainerConfig contCfg = ContainerConfig.builder().image(imageID).hostConfig(hostCfg).build();
//            image.setPlatformExecutionID(docker.createContainer(contCfg).id());
//        } catch (DockerException | InterruptedException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Removes the container. The container must be stopped before it can be
//     * removed.
//     * @param image Image copy to be unregistered
//     */
//    public void unregisterImage(ImageCopy image){
//        try {
//            docker.removeContainer(image.getPlatformExecutionID());
//        } catch (DockerException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Sends a reset message to the platform
//     * @param image Image to be restarted
//     */
//    public void restartExecution(ImageCopy image) throws PlatformOperationException {
//        try {
//            docker.restartContainer(image.getPlatformExecutionID(), Docker.STOP_GRACE_PERIOD_SECONDS);
//        } catch (DockerException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//


//
//    /**
//     * Changes VM configuration
//     * @param cores new number of cores for the VM
//     * @param ram new RAM value for the VM
//     * @param image Copy to be modified
//     */
//    public void configureExecutionHardware(int cores, int ram, ImageCopy image) throws PlatformOperationException {
//        // TODO implement
//        System.out.println("TODO Docker:configureExecutionHardware");
//        //throw new UnsupportedOperationException();
//    }
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
//    /**
//     * Copy a file on the container's filesystem. The path must already exist in the container
//     * @param image copy in which file will be pasted
//     * @param destinationRoute route in which file will be pasted
//     * @param sourceFile file to be copied
//     */
//    public void copyFileOnExecution(ImageCopy image, String destinationRoute, File sourceFile) throws PlatformOperationException {
//        try {
//            //TODO Create dir
//            System.out.println("DockerCopyFile: File path= '"+sourceFile.getAbsolutePath()+"' destination= '"+destinationRoute+"'");
//            docker.copyToContainer(sourceFile.toPath(), image.getPlatformExecutionID(), destinationRoute);
//        } catch (DockerException | InterruptedException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Takes a snapshot of the container. Unsuported
//     * @param image copy of the image that will have the new snapshot
//     * @param snapshotname
//     */
//    public void takeExecutionSnapshot(ImageCopy image,String snapshotname){
//        System.out.println("TODO Docker:takeExecutionSnapshot");
//        //throw new UnsupportedOperationException();
//    }
//
//    /**
//     * Deletes a snapshot of the container. Unsuported
//     * @param image copy of the image to delete its snapshot
//     * @param snapshotname
//     */
//    public void deleteExecutionSnapshot(ImageCopy image,String snapshotname){
//        System.out.println("TODO Docker:deleteExecutionSnapshot");
//        //throw new UnsupportedOperationException();
//    }
//
//    /**
//     * Changes the Container's MAC address
//     * @param image copy to be modified
//     */
//    public void changeExecutionMac(ImageCopy image) throws PlatformOperationException {
//        // TODO implement
//        System.out.println("TODO Docker:changeExecutionMac");
//        //throw new UnsupportedOperationException();
//    }
//
//    /**
//     * Restores a container to its snapshot. Unsuported
//     * @param image copy to be reverted
//     * @param snapshotname snapshot to which image will be restored
//     */
//    public void restoreExecutionSnapshot(ImageCopy image, String snapshotname) throws PlatformOperationException {
//        System.out.println("TODO Docker:restoreExecutionSnapshot");
//        //throw new UnsupportedOperationException();
//    }
//
//    /**
//     * Verifies if the container has the specified snapshot. Unsuported
//     * @param image
//     * @param snapshotname
//     */
//    public boolean existsExecutionSnapshot(ImageCopy image, String snapshotname) throws PlatformOperationException {
//        System.out.println("TODO Docker:existsExecutionSnapshot");
//        //throw new UnsupportedOperationException();
//        return false;
//    }
//
//    /**
//     * Unregisters all VMs from platform
//     */
//    public void unregisterAllVms(){
//        try {
//            List<Container> containers = docker.listContainers(ListContainersParam.allContainers());
//            for(Container c : containers) {
//                docker.removeContainer(c.id());
//            }
//        } catch (DockerException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Clones an image making a new copy
//     * @param source source copy
//     * @param dest empty destination copy
//     */
//    public void cloneImage(ImageCopy source, ImageCopy dest) {
//        dest.setImage(source.getImage());
//        dest.setMainFile(source.getMainFile());
//    }
//
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
//    public boolean checkExecutionStarted(Execution execution) {
//        boolean running = false;
//
//        Outer: for(int t=0;t<8 && !running;t++){
//            try {
//                // Returns only the running containers
//                List<Container>	containers = docker.listContainers();
//                for(Container c : containers) {
//                    if(c.id().equals(execution.getImage().getPlatformExecutionID())) {
//                        running = true;
//                        break Outer;
//                    }
//                }
//
//                Thread.sleep(30000);
//            } catch (DockerException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        return running;
//    }
}
