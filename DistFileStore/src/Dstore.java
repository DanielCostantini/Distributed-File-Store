import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Dstore {

    static int port;
    static int cport;
    static int timeout;
    static String file_folder;

    private static Socket cSocket = null;

    public static void main(String[] args) {

        port = Integer.parseInt(args[0]);
        cport = Integer.parseInt(args[1]);
        timeout = Integer.parseInt(args[2]);
        file_folder = args[3];

        clearFolder(file_folder);
        makeFolder(file_folder);

        try {

            // Create controller socket
            InetAddress address = InetAddress.getLocalHost();
            cSocket = new Socket(address, cport);
            System.out.println("Connection established to port " + cSocket.getPort());

            //Create thread to listen on cport
            new Thread(new DstoreControllerThread(cSocket)).start();

            // Listens on port for any new connections from clients
            listenForClients();

        } catch (Exception e) {

            System.err.println("error: " + e);

        }

    }

    private static void clearFolder(String file_folder) {

        File dir = new File(file_folder);

        if (dir.exists()) {

            String[] files = dir.list();

            if (files != null) {

                for (String filename : files) {

                    File file = new File(dir + "/" + filename);
                    if (file.isDirectory())
                        clearFolder(file_folder + "/" + filename);
                    else
                        file.delete();

                }

            }

            dir.delete();

        }


    }

    private static void makeFolder(String file_folder) {

        File dir = new File(file_folder);
        dir.mkdir();

    }

    private static void listenForClients() {

        ServerSocket server = null;

        try {

            server = new ServerSocket(port);

            while (true) {

                // When a new socket connects creates a new thread to listen on
                Socket socket = server.accept();
                System.out.println("Connecting port " + socket.getPort());
                new Thread(new DstoreClientThread(cSocket, socket)).start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (server != null) {

                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

    }

}

