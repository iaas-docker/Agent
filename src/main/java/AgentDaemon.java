import exceptions.AgentExecutionException;
import models.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;
import util.EPJson;

import java.util.Map;

public class AgentDaemon {

    final static Logger logger = LoggerFactory.getLogger(AgentDaemon.class);

    private DockerManager dockerManager;

    private SQSManager sqsManager;

    private static AgentDaemon instance;

    public AgentDaemon(SQSManager sqsManager, DockerManager dockerManager){
        this.sqsManager = sqsManager;
        this.dockerManager = dockerManager;
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
//                System.out.println(instance.getCores());
//                System.out.println(instance.getMemory());
//                System.out.println(instance.getRam());
                System.out.println(instance.getId());

                System.out.println();
//                System.out.println(newMessage.attributes());

//                switch (clouderServerRequest.getMainOp()) {
//            case UnaCloudAbstractMessage.EXECUTION_OPERATION:
//                oos.writeObject(attendExecutionOperation(clouderServerRequest,ois,oos));
//                break;
//            case UnaCloudAbstractMessage.PHYSICAL_MACHINE_OPERATION:
//                oos.writeObject(attendPhysicalMachineOperation(clouderServerRequest));
//                break;
//            case UnaCloudAbstractMessage.AGENT_OPERATION:
//                oos.writeObject(attendAgentOperation(clouderServerRequest));
//                break;
//            default:
//                throw new Exception("The requested operation is not yet supported.");
//                break;
//        }
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
