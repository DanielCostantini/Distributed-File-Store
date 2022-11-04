import java.io.*;
import java.net.Socket;

public class DstoreClientThread extends ThreadedSocket implements Runnable {

    private PrintWriter cpw = null;

    public DstoreClientThread(Socket cSocket, Socket socket) throws IOException {
        super(socket);

        this.socket = socket;
        socket.setSoTimeout(Dstore.timeout);

        cpw = new PrintWriter(cSocket.getOutputStream(), true);

    }

    @Override
    public void run() {

        try {

            String msg;

            while ((msg = br.readLine()) != null)
                messageReceived(msg);

        } catch (Exception e) {

            System.err.println(e);

        } finally {

            try {
                System.out.println("Closing port " + socket.getPort());
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    void messageReceived(String msg) throws Exception {

        System.out.println("Message received from port " + socket.getPort() + ": " + msg);

        if (msg.contains("STORE")) {

            String filename = getWord(msg, 1);
            int fileSize = getNum(msg, 2);

            pw.println("ACK");

            byte[] data = receiveFile(fileSize);
            cpw.println(store(filename, data));

        } else if (msg.contains("LOAD_DATA")) {

            byte[] data = load(getWord(msg, 1));
            sendFile(data);

        } else {

            throw MalformedException;

        }

    }

    /**
     * Loads a file
     *
     * @param filename name of file
     * @return the file data
     * @throws Exception
     */
    private byte[] load(String filename) throws Exception {

        File dir = new File(Dstore.file_folder);
        File filepath = new File(dir + "/" + filename);

        if (!dir.exists())
            throw new FileNotFoundException();

        FileInputStream fis = new FileInputStream(filepath);
        byte[] data = fis.readAllBytes();
        fis.close();

        return data;

    }

    /**
     * Stores data in a given filename
     *
     * @param filename file to store data in
     * @param data     file data
     * @return Ack message to return when complete
     * @throws Exception
     */
    public String store(String filename, byte[] data) {

        File dir = new File(Dstore.file_folder);
        System.out.println(dir);
        File filepath = new File(dir + "/" + filename);
        System.out.println(filepath);

        if (!dir.exists())
            dir.mkdir();

        try {

            FileOutputStream fos = new FileOutputStream(filepath);
            fos.write(data);
            fos.close();

        } catch (Exception e){

            System.err.println(e);
            System.out.println("ERROR STORING FILE " + filename + " in folder " + Dstore.file_folder + " path " + filepath.getAbsolutePath());

        }

        return "STORE_ACK " + filename;

    }

}
