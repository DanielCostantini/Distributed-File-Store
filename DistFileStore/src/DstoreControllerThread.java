import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;

public class DstoreControllerThread extends ThreadedSocket implements Runnable {

    public DstoreControllerThread(Socket socket) throws IOException {
        super(socket);

    }

    @Override
    public void run() {

        //Sends join message to controller
        pw.println("JOIN " + Dstore.port);

        try {

            // Forever listens for messages from the controller
            while (true) {

                String msg = br.readLine();

                if (msg != null)
                    messageReceived(msg);

            }

        } catch (Exception e) {

            System.err.println(e);

        }

    }

    protected void messageReceived(String msg) throws Exception {

        System.out.println("Message received from controller: " + msg);

        if (msg.contains("REMOVE")) {

            String filename = getWord(msg, 1);
            pw.println(deleteFile(filename));

        } else if (msg.contains("LIST")) {

            pw.println(getFileList());

        } else if (msg.contains("REBALANCE")) {

            String[] strs = msg.split(" ");
            ArrayList<String> words = new ArrayList<>();

            for (String str : strs) {

                words.add(str);

            }

            rebalance(words);

        } else {

            throw MalformedException;

        }

    }

    private void rebalance(ArrayList<String> words) {

        words.remove(0);
        int numFilesSend = Integer.parseInt(words.remove(0));

        for (int i = 0; i < numFilesSend; i++) {

            try {
                String filename = words.remove(0);
                int fileSize = getFileSize(filename);
                int numToSend = Integer.parseInt(words.remove(0));
                byte[] data = getFileData(filename);

                for (int j = 0; j < numToSend; j++) {

                    int port = Integer.parseInt(words.remove(0));

                    try {
                        sendFileToPort(port, filename, data, fileSize);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        int numRemove = Integer.parseInt(words.get(0));
        words.remove(0);

        for (int i = 0; i < numRemove; i++) {

            try {
                deleteFile(words.get(i));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        pw.println("REBALANCE_COMPLETE");

    }

    private void sendFileToPort(int port, String filename, byte[] data, int fileSize) throws IOException {


        // Create controller socket
        InetAddress address = InetAddress.getLocalHost();
        Socket socket = new Socket(address, port);
        System.out.println("Connection established to port " + socket.getPort());

        PrintWriter portpw = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader portbr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        portpw.println("REBALANCE_STORE " + filename + fileSize);
        String msg = portbr.readLine();

        if (msg.equals("ACK")) {

            sendFile(data);

        }

    }

    private byte[] getFileData(String filename) throws IOException {

        File file = new File("downloads/" + Dstore.file_folder + "/" + filename);
        return Files.readAllBytes(file.toPath());

    }

    private int getFileSize(String filename) {

        File file = new File("downloads/" + Dstore.file_folder + "/" + filename);

        return (int) file.length();

    }

    /**
     * Gets a space separated list of files in the DStore's folder
     *
     * @return the message to return to the client
     */
    private String getFileList() {

        String strList = "FILE";

        File dir = new File("downloads/" + Dstore.file_folder);

        if (dir.exists()) {

            String[] files = dir.list();

            for (String filename : files) {

                strList += " " + filename;

            }

        }

        return strList;

    }

    /**
     * Deletes the file in this DStore's folder with the given name
     *
     * @param filename name of file
     * @return an ack or error string to reply with
     * @throws Exception if file could not be deleted
     */
    private String deleteFile(String filename) throws Exception {

        File file = new File("downloads/" + Dstore.file_folder + "/" + filename);

        boolean deleted = false;

        if (file.exists())
            deleted = file.delete();
        else
            return "ERROR_FILE_DOES_NOT_EXIST " + filename;

        if (!deleted)
            throw new Exception("File could not be deleted");

        return "REMOVE_ACK " + filename;

    }

}
