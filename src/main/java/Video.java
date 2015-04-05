import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.MemoryDataSourceImpl;
import org.bytedeco.javacv.*;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.AutoFileChannelWrapper;
import org.jcodec.common.FileChannelWrapper;
import org.jcodec.common.NIOUtils;
import org.jcodec.movtool.streaming.tracks.SeekableByteChannelWrapper;

import java.awt.image.BufferedImage;
import java.io.File;
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
    private double duration;

    public Video(String file)
    {
        try
        {
            this.file = file;

            Path path = Paths.get(file);
            data = Files.readAllBytes(path);

            IsoFile isoFile = new IsoFile(new MemoryDataSourceImpl(data));
            duration = (double)isoFile.getMovieBox().getMovieHeaderBox().getDuration() / isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public VideoIterator iterator ()
    {
        return new VideoIterator();
    }

    public class VideoIterator
    {
        FFmpegFrameGrabber grabber;
        FileChannelWrapper wrapper;
        int currentFrame = 0;

        private VideoIterator ()
        {
            try
            {
                grabber = new FFmpegFrameGrabber(file);
                grabber.start();

                //wrapper = NIOUtils.readableFileChannel(file);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
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
