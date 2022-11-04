import java.util.ArrayList;

public class FileIndex {

    private ArrayList<DstoreFile> files = new ArrayList<>();

    public synchronized void add(DstoreFile file) {

        files.add(file);

    }

    public boolean fileExists(String filename) {

        for (DstoreFile file : files) {

            if (filename.equals(file.getName()))
                return true;

        }

        return false;

    }

    public DstoreFile getFile(String filename) throws Exception {

        for (DstoreFile file : files) {

            if (filename.equals(file.getName()))
                return file;

        }

        throw new Exception("File does not exist");

    }

    public synchronized DstoreFile removeFile(String filename) throws Exception {

        for (DstoreFile file : files) {

            if (filename.equals(file.getName())) {

                files.remove(file);
                return file;

            }

        }

        throw new Exception("File does not exist");

    }

    public synchronized void removeFile(DstoreFile file){

        files.remove(file);

    }

    public String getList() {

        String list = "";

        for (DstoreFile file : files) {

            list += file.getName() + " ";

        }

        return list.trim();

    }

    /**
     * Will choose and return the r ports which have the least files
     * @param replication
     * @return
     */
    public ArrayList<ControllerDstore> getRPorts(int replication, ArrayList<ControllerDstore> dStores) {

        ArrayList<ControllerDstore> rStores = new ArrayList<>();

        for (int i = 0; i < replication; i++) {

            ControllerDstore minFiles = new ControllerDstore(0, null, null);
            minFiles.setFileCount(Integer.MAX_VALUE);

            for (ControllerDstore dStore : dStores) {

                if(dStore.getFileCount() < minFiles.getFileCount() && !rStores.contains(dStore))
                    minFiles = dStore;

            }

            rStores.add(minFiles);

        }

        return rStores;

    }

    public ArrayList<DstoreFile> getFiles() {
        return files;
    }

    public synchronized void setFiles(ArrayList<DstoreFile> files) {
        this.files = files;
    }

    public synchronized void setStatus(String filename, String status) throws Exception {

        DstoreFile file = getFile(filename);
        file.setStatus(status);

    }

    public boolean isStatus(String filename, String status) throws Exception {

        DstoreFile file = getFile(filename);
        return file.getStatus().equals(status);

    }

}
