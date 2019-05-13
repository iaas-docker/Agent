package execution;

import init.EntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import util.Conf;

import java.util.List;

public class SQSManager {

    private static StaticCredentialsProvider awsCredentials = StaticCredentialsProvider
            .create(AwsBasicCredentials.create(Conf.AWS_ACCESS_KEY, Conf.AWS_ACCESS_SECRET));

    final static Logger logger = LoggerFactory.getLogger(SQSManager.class);

    private String queueUrl;

    SqsClient sqsClient;

    public SQSManager(String queueUrl){
        sqsClient = SqsClient
                .builder()
                .region(Region.SA_EAST_1)
                .credentialsProvider(awsCredentials)
                .build();
        this.queueUrl = queueUrl;
    }

    public Message receiveSingleMessage(){
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(this.queueUrl)
                .maxNumberOfMessages(1)
                .visibilityTimeout(Conf.DEFAULT_VISIBILITY_SECONDS)
                .attributeNamesWithStrings("MessageGroupId")
                .build();

        List<Message> messages= sqsClient.receiveMessage(receiveMessageRequest).messages();

        if (messages.size() == 1){
            return messages.get(0);
        } else {
            return null;
        }
    }

    public void deleteMessage(String queueUrl, String messageReceiptHandle){
        DeleteMessageRequest dmr = DeleteMessageRequest
                .builder()
                .queueUrl(queueUrl)
                .receiptHandle(messageReceiptHandle)
                .build();
        sqsClient.deleteMessage(dmr);
    }
}
