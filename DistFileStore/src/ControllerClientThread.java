import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ControllerClientThread extends ThreadedSocket implements Runnable {

    private static final Object storeLock = new Object();

    private Controller controller;
    private ControllerDstore dStore = null;
    private ArrayList<ControllerDstore> loadAttempts = null;

    public ControllerClientThread(Socket socket, Controller controller) throws IOException {
        super(socket);

        this.controller = controller;

    }


    @Override
    public void run() {

        try {

            String msg;

            while ((msg = br.readLine()) != null) {

                System.out.println(msg + " received");

                if (dStore == null)
                    messageReceived(msg);
                else
                    dStore.messageReceived(msg);

            }

        } catch (Exception e) {

            System.err.println(e);

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }


    void messageReceived(String msg) throws Exception {

        if (msg.contains("JOIN")) {

            int port = getNum(msg, 1);
            dStore = new ControllerDstore(port, br, pw);
            controller.addDStore(dStore);
//            controller.rebalance();

        } else if (msg.contains("STORE")) {

            String filename = getWord(msg, 1);
            int filesize = getNum(msg, 2);

            synchronized (storeLock) {

                if (controller.getDStoreSize() < controller.getReplication())
                    pw.println("ERROR_NOT_ENOUGH_DSTORES");
                else if (controller.getIndex().fileExists(filename))
                    pw.println("ERROR_FILE_ALREADY_EXISTS");
                else {

                    ArrayList<ControllerDstore> rPorts = controller.getIndex().getRPorts(controller.getReplication(), controller.getdStores());
                    controller.getIndex().add(new DstoreFile(filename, filesize, "store in progress", rPorts));


                    incrementFileCounts(rPorts);
                    pw.println("STORE_TO " + toPortString(rPorts));
                    controller.receivedAcks(rPorts, "STORE_ACK " + filename);
                    controller.getIndex().setStatus(filename, "store complete");
                    pw.println("STORE_COMPLETE");

                }

            }


        } else if (msg.contains("RELOAD")) {

            String filename = getWord(msg, 1);

            if (controller.getDStoreSize() < controller.getReplication())
                pw.println("ERROR_NOT_ENOUGH_DSTORES");
            else if (!controller.getIndex().fileExists(filename) || controller.getIndex().isStatus(filename, "store in progress")
                    || controller.getIndex().isStatus(filename, "remove in progress"))
                pw.println("ERROR_FILE_DOES_NOT_EXIST");
            else {

                DstoreFile file = controller.getIndex().getFile(filename);
                ControllerDstore loadAttempt = file.getDStore(loadAttempts);

                if (loadAttempt != null) {

                    loadAttempts.add(loadAttempt);
                    pw.println("LOAD_FROM " + loadAttempt.getPort() + " " + file.getSize());

                } else {

                    pw.println("ERROR_LOAD");

                }

            }

        } else if (msg.contains("LOAD")) {

            String filename = getWord(msg, 1);

            if (controller.getDStoreSize() < controller.getReplication())
                pw.println("ERROR_NOT_ENOUGH_DSTORES");
            else if (!controller.getIndex().fileExists(filename) || controller.getIndex().isStatus(filename, "store in progress")
                    || controller.getIndex().isStatus(filename, "remove in progress"))
                pw.println("ERROR_FILE_DOES_NOT_EXIST");
            else {

                loadAttempts = new ArrayList<>();
                DstoreFile file = controller.getIndex().getFile(filename);
                ControllerDstore loadAttempt = file.getDStore(loadAttempts);
                loadAttempts.add(loadAttempt);
                pw.println("LOAD_FROM " + loadAttempt.getPort() + " " + file.getSize());

            }

        } else if (msg.contains("REMOVE")) {

            String filename = getWord(msg, 1);

            if (controller.getDStoreSize() < controller.getReplication())
                pw.println("ERROR_NOT_ENOUGH_DSTORES");
            else if (!controller.getIndex().fileExists(filename) || controller.getIndex().isStatus(filename, "store in progress")
                    || controller.getIndex().isStatus(filename, "remove in progress"))
                pw.println("ERROR_FILE_DOES_NOT_EXIST");
            else {

                pw.println(controller.removeFile(filename));

            }

        } else if (msg.trim().equals("LIST")) {

            String fileList = "LIST " + controller.getIndex().getList();
            pw.println(fileList);

        } else {

            throw MalformedException;

        }

    }

    private void incrementFileCounts(ArrayList<ControllerDstore> dStores) {

        for (ControllerDstore dStore : dStores) {

            dStore.setFileCount(dStore.getFileCount() + 1);

        }

    }

    private static String toPortString(ArrayList<ControllerDstore> list) {

        String output = "";

        for (ControllerDstore dStore : list) {

            output += " " + dStore.getPort();

        }

        return output.trim();

    }

}
