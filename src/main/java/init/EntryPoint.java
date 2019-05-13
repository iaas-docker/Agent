package init;

import exceptions.AgentExecutionException;
import execution.DockerManager;
import execution.SQSManager;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.CrudService;
import util.Conf;

public class EntryPoint {

    final static Logger logger = LoggerFactory.getLogger(EntryPoint.class);

    public static void main(String[] args) {
        initializeAgent();
    }

    private static void initializeAgent(){
        try {
            //Validate docker is running
            DockerManager dockerManager = new DockerManager();

            //Validate docker registry is reachable
            dockerManager.testPrivateRegistryConnection();
            logger.info("Connected successfully registry");

            //Create connection to the queue
            SQSManager sqsManager = new SQSManager(Conf.QUEUE_URL);
            logger.info("Connected successfully to SQS queue");

            //Test Mongo DB connection
            CrudService.testConnection();
            logger.info("Connected successfully to Mongo DB");

            AgentDaemon daemon = AgentDaemon.instance(sqsManager, dockerManager);
            daemon.execute();
        } catch (AgentExecutionException aex) {
            initializeAgent();
        } catch (Exception ex) {
            logger.error("Error during start up, killing agent.", ex);
        }
    }
}
