import javax.xml.transform.Source;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 */
public class TCPHandlerThread extends Thread
{
    private Socket socket;
    private InetAddress clientIP;
    private int clientUDPPort;

    public TCPHandlerThread(Socket socket)
    {
        try
        {
            this.socket = socket;
            clientIP = socket.getInetAddress();
            clientIP = InetAddress.getLocalHost();
            clientUDPPort = 4040;
        }
        catch (Exception e){ e.printStackTrace(); }
    }

    @Override
    public void run()
    {
        super.run();

        try
        {
            String clientSays, response;

            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());

            clientSays = inFromClient.readLine();

            outToClient.writeBytes("USER ACK");

            clientSays = inFromClient.readLine();

            outToClient.writeBytes("CREDENTIALS OK");

            clientSays = inFromClient.readLine();

            outToClient.writeBytes("PORT ACK");

            //Everything set, ready to stream
            dispatchStreamingThread();

            while (true)
            {
                clientSays = inFromClient.readLine();

                if (clientSays.equals("PAUSE PLEASE"))
                {

                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void dispatchStreamingThread ()
    {
        new StreamingThread("/Users/Diego/Desktop/Honda Ad H264.mp4", clientIP, clientUDPPort).start();
    }
}
