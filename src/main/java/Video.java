import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.MemoryDataSourceImpl;
import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncodingAttributes;
import org.bytedeco.javacv.*;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.AutoFileChannelWrapper;
import org.jcodec.common.FileChannelWrapper;
import org.jcodec.common.NIOUtils;
import org.jcodec.movtool.streaming.tracks.SeekableByteChannelWrapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.Buffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 */
public class Video
{
    private String file;
    private double framerate;
    private byte[] data;
    private File audioTempFile;
    private double duration;

    public Video(String file) throws IOException
    {
        this.file = file;

        Path path = Paths.get(file);
        data = Files.readAllBytes(path);

        IsoFile isoFile = new IsoFile(new MemoryDataSourceImpl(data));
        duration = (double)isoFile.getMovieBox().getMovieHeaderBox().getDuration() / isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
    }

    public VideoIterator iterator ()
    {
        return new VideoIterator();
    }

    public class VideoIterator
    {
        private FFmpegFrameGrabber grabber;
        private FileChannelWrapper wrapper;
        private int currentFrame = 0;

        private final int AUDIO_SAMPLE_SIZE = 100000;

        private long audioFileSize = 0;
        private int audioBytesCount = 0;

        private FileInputStream audioStream = null;

        private VideoIterator ()
        {
            try
            {
                grabber = new FFmpegFrameGrabber(file);
                grabber.start();


                audioFileSize = audioTempFile.length();
                audioStream = new FileInputStream(audioTempFile);

                //wrapper = NIOUtils.readableFileChannel(file);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        private void extractAudio ()
        {
            try
            {
                File source = new File(file);
                File target = File.createTempFile("tmp", ".tmp");
                AudioAttributes audio = new AudioAttributes();
                audio.setCodec("libmp3lame");
                audio.setBitRate(new Integer(128000));
                audio.setChannels(new Integer(2));
                audio.setSamplingRate(new Integer(44100));
                EncodingAttributes attrs = new EncodingAttributes();
                attrs.setFormat("mp3");
                attrs.setAudioAttributes(audio);
                Encoder encoder = new Encoder();
                encoder.encode(source, target, attrs);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        public byte[] getNextAudioSample ()
        {
            try
            {
                if (audioBytesCount < audioFileSize)
                {
                    byte[] buf = new byte[AUDIO_SAMPLE_SIZE];

                    int audioBytesRead = audioStream.read(buf, 0, AUDIO_SAMPLE_SIZE);
                    audioBytesCount += audioBytesRead;
                    return buf;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        public BufferedImage getNextFrame ()
        {
            try
            {
                /*System.out.println("Frame "+ currentFrame);
                BufferedImage frame = FrameGrab.getFrame(wrapper, currentFrame++);
                return frame;*/
                return grabber.grab().getBufferedImage();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }
}
