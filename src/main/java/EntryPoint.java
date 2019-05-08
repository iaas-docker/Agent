import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntryPoint {

    final static Logger logger = LoggerFactory.getLogger(EntryPoint.class);

    //Initialize env vars with .env
    public final static Dotenv dotenv = Dotenv.load();

    public static void main(String[] args) {
        try {
            //Validate docker is running
            DockerManager dockerManager = new DockerManager();

            //Validate docker registry is reachable
            dockerManager.testPrivateRegistryConnection();
            logger.info("Registry contacted successfully");

            //Create connection to the queue


            while (true) {
                try {
//                    Socket s=serverSocket.accept();
//                    ExecutorService.executeRequestTask(new ClouderServerAttentionThread(s));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            logger.error("Error during start up", ex);
        }


    }
}
