package distributedsyncsimulator.ifc;

import shared.MyAction;
import shared.MyTransaction;

import java.rmi.*;

public interface LeadIFC extends Remote{

    public void getLock(MyAction act) throws RemoteException;

    public void freeLock(MyTransaction tran) throws RemoteException;
}