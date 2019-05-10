package init;

import exceptions.AgentExecutionException;
import execution.DockerManager;
import execution.InstanceCoordinator;
import execution.SQSManager;
import models.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.model.Message;
import util.Conf;
import util.EPJson;
import util.IaaSConstants;

public class AgentDaemon {

    final static Logger logger = LoggerFactory.getLogger(AgentDaemon.class);

    private InstanceCoordinator instanceCoordinator;

    private SQSManager sqsManager;

    private static AgentDaemon instance;

    public AgentDaemon(SQSManager sqsManager, DockerManager dockerManager){
        this.sqsManager = sqsManager;
        this.instanceCoordinator = InstanceCoordinator.getInstance(dockerManager);
    }

    public void execute() throws Exception {
        while (true) {
            try {
                Message newMessage = sqsManager.receiveSingleMessage();
                if (newMessage == null) {
                    continue;
                }

                String action = newMessage.attributesAsStrings().get("MessageGroupId");
                Instance instance = EPJson.objectAs(newMessage.body(), Instance.class);

                switch (action) {
                    case IaaSConstants.START_INSTANCE:
                        instanceCoordinator.startInstance(instance);
                        break;
                    case IaaSConstants.STOP_INSTANCE:
//                        oos.writeObject(attendPhysicalMachineOperation(clouderServerRequest));
                        break;
                    default:
                        throw new Exception("The requested operation is not yet supported.");
                }

                sqsManager.deleteMessage(Conf.QUEUE_URL, newMessage.receiptHandle());

            } catch (Exception ex) {
                logger.error("Error at execution time, will restart agent.", ex);
                throw new AgentExecutionException(ex.getLocalizedMessage());
            }
        }
    }

    public static AgentDaemon instance(SQSManager sqsManager, DockerManager dockerManager){
        if (instance == null){
            instance = new AgentDaemon(sqsManager, dockerManager);
        }
        return instance;
    }

}
