package distributedsyncsimulator.ifc;
import static distributedsyncsimulator.utilities.Constants.*;
import distributedsyncsimulator.utilities.*;
import distributedsyncsimulator.shared.MyAction;
import distributedsyncsimulator.shared.MyTransaction;
import java.util.*;
import java.rmi.*;

public interface LeaderIFC extends Remote{

    public LockStatus acquireLock(MyAction act) throws RemoteException;

    public void releaseLock(MyTransaction tran) throws RemoteException;

    public void HelloLead(String workerName, UUID transId) throws RemoteException;

    public void ByeLead(String workerName, UUID transId) throws RemoteException;

    public void RollbackTransction(MyTransaction mt) throws RemoteException;
    
    public void AbortTransaction(MyTransaction mt) throws RemoteException;
}