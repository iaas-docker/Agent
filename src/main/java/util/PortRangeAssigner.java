package util;

import javax.net.ServerSocketFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.*;

public class PortRangeAssigner {

    private static final Integer START_PORT = 30000;
    private static final Integer AMOUNT_RANGE = Conf.AMOUNT_EXPOSED_PORTS;

    /**
     *
     * @param assignedRanges, is not null
     * @return
     */
    public static List<Integer> getPortRange(List<Integer> assignedRanges){
        assignedRanges.add(START_PORT);
        TreeSet<Integer> rangesSet = new TreeSet<>(assignedRanges);
        Integer startRange = rangesSet.last() + AMOUNT_RANGE;
        while (!isPortRangeAvailable(startRange, startRange + AMOUNT_RANGE)){
            startRange += AMOUNT_RANGE;
        }
        rangesSet.add(startRange);
        return new ArrayList<> (rangesSet);
    }

    /**
     *
     * @param start inclusive
     * @param end not inclusive
     * @return
     */
    private static boolean isPortRangeAvailable(int start, int end){
        boolean isAvailable = true;
        for (int i = start; i < end && isAvailable; i++) {
            isAvailable = isPortAvailable(i);
        }
        return isAvailable;
    }

    private static boolean isPortAvailable(int port) {
        try {
            ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(
                    port, 1, InetAddress.getByName("localhost"));
            serverSocket.close();
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }
}
