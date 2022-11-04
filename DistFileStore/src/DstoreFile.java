import java.util.ArrayList;

public class DstoreFile {

    private String name;
    private int size;
    private String status;
    private ArrayList<ControllerDstore> dStores;

    public DstoreFile(String name, int size, String status, ArrayList<ControllerDstore> dStorePorts) {

        this.name = name;
        this.size = size;
        this.status = status;
        this.dStores = dStorePorts;

    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<ControllerDstore> getdStores() {
        return dStores;
    }

    public void setdStores(ArrayList<ControllerDstore> dStores) {
        this.dStores = dStores;
    }

    public void adddStorePort(ControllerDstore port) {

        if (dStores != null) {

            dStores.add(port);

        } else {

            dStores = new ArrayList<>();
            dStores.add(port);

        }

    }

    public Integer removedStorePort(ControllerDstore port){

        if (dStores != null) {

            for (ControllerDstore storePort: dStores) {

                if(storePort.equals(port)){

                    dStores.remove(storePort);
                    return storePort.getPort();

                }

            }

            return 0;

        } else {

            return 0;

        }

    }

    public ControllerDstore getDStore(ArrayList<ControllerDstore> loadAttempts){

        for (ControllerDstore dStore: dStores) {

            if(!loadAttempts.contains(dStore))
                return dStore;

        }

        return null;

    }

}
