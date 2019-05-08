import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

public class SQSManager {

    private static StaticCredentialsProvider awsCreds = StaticCredentialsProvider
            .create(AwsBasicCredentials
                    .create(EntryPoint.dotenv.get("AWS_ACCESS_KEY"), EntryPoint.dotenv.get("AWS_ACCESS_SECRET")));

    final static Logger logger = LoggerFactory.getLogger(SQSManager.class);

    private String queueUrl;

    SqsClient sqsClient;

    public SQSManager(String queueUrl){
        sqsClient = SqsClient
                .builder()
                .region(Region.SA_EAST_1)
                .credentialsProvider(awsCreds)
                .build();
        this.queueUrl = queueUrl;
    }

    public void receive(){
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(this.queueUrl)
                .maxNumberOfMessages(1)
                .visibilityTimeout(10)
                .build();

        List<Message> messages= sqsClient.receiveMessage(receiveMessageRequest).messages();

        logger.info("Got {} messages.", messages.size());
    }
}
