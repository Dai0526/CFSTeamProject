package distributedsyncsimulator.leadnode;

import distributedsyncsimulator.ifc.*;
import distributedsyncsimulator.utilities.*;
import distributedsyncsimulator.shared.*;
import static distributedsyncsimulator.utilities.Constants.*;

import java.util.*;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class LeadNode implements LeaderIFC{

    public enum SyncManagerType{
        TWO_PHASE_LOCK(0),
        TIMESTAMP(1),
        SNAPSHOT_ISOLATION(2);

        private final int value;

        SyncManagerType(int val){
            value = val;
        }

        public static SyncManagerType getValue(int value) {
            for(SyncManagerType e : SyncManagerType.values()) {
                if(e.value == value) {
                    return e;
                }
            }
                return SyncManagerType.TWO_PHASE_LOCK;// not found
        }
    };

    // main method
    public static void main(String[] args){

        int syncMethod = Integer.parseInt(args[0]);

        try {
            m_log = new MyLog();
            m_log.init(LEAD_NODE_NAME);
			LeadNode leader = new LeadNode(m_nPort, SyncManagerType.getValue(syncMethod));
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

    public SyncManagerType m_syncType;

    private int m_transactionCount = 0;
    private int m_nDeadLock = 0;
    private SyncManagerBase m_syncManager;
    private static MyLog m_log;

    private int m_nProcessed = 0;
    private long m_totalTime;
    private HashMap<UUID, Long> m_timeTable = new HashMap<UUID, Long>();

    public LeadNode(int port, SyncManagerType type){
        m_nPort = port;
        m_syncType = type;
        try{
            Registry reg = LocateRegistry.createRegistry(m_nPort);
            LeaderIFC leadIfc = (LeaderIFC)UnicastRemoteObject.exportObject(this, 0);
            reg.bind(LEAD_NODE_NAME, leadIfc);

            switch(type){
                case TIMESTAMP:
                    m_syncManager = new TimeStampOrderingManager();
                case SNAPSHOT_ISOLATION:
                    m_syncManager = new SanpshotIsolationManager();
                case TWO_PHASE_LOCK:
                default:
                    m_syncManager = new TwoPhaseLockManager();
                    break;
            }

            m_log.log("LeadNode Ready with " + m_syncType.name() + NEWLINE);

        }catch(RemoteException re){
            System.out.println("Remote Exception: " + re.getMessage());
        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
        }

        m_log.log(getStats());
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
    @Override
    public synchronized boolean acquireLock(MyAction act) throws RemoteException{
        boolean status = false;
        
        try{
            //System.out.println(m_name + ": acquire lock for act " + act);
            status = m_syncManager.acquireLocks(act);
        }catch(Exception ex){
            System.out.println(m_name + ": acquire lock exception " + ex.getMessage());
            ex.printStackTrace();
        }
        //System.out.println(m_name + ": acquire lock status " + status);
        m_log.log("Acquire lock for act " + act + ", Satus = " + status + NEWLINE);
        return status;
    }

    @Override
    public synchronized void releaseLock(MyTransaction tran) throws RemoteException{
        m_log.log("ReleaseLock for " + tran + NEWLINE);
        
        try{
            List<String> workers = m_syncManager.releaseLocks(tran);
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
    @Override
    public synchronized void HelloLead(String workerName, UUID transId) throws RemoteException{
        long start = System.currentTimeMillis();
        m_timeTable.put(transId, start);
        m_log.log("Get Transaction " + transId + " from " + workerName + NEWLINE);
    }

    @Override
    public synchronized void ByeLead(String workerName, UUID transId) throws RemoteException{
        long end = System.currentTimeMillis();
        long start = m_timeTable.get(transId);
        m_timeTable.remove(transId);

        m_totalTime += (end - start);
        ++m_nProcessed;
        m_log.log("Commited Transaction " + transId + " by " + workerName + NEWLINE);
        m_log.log(getStats());
    }

    private String getStats(){
        StringBuilder sb = new StringBuilder();
        sb.append(NEWLINE + "======================Statistic======================" + NEWLINE);
        sb.append("\t\tTotal Transaction Processed: " + m_nProcessed + NEWLINE);
        sb.append("\t\tTotal time spent (mSecs): " + m_totalTime + NEWLINE);
        sb.append("\t\tAverage Process Speed: " + (m_totalTime / (double)m_nProcessed) + NEWLINE);
        sb.append(NEWLINE + "=====================================================" + NEWLINE);
        return sb.toString();
    }
}