import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class ControllerDstore {

    private int port;
    private BufferedReader br;
    private PrintWriter pw;
    private ArrayList<CountDownLatch> latches = new ArrayList<>();
    private ArrayList<String> messages = new ArrayList<>();
    private int fileCount = 0;

    public ControllerDstore(int port, BufferedReader br, PrintWriter pw) {

        this.port = port;
        this.br = br;
        this.pw = pw;

    }

    void messageReceived(String msg) throws Exception {

        for (int i = 0; i < latches.size(); i++) {

            if (msg.equals(messages.get(i))) {

                latches.get(i).countDown();
                removeExpectedMsg(i);

            }

        }

        if (msg.contains("LIST")) {

            String[] files = msg.split(" ");
            fileCount = files.length - 1;


        } else if (msg.contains("REBALANCE_COMPLETE")) {

        } else {

//            throw MalformedException;

        }

    }

    public int getPort() {
        return port;
    }

    public synchronized void addExpectedMsg(CountDownLatch latch, String msg) {

        latches.add(latch);
        messages.add(msg);

    }

    public synchronized void removeExpectedMsg(int index) {

        latches.remove(index);
        messages.remove(index);

    }

    public void sendMessage(String msg) {

        pw.println(msg);

    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public ArrayList<String> getFilenames() throws IOException {

        ArrayList<String> filenames = new ArrayList<>();

        pw.println("LIST");
        //TIMEOUT WAIT

        String msg = br.readLine();

        if (msg.contains("LIST")) {

            String[] words = msg.split(" ");

            for (String word : words) {

                filenames.add(word);

            }

            filenames.remove(0);

        }


        return filenames;

    }

}
