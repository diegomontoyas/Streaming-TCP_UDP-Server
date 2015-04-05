import java.net.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 */
public class Server
{
    public static final int TCP_PORT = 5050;

    ServerSocket TCPSocket;

    public static void main(String[] args)
    {
        new Server().start();
    }

    public Server ()
    {

    }

    public void start ()
    {
        try
        {
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
