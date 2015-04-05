import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private final Object lock = new Object();

    private AtomicBoolean playing = new AtomicBoolean(false);

    DatagramSocket UDPSocket;

    public StreamingThread(String file, InetAddress clientIP, int clientUDPPort)
    {
        video = new Video(file);
        this.clientIP = clientIP;
        this.clientUDPPort = clientUDPPort;
    }

    public void play ()
    {
        playing.set(true);

        synchronized (lock)
        {
            lock.notify();
        }
    }

    public void pause ()
    {
        playing.set(false);
    }

    @Override
    public void run()
    {
        super.run();

        try
        {
            UDPSocket = new DatagramSocket();

            playing.set(true);

            Video.VideoIterator iterator = video.iterator();

            BufferedImage currentFrame = iterator.getNextFrame();

            while (currentFrame != null)
            {
                while (!playing.get())
                {
                    synchronized (lock)
                    {
                        lock.wait();
                    }
                }

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
