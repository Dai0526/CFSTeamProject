package distributedsyncsimulator.ifc;

public interface WorkerIFC extends Remote {

    public void unlock() throws RemoteException;
    public void write() throws RemoteException;
}