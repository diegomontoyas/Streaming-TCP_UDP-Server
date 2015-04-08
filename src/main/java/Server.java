import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 */
public class Server
{
    public static final int TCP_PORT = 5050;

    public static Hashtable<String, String> users = new Hashtable<String, String>();

    public static final Hashtable<String, String> channelList = new Hashtable<String, String>();
    public static final Hashtable<String, InetAddress> channelListGroupsIPS = new Hashtable<String, InetAddress>();
    public static final Hashtable<String, Integer> channelListGroupsUDPPorts = new Hashtable<String, Integer>();

    private ArrayList<StreamingThread> streamingThreads = new ArrayList<StreamingThread>();

    private static PrintWriter writer;

    public static void main(String[] args)
    {
        new Server().start();
    }

    public Server ()
    {
        try
        {
            writer = new PrintWriter("/Users/Diego/Desktop/logs.txt", "UTF-8");

            loadUsers();

            addVideoToChannelList("Charles Chaplin", "/Users/Diego/Desktop/charles.mp4", "239.255.255.250", 8881);
            addVideoToChannelList("Buster Keaton", "/Users/Diego/Desktop/Buster Keaton.mp4", "239.255.255.251", 8882);
            addVideoToChannelList("Honda", "/Users/Diego/Desktop/honda.mp4", "239.255.255.252", 8883);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void loadUsers ()
    {
        try
        {
            Properties properties = new Properties();
            InputStream stream = getClass().getClassLoader().getResourceAsStream("usuarios.properties");
            properties.load(stream);

            for (Object key:properties.keySet())
            {
                String username = (String)key;
                users.put(username, properties.getProperty(username));
            }
        }
        catch(Exception e)
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
                log("Client accepted at "+connectionSocket.getInetAddress());
                new TCPHandlerThread(connectionSocket).start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void log(String message)
    {
        writer.println(message);
    }
}
