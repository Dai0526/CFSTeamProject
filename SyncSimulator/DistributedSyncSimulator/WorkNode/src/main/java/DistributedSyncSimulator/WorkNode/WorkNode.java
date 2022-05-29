package distributedsyncsimulator.worknode;

import static distributedsyncsimulator.utilities.Constants.*;
import distributedsyncsimulator.ifc.*;
import distributedsyncsimulator.utilities.*;
import distributedsyncsimulator.shared.*;
import distributedsyncsimulator.shared.MyAction.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.lang.Thread;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.HashMap;


public class WorkNode implements WorkerIFC, Runnable {

    public static void main(String[] args){
        String id = args[0]; 
        String transFilePath = args[1];
        WorkNode wn = new WorkNode(HOST_IP, DEFAULT_LEAD_PORT, id, transFilePath);
        wn.run();
    }

    private LeaderIFC m_leadInterface;

    private boolean m_isBlocked = false;
    public boolean m_requestAbort = false;

    public String m_name;
    public String m_id;
    private String m_host;
    private int m_nPort;

    private Deque<MyTransaction> m_transList;
    private MyDatabase m_dbm;

    private int m_nProcessed = 0;
    private int m_nCommited = 0;
    private int m_nAborted = 0;
    private static MyLog m_log;

    private MyConfiguration m_config;

    public WorkNode(String ip, int port, String id, String trasFiles){
        try{

            m_name = String.format("%s%s", WORK_NODE_NAME, id);

            m_log = new MyLog();
            m_log.init(m_name);

            m_dbm.instance();

            m_host = ip;
            m_nPort = port;
            m_id = id;
  
            
            m_config = new MyConfiguration();

            getTransactionSequence(trasFiles);

            Registry reg = LocateRegistry.getRegistry(ip, port);
            m_leadInterface = (LeaderIFC) reg.lookup(Constants.LEAD_NODE_NAME);

            WorkerIFC workerInterface = (WorkerIFC) UnicastRemoteObject.exportObject(this, 0);
            reg.bind(m_name, workerInterface);

            m_log.log("WorkNode " + m_name + " is running" + NEWLINE);
            Thread.sleep(RUN_INTERVAL_MS);

        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
        }
    }


