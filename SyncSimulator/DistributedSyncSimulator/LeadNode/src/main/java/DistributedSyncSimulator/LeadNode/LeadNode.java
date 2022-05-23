package distributedsyncsimulator.leadnode;

import distributedsyncsimulator.ifc.*;
import distributedsyncsimulator.utilities.*;
import distributedsyncsimulator.shared.*;
import static distributedsyncsimulator.utilities.Constants.*;

import java.util.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class LeadNode implements LeaderIFC{

    // main method
    public static void main(String[] args){
        
        try {
			 
			LeadNode leader = new LeadNode(m_nPort);
		
		}
		catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
        
    }


    /*
        Lead node class 
    */
    private static int m_nPort = DEFAULT_LEAD_PORT;
    private static int m_nDeteInterval = DETECTION_INTERVAL_MS;
    public static String m_name = LEAD_NODE_NAME;

    private int m_nWorker = 0;
    private int m_nDeadLock = 0;
    private Hashtable<Integer, String> m_workerMap;
    private TwoPhaseLockManager m_2plMgr;

    public LeadNode(int port){
        m_nPort = port;
        m_workerMap = new Hashtable<Integer, String>();

        try{
            Registry reg = LocateRegistry.createRegistry(m_nPort);
            LeaderIFC leadIfc = (LeaderIFC)UnicastRemoteObject.exportObject(this, 0);
            reg.bind(LEAD_NODE_NAME, leadIfc);

            m_2plMgr = new TwoPhaseLockManager();

            System.out.println("LeadNode Ready!");
        }catch(RemoteException re){
            System.out.println("Remote Exception: " + re.getMessage());
        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
        }
    }


    // get requestor's function call interface
    private WorkerIFC getRequestorFuncs(String id) {
		try {
			
			Registry registry = LocateRegistry.getRegistry(m_nPort);
			String workerName = WORK_NODE_NAME + id; // worker1, worker2
			WorkerIFC dataSiteStub = (WorkerIFC) registry.lookup(workerName);
			
			return dataSiteStub;
			
		}
		catch(RemoteException e) {
			System.out.println("Remote Exception: " + e.getMessage());
		}
		catch(Exception e) {
			System.out.println("Exception in getRequestorFuncs: " + e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}


    // rmi stub function calls
    public synchronized void acquireLock(MyAction act) throws RemoteException{
        System.out.println("Lead Node Process acquireLock");
        WorkerIFC wifc = getRequestorFuncs("001");
        wifc.HelloWorker("LeadNode");
    }

    public synchronized void releaseLock(MyTransaction tran) throws RemoteException{
        System.out.println("Lead Node Process releaseLock");
        WorkerIFC wifc = getRequestorFuncs("001");
        wifc.HelloWorker("LeadNode");
    }

    // test
    public synchronized void HelloLead(String workerName) throws RemoteException{
        System.out.println("Lead Node Says Hello " + workerName);
        WorkerIFC wifc = getRequestorFuncs("001");
        wifc.HelloWorker("LeadNode");
    }
}