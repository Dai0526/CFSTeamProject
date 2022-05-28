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

    public WorkNode(String ip, int port, String id, String trasFiles){
        try{
            m_host = ip;
            m_nPort = port;
            m_id = id;
            m_name = String.format("%s%s", WORK_NODE_NAME, id);
            
            m_log = new MyLog();
            m_log.init(m_name);

            getTransactionSequence(trasFiles);

            Registry reg = LocateRegistry.getRegistry(ip, port);
            m_leadInterface = (LeaderIFC) reg.lookup(Constants.LEAD_NODE_NAME);

            WorkerIFC workerInterface = (WorkerIFC) UnicastRemoteObject.exportObject(this, 0);
            reg.bind(m_name, workerInterface);

            m_log.log("WorkNode " + m_name + " is running" + NEWLINE);

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
                        System.out.println(m_name + ": No new transactions. Blocked and wait... ");
                        System.out.println(m_name + ": Processed " + m_nProcessed + " transactions");
                        System.out.println(m_name + ": Commited " + m_nProcessed + " transactions");
                        System.out.println(m_name + ": spent " + (endTime - startTime) + " mSecs");
                        blockAndWait();
                    }

                    m_leadInterface.HelloLead(m_name, curr.m_id);
                    //System.out.println(m_name + ": Start processing Transaction from " + curr);
                    m_log.log(m_name + ": Start processing Transaction from " + curr);
                    for(MyAction act : curr.m_actions){
                        if(m_requestAbort){
                            break;
                        }

                        if(checkActionStatus(act) == true){
                            curr.execSingleAct(act);
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
        System.out.println(m_name + ": Processed " + m_nProcessed + " transactions");
        System.out.println(m_name + ": Commited " + m_nProcessed + " transactions");
        System.out.println(m_name + ": spent " + (endTime - startTime) + " mSecs");
    }


    private MyTransaction getNextTransaction(){
		if(m_transList.size() == 0) {
            return null;
		}
		
        MyTransaction next = m_transList.getFirst();
        m_transList.removeFirst();
		return next;
    }



    private boolean checkActionStatus(MyAction act) throws Exception {
        boolean canProcess = false;
        switch(act.m_actType){
            case READ:
            case WRITE:
                boolean status = m_leadInterface.acquireLock(act);
                if(status == false){
                    blockAndWait();
                }

                if(m_requestAbort){
                    break;
                }
                canProcess = true;
                break;
            case MINUS:
            case ADD:
                canProcess = true;
                break;
            default:
                break;
        }

        return canProcess;
    }

    private boolean checkTransactionStatus(MyTransaction tras) throws RemoteException {
        // commit if transaction done without aborting
        if(m_requestAbort){
            m_requestAbort = false;
            m_log.log("Transaction " + tras.m_id + " boarted." + NEWLINE);
            return false;
        }

        tras.commit();
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

    private void rollbackTransaction(MyTransaction mt, boolean toHead){
        if(toHead){
            m_transList.addFirst(mt);
        }else{
            mt.resetTimestamp();
            m_transList.add(mt);
        }
    }

    /*
        rmi function calls overrided
    */

    @Override
	public void abortTransaction() throws RemoteException {
		m_requestAbort = true;
        m_log.log(m_name + " transaction aborted " + NEWLINE);
        unblock();
	}

    @Override
    public void unblock(){
        m_isBlocked = false;
        m_log.log(m_name + " unblocked" + NEWLINE);
    }

    @Override
    public void HelloWorker(String name) throws RemoteException{
        System.out.println("Work Node Says Hello " + name);
    }

    @Override
    public void RollbackTransction(MyTransaction mt)throws RemoteException{
        rollbackTransaction(mt, false);
        m_isBlocked = false;
    }

    @Override
    public void DelayTransction(MyTransaction mt)throws RemoteException{
        rollbackTransaction(mt, true);
        m_isBlocked = false;
    }
}