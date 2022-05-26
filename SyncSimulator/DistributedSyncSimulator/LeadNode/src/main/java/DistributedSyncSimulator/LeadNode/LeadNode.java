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
            m_log = new MyLog();
            m_log.init(LEAD_NODE_NAME);
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

    private int m_transactionCount = 0;
    private int m_nDeadLock = 0;
    private TwoPhaseLockManager m_2plMgr;
    private static MyLog m_log;

    public LeadNode(int port){
        m_nPort = port;

        try{
            Registry reg = LocateRegistry.createRegistry(m_nPort);
            LeaderIFC leadIfc = (LeaderIFC)UnicastRemoteObject.exportObject(this, 0);
            reg.bind(LEAD_NODE_NAME, leadIfc);
            m_2plMgr = new TwoPhaseLockManager();
            m_log.log("LeadNode Ready!" + NEWLINE);

        }catch(RemoteException re){
            System.out.println("Remote Exception: " + re.getMessage());
        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
        }
    }


    // get requestor's function call interface
    private WorkerIFC getRequestorFuncs(String workerName) {
		try {
			
			Registry registry = LocateRegistry.getRegistry(m_nPort);
			//String workerName = WORK_NODE_NAME + id; // worker1, worker2, etc
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
    public synchronized boolean acquireLock(MyAction act) throws RemoteException{
        boolean status = false;
        
        try{
            //System.out.println(m_name + ": acquire lock for act " + act);
            status = m_2plMgr.acquireLocks(act);
        }catch(Exception ex){
            System.out.println(m_name + ": acquire lock exception " + ex.getMessage());
            ex.printStackTrace();
        }
        //System.out.println(m_name + ": acquire lock status " + status);
        m_log.log("Acquire lock for act " + act + ", Satus = " + status + NEWLINE);
        return status;
    }

    public synchronized void releaseLock(MyTransaction tran) throws RemoteException{
        m_log.log("ReleaseLock for " + tran + NEWLINE);
        
        try{
            List<String> workers = m_2plMgr.releaseLocks(tran);
            for(String name : workers){
                WorkerIFC wifc = getRequestorFuncs(name);
                wifc.unblock();
                m_log.log(name + " 's log released" + NEWLINE);
            }
        }catch(Exception ex){
            System.out.println(m_name + ": release lock exception " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // test
    public synchronized void HelloLead(String workerName) throws RemoteException{
        System.out.println("Lead Node Says Hello " + workerName);
        WorkerIFC wifc = getRequestorFuncs(workerName);
        wifc.HelloWorker("LeadNode");
    }
}