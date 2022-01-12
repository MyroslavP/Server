import com.google.common.collect.Table;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Socket;
import java.net.URL;
public class EchoThread extends Thread {

    protected Socket socket;
    OutputStream output;

    public EchoThread(Socket clientSocket) {
        this.socket = clientSocket;
    }
    public void run() {

        try {
            output = socket.getOutputStream();

            Authenticator.setDefault(new Authenticator() {

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("admin", "Vlada123".toCharArray());
                }
            });
               Thread t1 = new Thread(new MjpegRunner(output, new URL("http://192.168.0.211//video/mjpg.cgi")));
               t1.start();

               Thread t2 = new Thread(new MjpegRunner(output, new URL("http://192.168.0.180//video/mjpg.cgi")));
               t2.start();

        } catch (IOException e) {
            return;
        }
    }
}



