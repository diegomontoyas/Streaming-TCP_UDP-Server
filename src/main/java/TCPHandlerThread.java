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
    private int clientUDPPort;

    private BufferedReader inFromClient;
    private DataOutputStream outToClient;

    private StreamingThread streamingThread;

    private Hashtable<String, String> playlist;

    public TCPHandlerThread(Socket socket)
    {
        try
        {
            this.socket = socket;
            clientIP = socket.getInetAddress();
            clientUDPPort = 4040;

            inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToClient = new DataOutputStream(socket.getOutputStream());

            playlist = new Hashtable<String, String>();
            playlist.put("Honda AD", "/Users/Diego/Desktop/Honda Ad H264.mp4");
            playlist.put("Gorillaz", "/Users/Diego/Desktop/gorillaz.mp4");
        }
        catch (Exception e){ e.printStackTrace(); return; }
    }

    @Override
    public void run()
    {
        super.run();

        try
        {
            clientAnswersValueToKey("USER");

            sendResponse("USER ACK");

            clientAnswersValueToKey("PASSWORD");

            sendResponse("CREDENTIALS OK");

            clientUDPPort = Integer.parseInt(clientAnswersValueToKey("PORT"));

            sendResponse("PORT ACK");

            if (clientSays("PLAYLIST PLEASE"))
            {
                String playlistResponse = "PLAYLIST=";

                for(String key: playlist.keySet())
                {
                    playlistResponse += key + ",";
                }
                playlistResponse = playlistResponse.substring(0, playlistResponse.length()-1);

                sendResponse(playlistResponse);
            }

            //Everything set, ready to stream

            while (true)
            {
                String clientSays = clientSays();

                if (clientSays.equals("PLAY PLEASE"))
                {
                    streamingThread.play();
                }
                else if (clientSays.equals("PAUSE PLEASE"))
                {
                    streamingThread.pause();
                }
                else if (clientSays.startsWith("PLAY="))
                {
                    dispatchStreamingThread(clientSays.split("=")[1]);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
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

    public void dispatchStreamingThread (String videoTitle)
    {
        if (streamingThread != null)
        {
            streamingThread.die();
            streamingThread = null;
        }

        streamingThread = new StreamingThread(playlist.get(videoTitle), clientIP, clientUDPPort);
        streamingThread.start();
    }
}
