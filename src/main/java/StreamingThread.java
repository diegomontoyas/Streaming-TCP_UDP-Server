import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 */
public class StreamingThread extends Thread
{
    public static final double FRAMERATE = 30;

    private Video video;
    private InetAddress clientIP;
    private int clientUDPPort;

    DatagramSocket UDPSocket;

    public StreamingThread(String file, InetAddress clientIP, int clientUDPPort)
    {
        video = new Video(file);
        this.clientIP = clientIP;
        this.clientUDPPort = clientUDPPort;
    }

    @Override
    public void run()
    {
        super.run();

        try
        {
            UDPSocket = new DatagramSocket(Server.UDP_PORT);

            Video.VideoIterator iterator = video.iterator();

            BufferedImage currentFrame = iterator.getNextFrame();

            while (currentFrame != null)
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(currentFrame, "jpg", baos);
                byte[] frameData = baos.toByteArray();

                DatagramPacket packet = new DatagramPacket(frameData, frameData.length, clientIP, clientUDPPort);
                UDPSocket.send(packet);

                currentFrame = iterator.getNextFrame();

                Thread.sleep((long)((1/FRAMERATE)*1000));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
