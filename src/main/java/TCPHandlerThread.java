import javax.xml.transform.Source;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 */
public class TCPHandlerThread extends Thread
{
    private Socket socket;
    private InetAddress clientIP;

    private BufferedReader inFromClient;
    private DataOutputStream outToClient;

    private StreamingThread streamingThread;

    public TCPHandlerThread(Socket socket)
    {
        try
        {
            this.socket = socket;
            clientIP = socket.getInetAddress();

            inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToClient = new DataOutputStream(socket.getOutputStream());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        super.run();

        try
        {
            String credentials = clientSays();
            String[] usernamePassword = credentials.split(":");

            if (Server.users.get(usernamePassword[0])==null || !Server.users.get(usernamePassword[0]).equals(usernamePassword[1]))
            {
                Server.log(clientIP+ " failed to login: Incorrect credentials");
                sendResponse("INCORRECT CREDENTIALS");
                return;
            }

            Server.log(clientIP+ " logged in successfully");
            sendResponse("CREDENTIALS OK");

            if (clientSays("CHANNEL LIST PLEASE"))
            {
                String channelListResponse = "PLAYLIST=";

                for(String key: Server.channelList.keySet())
                {
                    channelListResponse += key + ",";
                }
                channelListResponse = channelListResponse.substring(0, channelListResponse.length()-1);

                sendResponse(channelListResponse);
            }

            //Everything set, ready to stream

            while (true)
            {
                String clientRequest = clientSays();

                if (clientRequest.equals("PLEASE PLAY"))
                {
                    //streamingThread.play();
                }
                else if (clientRequest.equals("PLEASE PAUSE"))
                {
                    //streamingThread.pause();
                }
                else if (clientRequest.startsWith("PLEASE GIVE ME INFO FOR="))
                {
                    //dispatchStreamingThread(clientSays.split("=")[1]);

                    String videoName = clientRequest.split("=")[1];

                    sendResponse(Server.channelListGroupsIPS.get(videoName).getHostName()+":"+Server.channelListGroupsUDPPorts.get(videoName));

                    Server.log(clientIP+ " tuned in to channel "+videoName);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            try
            {
                socket.close();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
    }

    private String clientAnswersValueToKey (String key) throws Exception
    {
        String clientResponse = inFromClient.readLine();
        String[] components = clientResponse.split("=");

        if (!components[0].equals(key)) throw new Exception("Client didn't say expected key");

        return components[1];
    }

    private boolean clientSays (String value) throws Exception
    {
        String clientResponse = inFromClient.readLine();
        return clientResponse.equals(value);
    }

    private String clientSays () throws Exception
    {
        return inFromClient.readLine();
    }

    public void sendResponse (String value) throws IOException
    {
        outToClient.writeBytes(value);
    }

    /*public void dispatchStreamingThread (String videoTitle)
    {
        try
        {
            if (streamingThread != null)
            {
                streamingThread.die();
                streamingThread = null;
            }

            streamingThread = new StreamingThread(Server.channelList.get(videoTitle), clientIP, clientUDPPort);
            streamingThread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }*/
}
