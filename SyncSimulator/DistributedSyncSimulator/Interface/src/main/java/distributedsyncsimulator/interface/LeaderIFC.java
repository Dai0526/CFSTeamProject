package distributedsyncsimulator.ifc;

import distributedsyncsimulator.shared.MyAction;
import distributedsyncsimulator.shared.MyTransaction;

import java.rmi.*;

public interface LeaderIFC extends Remote{

    public boolean acquireLock(MyAction act) throws RemoteException;

    public void releaseLock(MyTransaction tran) throws RemoteException;

    public void HelloLead(String workerName) throws RemoteException;
}