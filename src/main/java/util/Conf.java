package util;

import init.EntryPoint;
import io.github.cdimascio.dotenv.Dotenv;

public class Conf {

//    EntryPoint.
    private final static Dotenv dotenv = Dotenv.load();

    //Collection names
    public static final String REGISTRY_FQDN = dotenv.get("REGISTRY_FQDN");
    public static final String AWS_ACCESS_KEY = dotenv.get("AWS_ACCESS_KEY");
    public static final String AWS_ACCESS_SECRET = dotenv.get("AWS_ACCESS_SECRET");
    public static final String EMPTY_IMAGE_NAME = dotenv.get("EMPTY_IMAGE_NAME");
    public static final String QUEUE_URL = dotenv.get("QUEUE_URL");

    //Instance states
    public static final String STARTED = "STARTED";
    public static final String DELETED = "DELETED";
    public static final String RESTARTING = "RESTARTING";

    //Instance states messages
    public static final String STARTED_MESSAGE = "Instance successfully started";
    public static final String DELETED_MESSAGE = "Instance successfully deleted";
    public static final String RESTARTING_MESSAGE = "Instance restarting";


    //Amount of exposed ports
    public static final Integer AMOUNT_EXPOSED_PORTS = 100;

    //Container stop period
    public static final Integer STOP_GRACE_PERIOD_SECONDS= 15;

}
