import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Controller {

    public static void main(String[] args) {

        int cport = Integer.parseInt(args[0]);
        int replication = Integer.parseInt(args[1]);
        int timeout = Integer.parseInt(args[2]);
        int rebalance_period = Integer.parseInt(args[3]);

        new Controller(cport, replication, timeout, rebalance_period).startServer();

    }

    private final int cport;
    private final int replication;
    private final int timeout;
    private final int rebalance_period;
    private ArrayList<ControllerDstore> dStores;
    private FileIndex index;

    public Controller(int cport, int replication, int timeout, int rebalance_period) {

        this.cport = cport;
        this.replication = replication;
        this.timeout = timeout;
        this.rebalance_period = rebalance_period;
        dStores = new ArrayList<>();
        index = new FileIndex();

    }

    private void startServer() {

        ServerSocket server = null;

        try {

            server = new ServerSocket(cport);

            while (true) {

                Socket socket = server.accept();
                System.out.println("Connecting port " + socket.getPort());
                new Thread(new ControllerClientThread(socket, this)).start();

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

    public int getCport() {
        return cport;
    }

    public int getReplication() {
        return replication;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getRebalance_period() {
        return rebalance_period;
    }

    public ArrayList<ControllerDstore> getdStores() {
        return dStores;
    }

    public FileIndex getIndex() {
        return index;
    }

    public void addDStore(ControllerDstore dStore) throws IOException {

        dStores.add(dStore);

    }

    public int getDStoreSize() {

        return dStores.size();

    }

    public void receivedAcks(ArrayList<ControllerDstore> rStores, String ackMsg) throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(rStores.size());

        for (ControllerDstore rStore : rStores) {

            rStore.addExpectedMsg(latch, ackMsg);

        }

        latch.await((long) ((double) timeout * (4.0 / 5.0)), TimeUnit.MILLISECONDS);

    }

    public String removeFile(String filename) throws Exception {

        DstoreFile file = index.getFile(filename);
        file.setStatus("remove in progress");
        ArrayList<ControllerDstore> rStores = file.getdStores();
        CountDownLatch latch = new CountDownLatch(rStores.size());

        for (ControllerDstore rStore : rStores) {

            rStore.addExpectedMsg(latch, "REMOVE_ACK " + filename);
            rStore.sendMessage("REMOVE " + filename);

        }


        boolean answered = latch.await((long) ((double) timeout * (4.0 / 5.0)), TimeUnit.MILLISECONDS);

        index.removeFile(file);

        return "REMOVE_COMPLETE";


    }

    public void rebalance() throws InterruptedException {

//            1.Controller asks each Dstore what files it stores
        ArrayList<ArrayList<String>> allFiles = new ArrayList<>();

        for (ControllerDstore dStore : dStores) {

            try {
                ArrayList<String> filenames = dStore.getFilenames();
                allFiles.add(filenames);
            } catch (IOException e) {
                dStores.remove(dStore);
                e.printStackTrace();
            }

        }

//            2. Controller revises file allocation
        ArrayList<ArrayList<String>> allSend = new ArrayList<>();
        ArrayList<ArrayList<String>> allRemove = new ArrayList<>();

        for (ControllerDstore dStore : dStores) {

        }


//            3. Controller tells each Dstore which files it should send to other Dstores or remove
        for (int i = 0; i < allFiles.size(); i++) {

            ArrayList<String> toSend = allSend.get(i);
            ArrayList<String> toRemove = allRemove.get(i);

            dStores.get(i).sendMessage("REBALANCE " + toSend.size() + " " + toSeparatedString(toSend) + " "
                    + toRemove.size() + " " + toSeparatedString(toRemove));

        }

//            4. Each Dstore sends/removes specified files and inform Controller once finished
        receivedAcks(dStores, "REBALANCE_COMPLETE");


    }

    private String toSeparatedString(ArrayList<String> strings) {

        String str = "";

        for (String string : strings) {

            str += string + " ";

        }

        str = str.trim();

        return str;

    }

}
