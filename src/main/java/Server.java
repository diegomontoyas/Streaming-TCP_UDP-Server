import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 */
public class Server
{
    public static final int TCP_PORT = 5050;

    public static final Hashtable<String, String> channelList = new Hashtable<String, String>();
    public static final Hashtable<String, InetAddress> channelListGroupsIPS = new Hashtable<String, InetAddress>();
    public static final Hashtable<String, Integer> channelListGroupsUDPPorts = new Hashtable<String, Integer>();

    private ArrayList<StreamingThread> streamingThreads = new ArrayList<StreamingThread>();

    public static void main(String[] args)
    {
        new Server().start();
    }

    public Server ()
    {
        try
        {
            addVideoToChannelList("Honda", "/Users/Diego/Desktop/Honda.mp4", "239.255.255.250", 8881);
            addVideoToChannelList("Gorillaz", "/Users/Diego/Desktop/gorillaz.mp4", "239.255.255.251", 8882);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void addVideoToChannelList(String channelName, String fileRoute, String groupIP, Integer groupPort) throws UnknownHostException
    {
        channelList.put(channelName, fileRoute);
        channelListGroupsIPS.put(channelName, InetAddress.getByName(groupIP));
        channelListGroupsUDPPorts.put(channelName, groupPort);
    }

    private void dispatchStreamingThreads ()
    {
        try
        {
            for(String videoTitle: channelList.keySet())
            {
                StreamingThread streamingThread = new StreamingThread(channelList.get(videoTitle), channelListGroupsIPS.get(videoTitle), channelListGroupsUDPPorts.get(videoTitle));
                streamingThread.start();
                streamingThreads.add(streamingThread);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void start ()
    {
        try
        {
            dispatchStreamingThreads();

            ServerSocket welcomeSocket = new ServerSocket(TCP_PORT);

            while(true)
            {
                Socket connectionSocket = welcomeSocket.accept();
                new TCPHandlerThread(connectionSocket).start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
