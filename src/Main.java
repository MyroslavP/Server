
import java.io.*;
import java.net.*;


public class Main implements Runnable {

    public OutputStream output;
    public ServerSocket serverSocket;
    public Socket socket;

    @Override
    synchronized public void run() {
        try {
            serverSocket = new ServerSocket(5000);

            try {

                while (true) {
                    socket = serverSocket.accept();

                    System.out.println("New client connected");
                    new EchoThread(socket).start();

                }

            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        Main r1 = new Main();
        Thread t1 = new Thread(r1);

        t1.start();
    }
}
