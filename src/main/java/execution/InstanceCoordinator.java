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

        dockerImageCrud.findById(actualImage)

        dockerManager.startExecution(Conf.REGISTRY_FQDN +"/"+ instance.getImageId());
        instance.setState("STARTED");

        if (! instance.getImageId().equals(instance.getBaseImageId() ) ){
            System.out.println("delete from repo");
        }
    }

    public static InstanceCoordinator getInstance(DockerManager dm){
        dockerManager = dm;
        return instance;
    }

}
