import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class ThreadedSocket {

    final Exception MalformedException = new Exception("Malformed message");

    Socket socket;
    PrintWriter pw = null;
    BufferedReader br = null;

    public ThreadedSocket(Socket socket) throws IOException {

        this.socket = socket;
        pw = new PrintWriter(socket.getOutputStream(), true);
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    }

    abstract void messageReceived(String msg) throws Exception;

    /**
     * Sends a file
     *
     * @param data the file data
     * @throws IOException
     */
    void sendFile(byte[] data) throws IOException {

        socket.getOutputStream().write(data);

    }

    /**
     * Listens for a file
     *
     * @param size the size of the file
     * @return the received data
     * @throws IOException
     */
    byte[] receiveFile(int size) throws IOException {

        byte[] data = socket.getInputStream().readNBytes(size);

        return data;

    }

    /**
     * Gets the num at the ith word in a string
     *
     * @param msg the string
     * @param i   the index
     * @return the num
     * @throws Exception
     */
    int getNum(String msg, int i) throws Exception {

        String[] words = msg.split(" ");

        try {

            return Integer.parseInt(words[i]);

        } catch (Exception e) {

            throw MalformedException;

        }

    }

    /**
     * Gets the ith word in a string
     *
     * @param msg the string
     * @param i   the index
     * @return the ith word
     * @throws Exception
     */
    String getWord(String msg, int i) throws Exception {


        String[] words = msg.split(" ");

        try {

            return words[i];

        } catch (Exception e) {

            throw MalformedException;

        }

    }

}
