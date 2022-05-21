package distributedsyncsimulator.ifc;

import distributedsyncsimulator.shared.MyAction;
import distributedsyncsimulator.shared.MyTransaction;

import java.rmi.*;

public interface LeaderIFC extends Remote{

    public void getLock(MyAction act) throws RemoteException;

    public void freeLock(MyTransaction tran) throws RemoteException;
}