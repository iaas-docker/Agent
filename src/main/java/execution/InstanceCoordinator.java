package execution;

import models.DockerImage;
import models.Image;
import models.Instance;
import persistence.CrudService;
import util.Conf;
import util.IaaSConstants;

public class InstanceCoordinator {

    private static DockerManager dockerManager;
    private static InstanceCoordinator instance = new InstanceCoordinator();

    private static CrudService<Instance> instanceCrud =
            new CrudService<>(IaaSConstants.INSTANCE_COLLECTION, Instance.class);

    private static CrudService<Image> imageCrud =
            new CrudService<>(IaaSConstants.IMAGE_COLLECTION, Image.class);

    private static CrudService<DockerImage> dockerImageCrud =
            new CrudService<>(IaaSConstants.DOCKER_IMAGE_COLLECTION, DockerImage.class);


    public void startInstance(Instance instance) throws Exception{
        Image actualImage = imageCrud.findById(instance.getImageId());
        DockerImage dockerImage = dockerImageCrud.findById(actualImage.getBackedById());

        dockerManager.pullImage(dockerImage.getTag());

        String containerId = dockerManager.createContainer(dockerImage.getTag());

        dockerManager.startExecution(containerId);
        instance.setState(Conf.STARTED);
        instance.setContainerId(containerId);

        if (! instance.getImageId().equals(instance.getBaseImageId() ) ){
            //TODO: Delete image from repo
        }
    }

    public static InstanceCoordinator getInstance(DockerManager dm){
        dockerManager = dm;
        return instance;
    }

}
