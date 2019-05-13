package execution;

import models.DockerImage;
import models.Image;
import models.Instance;
import models.PhysicalMachine;
import persistence.CrudService;
import util.Conf;
import util.IaaSConstants;
import util.PortRangeAssigner;

import java.util.List;

public class InstanceCoordinator {

    private static DockerManager dockerManager;
    private static InstanceCoordinator instance = new InstanceCoordinator();

    private static CrudService<Instance> instanceCrud =
            new CrudService<>(IaaSConstants.INSTANCE_COLLECTION, Instance.class);

    private static CrudService<Image> imageCrud =
            new CrudService<>(IaaSConstants.IMAGE_COLLECTION, Image.class);

    private static CrudService<DockerImage> dockerImageCrud =
            new CrudService<>(IaaSConstants.DOCKER_IMAGE_COLLECTION, DockerImage.class);

    private static CrudService<PhysicalMachine> physicalMachineCrud =
            new CrudService<>(IaaSConstants.PHYSICAL_COLLECTION, PhysicalMachine.class);


    public void startInstance(Instance instance) throws Exception{
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
        instanceCrud.update(instance.getId(), instance);

        physicalMachine.setAssignedRanges(assignedRanges);
        physicalMachineCrud.update(physicalMachine.getId(), physicalMachine);

        if (! instance.getImageId().equals(instance.getBaseImageId() ) ){
            //TODO: Delete image from repo
        }
    }

    public static InstanceCoordinator getInstance(DockerManager dm){
        dockerManager = dm;
        return instance;
    }

}
