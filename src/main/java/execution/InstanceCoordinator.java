package execution;

import init.AgentDaemon;
import models.DockerImage;
import models.Image;
import models.Instance;
import models.PhysicalMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.CrudService;
import util.Conf;
import util.IaaSConstants;
import util.PortRangeAssigner;

import java.util.List;

public class InstanceCoordinator {

    private static DockerManager dockerManager;
    private static InstanceCoordinator instance = new InstanceCoordinator();
    final static Logger logger = LoggerFactory.getLogger(InstanceCoordinator.class);

    private static CrudService<Instance> instanceCrud =
            new CrudService<>(IaaSConstants.INSTANCE_COLLECTION, Instance.class);

    private static CrudService<Image> imageCrud =
            new CrudService<>(IaaSConstants.IMAGE_COLLECTION, Image.class);

    private static CrudService<DockerImage> dockerImageCrud =
            new CrudService<>(IaaSConstants.DOCKER_IMAGE_COLLECTION, DockerImage.class);

    private static CrudService<PhysicalMachine> physicalMachineCrud =
            new CrudService<>(IaaSConstants.PHYSICAL_COLLECTION, PhysicalMachine.class);


    public void startInstance(Instance instance) throws Exception {
        Image actualImage = imageCrud.findById(instance.getImageId());
        DockerImage dockerImage = dockerImageCrud.findById(actualImage.getBackedById());
        PhysicalMachine physicalMachine = physicalMachineCrud.findById(instance.getPhysicalMachineId());

        List<Integer> assignedRanges = PortRangeAssigner.getPortRange(physicalMachine.getAssignedRanges());

        dockerManager.pullImage(dockerImage.getTag());

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

    public void deleteInstance(Instance instance) throws Exception {
        dockerManager.stopExecution(instance.getContainerId());
        dockerManager.removeContainer(instance.getContainerId());

        instance.setState(Conf.DELETED);
        instance.setStateMessage(Conf.DELETED_MESSAGE);
        instanceCrud.update(instance.getId(), instance);

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
