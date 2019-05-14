package execution;

import models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.CrudService;
import util.Conf;
import util.IaaSActionConstants;
import util.PortRangeAssigner;

import java.util.List;

public class InstanceCoordinator {

    private static DockerManager dockerManager;
    private static InstanceCoordinator instance = new InstanceCoordinator();
    final static Logger logger = LoggerFactory.getLogger(InstanceCoordinator.class);

    private static CrudService<Instance> instanceCrud =
            new CrudService<>(IaaSActionConstants.INSTANCE_COLLECTION, Instance.class);

    private static CrudService<User> userCrud =
            new CrudService<>(IaaSActionConstants.USER_COLLECTION, User.class);

    private static CrudService<Image> imageCrud =
            new CrudService<>(IaaSActionConstants.IMAGE_COLLECTION, Image.class);

    private static CrudService<DockerImage> dockerImageCrud =
            new CrudService<>(IaaSActionConstants.DOCKER_IMAGE_COLLECTION, DockerImage.class);

    private static CrudService<PhysicalMachine> physicalMachineCrud =
            new CrudService<>(IaaSActionConstants.PHYSICAL_COLLECTION, PhysicalMachine.class);


    public void startInstance(Instance instance) throws Exception {
        Image actualImage = imageCrud.findById(instance.getImageId());
        DockerImage dockerImage = dockerImageCrud.findById(actualImage.getBackedById());
        PhysicalMachine physicalMachine = physicalMachineCrud.findById(instance.getPhysicalMachineId());
        User user = userCrud.findById(instance.getUserId());

        List<Integer> assignedRanges = PortRangeAssigner.getPortRange(physicalMachine.getAssignedRanges());

        dockerManager.pullImage(dockerImage.getTag(), user);

        String containerId = dockerManager.createContainer(dockerImage.getTag(), assignedRanges.get(assignedRanges.size()-1));

        dockerManager.startExecution(containerId);

        instance.setState(Conf.STARTED);
        instance.setStateMessage(Conf.STARTED_MESSAGE);
        instance.setContainerId(containerId);
        instance.setPortRangeStart( assignedRanges.get(assignedRanges.size()-1) );
        instanceCrud.update(instance.getId(), instance);

        physicalMachine.setAssignedRanges(assignedRanges);
        physicalMachineCrud.update(physicalMachine.getId(), physicalMachine);

        if (! instance.getImageId().equals(instance.getBaseImageId() ) ){
            //TODO: Delete image from repo
        }
    }

    public void stopInstance(Instance instance) throws Exception {
        dockerManager.stopExecution(instance.getContainerId());

        User user = userCrud.findById(instance.getUserId());
        String tag = dockerManager.createContainerTag(instance, user);
        String newDockerImage = dockerManager.commitContainer(instance, tag);
        System.out.println(newDockerImage);
        dockerManager.pushContainer(tag, user);

        dockerManager.removeContainer(instance.getContainerId());

        DockerImage dockerImage = new DockerImage(tag, "Paused", newDockerImage);
        dockerImage = dockerImageCrud.create(dockerImage);
        Image image = new Image(Conf.DOCKER_TYPE, dockerImage.getId());
        image = imageCrud.create(image);

        instance.setState(Conf.STOPPED);
        instance.setStateMessage(Conf.STOPPED_MESSAGE);
        instance.setImageId(image.getId());
        instanceCrud.update(instance.getId(), instance);

        freePhysicalResources(instance);
    }

    public void deleteInstance(Instance instance) throws Exception {
        dockerManager.stopExecution(instance.getContainerId());
        dockerManager.removeContainer(instance.getContainerId());

        instance.setState(Conf.DELETED);
        instance.setStateMessage(Conf.DELETED_MESSAGE);
        instanceCrud.update(instance.getId(), instance);

        freePhysicalResources(instance);
    }

    private void freePhysicalResources(Instance instance){
        PhysicalMachine physicalMachine = physicalMachineCrud.findById(instance.getPhysicalMachineId());
        physicalMachine.setFreeCores(physicalMachine.getFreeCores() + instance.getCores());
        physicalMachine.setFreeRam(physicalMachine.getFreeRam() + instance.getRam());
        physicalMachine.setFreeMemory(physicalMachine.getFreeMemory() + instance.getMemory());
        physicalMachineCrud.update(physicalMachine.getId(), physicalMachine);
    }

    public void restartInstance(Instance instance) {
        try {
            instance.setState(Conf.RESTARTING);
            instance.setStateMessage(Conf.RESTARTING_MESSAGE);
            instanceCrud.update(instance.getId(), instance);

            dockerManager.restartContainer(instance.getContainerId());

            instance.setState(Conf.STARTED);
            instance.setStateMessage(Conf.STARTED_MESSAGE);
            instanceCrud.update(instance.getId(), instance);
        } catch (Exception e){
          logger.error("Error restarting instance", e);
            instance.setStateMessage(e.getLocalizedMessage());
            instanceCrud.update(instance.getId(), instance);
        }
    }

    public static InstanceCoordinator getInstance(DockerManager dm){
        dockerManager = dm;
        return instance;
    }

}
