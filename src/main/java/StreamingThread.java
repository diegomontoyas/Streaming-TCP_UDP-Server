import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private InetAddress groupIP;
    private int groupUDPPort;

    private final Object lock = new Object();

    private AtomicBoolean playing = new AtomicBoolean(false);

    private boolean shouldDie = false;

    DatagramSocket UDPSocket;

    public StreamingThread(String file, InetAddress groupIP, int groupUDPPort) throws IOException
    {
        System.out.println("Preparing to stream "+ file);
        video = new Video(file);
        this.groupIP = groupIP;
        this.groupUDPPort = groupUDPPort;
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

    public void die ()
    {
        shouldDie = true;
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
                if (shouldDie) return;

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

                DatagramPacket packet = new DatagramPacket(frameData, frameData.length, groupIP, groupUDPPort);
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
