package distributedsyncsimulator.ifc;
import distributedsyncsimulator.shared.MyTransaction;
import java.rmi.*;

public interface WorkerIFC extends Remote {

    public void unblock() throws RemoteException;

    public void abortTransaction() throws RemoteException;

    public void SyncData(MyTransaction tran) throws RemoteException;

    public void RollbackTransction(MyTransaction mt) throws RemoteException;
    
    public void DelayTransction(MyTransaction mt) throws RemoteException;
}