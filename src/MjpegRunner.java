import java.awt.image.BufferedImage;
import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;


 public class MjpegRunner extends Thread
{
    private static final String CONTENT_LENGTH = "Content-length: ";
    private static final String CONTENT_TYPE = "Content-type: image/jpeg";
    public OutputStream outputStream;
    public InputStream urlStream;
    public StringWriter stringWriter;
    public boolean processing = true;
    public static int MAGIC = 0xABBABABA;
    public byte type;
    public String urls;
    public Lock lock = new ReentrantLock();


    public MjpegRunner(OutputStream outputStream, URL url) throws IOException
    {
            urls = url.toString();
            this.outputStream = outputStream;
            URLConnection urlConn = url.openConnection();
            urlConn.setReadTimeout(20000);
            urlConn.connect();
            urlStream = urlConn.getInputStream();
            stringWriter = new StringWriter(128);
    }

    @Override
    public void run()
    {
        printTable();
    }

    synchronized void printTable(){

        while(processing)
        {
            try
            {
                byte[] imageBytes = retrieveNextImage();
                ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                BufferedImage image = ImageIO.read(bais);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(image,"jpg",bos);


                byte[] s = ByteBuffer.allocate(4).putInt(MAGIC).array();
                byte[] b = ByteBuffer.allocate(4).putInt(bos.size() + 9).array();


                System.out.println("[");

                outputStream.write(s);

                System.out.println("Sending " +  "(" + bos.size() + " bytes)");


                outputStream.write(b);

                if (urls.equals("http://192.168.0.211//video/mjpg.cgi"))
                {
                    outputStream.write(type = 'a');
                }
                else if (urls.equals("http://192.168.0.180//video/mjpg.cgi"))
                {
                    outputStream.write(type = 'b');
                }

                outputStream.write(bos.toByteArray());

                System.out.println("]");

            }catch(SocketTimeoutException ste){
                System.err.println("failed stream read time: " + ste);

            }catch(IOException e){
                System.err.println("failed stream read: " + e);
            }
        }

        try
        {
            urlStream.close();
        }catch(IOException ioe){
            System.err.println("Failed to close the stream: " + ioe);
        }
    }

    private  byte[] retrieveNextImage() throws IOException
    {
        boolean haveHeader = false;
        int currByte = -1;

        String header = null;
        while((currByte = urlStream.read()) > -1 && !haveHeader)
        {
            stringWriter.write(currByte);

            String tempString = stringWriter.toString();
            int indexOf = tempString.indexOf(CONTENT_TYPE);
            if(indexOf > 0)
            {
                haveHeader = true;
                header = tempString;
            }
        }

        while((urlStream.read()) != 255)
        {

        }

        int contentLength = contentLength(header);
        byte[] imageBytes = new byte[contentLength + 1];
        imageBytes[0] = (byte)255;
        int offset = 1;
        int numRead = 0;
        while (offset < imageBytes.length
                && (numRead=urlStream.read(imageBytes, offset, imageBytes.length-offset)) >= 0)
        {
            offset += numRead;
        }

        stringWriter = new StringWriter(128);

        return imageBytes;
    }

    private  static int contentLength(String header)
    {
        int indexOfContentLength = header.indexOf(CONTENT_LENGTH);
        int valueStartPos = indexOfContentLength + CONTENT_LENGTH.length();
        int indexOfEOL = header.indexOf('\n', indexOfContentLength);

        String lengthValStr = header.substring(valueStartPos, indexOfEOL).trim();

        int retValue = Integer.parseInt(lengthValStr);

        return retValue;
    }
}