    @Override
    public void run(){
        long startTime = System.currentTimeMillis();
       
        try{
            int nTrans = m_transList.size();
            while(true){

                synchronized(this){
                    MyTransaction curr = getNextTransaction();

                    if(curr == null){
                        long endTime = System.currentTimeMillis();
                        m_log.log(m_name + ": No new transactions. Blocked and wait... " + NEWLINE);
                        m_log.log(m_name + ": Processed " + m_nProcessed + " transactions" + NEWLINE);
                        m_log.log(m_name + ": Commited " + m_nProcessed + " transactions" + NEWLINE);
                        m_log.log(m_name + ": spent " + (endTime - startTime) + " mSecs" + NEWLINE);
                        blockAndWait();
                    }

                    m_leadInterface.HelloLead(m_name, curr.m_id);
                    //System.out.println(m_name + ": Start processing Transaction from " + curr);
                    m_log.log(m_name + ": Start processing Transaction from " + curr);
                    for(MyAction act : curr.m_actions){
                        if(m_requestAbort){
                            break;
                        }

                        ActionStatus actStatus = checkActionStatus(act);

                        switch(actStatus){
                            case PERMITTED:
                                curr.execSingleAct(act);
                                break;
                            case ROLLBACK:
                                rollbackTransaction(curr, true);
                                break;
                            case ABORT:
                                abortTransaction(curr);
                                break;
                            case IGNORE:
                                continue;
                            default:
                                break;
                        }
                    }
                    
                    checkTransactionStatus(curr);
                }
            }
        }catch(RemoteException re){
            System.out.println(m_name + ": RemoteException: " + re.getMessage());
        }
        catch(Exception e){
            System.out.println(m_name + ": Exception: " + e.getMessage());
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        m_log.log(m_name + ": Processed " + m_nProcessed + " transactions" + NEWLINE);
        m_log.log(m_name + ": Commited " + m_nProcessed + " transactions" + NEWLINE);
        m_log.log(m_name + ": spent " + (endTime - startTime) + " mSecs" + NEWLINE);
    }


    private MyTransaction getNextTransaction(){
		if(m_transList.size() == 0) {
            return null;
		}
		
        MyTransaction next = m_transList.getFirst();
        m_transList.removeFirst();
		return next;
    }



    private ActionStatus checkActionStatus(MyAction act) throws Exception {
        
        ActionStatus actStatus = ActionStatus.REJECT;
        switch(act.m_actType){
            case READ:
            case WRITE:
                LockStatus status = m_leadInterface.acquireLock(act);
                 m_log.log("Action " + act + "'s lock status = " + status + NEWLINE);
                if(status.compareTo(LockStatus.REJECT) == 0){
                    blockAndWait();

                    if(m_requestAbort){
                        return ActionStatus.REJECT; // resume due to abort
                    }

                }else if(status.compareTo(LockStatus.ROLLBACK) == 0){
                    actStatus = ActionStatus.ROLLBACK;
                }else if(status.compareTo(LockStatus.ABORT) == 0){
                    actStatus = ActionStatus.ABORT;
                }else{
                    actStatus = ActionStatus.PERMITTED;
                }
                
                break;
            case MINUS:
            case ADD:
                actStatus = ActionStatus.PERMITTED;
                break;
            default:
                break;
        }

        return actStatus;
    }

    private boolean checkTransactionStatus(MyTransaction tras) throws RemoteException {
        // commit if transaction done without aborting
        if(m_requestAbort){
            m_requestAbort = false;
            m_log.log("Transaction " + tras.m_id + " boarted." + NEWLINE);
            return false;
        }

        tras.commit();
        broadCastCommit(tras);
        m_leadInterface.ByeLead(m_name, tras.m_id);
        ++m_nCommited;
        m_leadInterface.releaseLock(tras);
        m_log.log("Release Lock for Transaction " + tras.m_id + NEWLINE);
        MyDatabase.instance().readAll(m_name);
        ++m_nProcessed;
        return true;
    }

    private void getTransactionSequence(String path) throws Exception {
        m_transList = new LinkedList<MyTransaction>();

        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            for(String line; (line = br.readLine()) != null; ) {
                long nowStamp = MyUtils.getTimestamp();
                MyTransaction mt = new MyTransaction(nowStamp, m_name);
                
                String[] segs = line.split(",");

                /*
                    r-Banana, p-Banana-2, w-Banana
                    r-Banana, w-Banana-2, w-Banana
                    read   -> r - <target name> 
                    write  -> w - <target name>
                    add    -> p 
                    minus  -> m
                */

                for(String s : segs){
                    MyAction act = new MyAction(mt.m_id, mt.m_workName);
                    act.setTransTimestamp(mt.m_timeCreated);
                    char type = s.charAt(0);
                    String[] items = s.split("-");
                    switch(type){
                        case 'r':
                            act.setType(ActionType.READ);
                            act.setTarget(items[1]);
                            break;
                        case 'm':
                            act.setType(ActionType.MINUS);
                            act.setTarget(items[1]);
                            act.setValue(Integer.parseInt(items[2]));
                            break;
                        case 'p':
                            act.setType(ActionType.ADD);
                            act.setTarget(items[1]);
                            act.setValue(Integer.parseInt(items[2]));
                            break;
                        case 'w':
                            act.setType(ActionType.WRITE);
                            act.setTarget(items[1]);
                            break;
                        default:
                            break;
                    }
                    mt.addAction(act);
                    
                }

                m_transList.add(mt);
            }
        }catch(Exception ex){
            m_log.log(m_name + " read transaction failed: " + ex.getMessage() + NEWLINE);
            return;
        }

        m_log.log("# of Transaction Read = "  + m_transList.size() + NEWLINE);
    }

    public synchronized void blockAndWait(){
        try{
            m_isBlocked = true;
            m_log.log(m_name + " block and wait... " + NEWLINE);
            while(m_isBlocked){
                Thread.sleep(DETECTION_INTERVAL_MS);
            }
        }catch(Exception e){
            System.out.println(m_name + ": throw exception: " + e.getMessage());
			e.printStackTrace();
        }
    }

    private synchronized void rollbackTransaction(MyTransaction mt, boolean toHead) throws RemoteException {
        if(toHead){
            m_transList.addFirst(mt);
        }else{
            mt.resetTimestamp();
            m_transList.add(mt);
        }

        m_leadInterface.RollbackTransction(mt);

    }
    public synchronized void abortTransaction(MyTransaction mt) throws RemoteException {
		m_requestAbort = true;
        m_log.log(m_name + " transaction aborted " + NEWLINE);
        m_leadInterface.AbortTransaction(mt);
        unblock();
	}

    private void broadCastCommit(MyTransaction tran) throws RemoteException{
        HashMap<String, String> workers = m_config.getWorkers();

        Iterator wkrItr = workers.entrySet().iterator();

        while (wkrItr.hasNext()) {
            Map.Entry curr = (Map.Entry)wkrItr.next();
            String name = (String)curr.getKey();
            if(name.compareTo(m_name) == 0){
                continue;
            }

            m_log.log("Inform " + name + " to update" + NEWLINE);// 
            WorkerIFC wifc = getRequestorFuncs(name);
            wifc.SyncData(tran);
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

    /*
        rmi function calls overrided
    */

    @Override
    public void unblock(){
        m_isBlocked = false;
        m_log.log(m_name + " unblocked" + NEWLINE);
    }

    @Override
    public void SyncData(MyTransaction tran) throws RemoteException{
        Iterator cmtItr = tran.m_candidates.entrySet().iterator();
        while(cmtItr.hasNext()){
            Map.Entry element = (Map.Entry)cmtItr.next();
            MyDatabase.instance().write((String)element.getKey(), (Integer)element.getValue());
            MyDatabase.instance().readAll(m_name);
        }
    }


}