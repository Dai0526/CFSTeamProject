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
    private ArrayList<MyTransaction> m_transList;
    private MyDatabase m_dbm;

    public WorkNode(String ip, int port, String id, String trasFiles){
        try{
            m_host = ip;
            m_nPort = port;
            m_id = id;
            m_name = String.format("%s%s", WORK_NODE_NAME, id);

            getTransactionSequence(trasFiles);

            Registry reg = LocateRegistry.getRegistry(ip, port);
            m_leadInterface = (LeaderIFC) reg.lookup(Constants.LEAD_NODE_NAME);

            WorkerIFC workerInterface = (WorkerIFC) UnicastRemoteObject.exportObject(this, 0);
            reg.bind(m_name, workerInterface);

            System.out.println("WorkNode " + m_name + " is running");


        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
        }
    }


    @Override
    public void run(){

        try{
            int nTrans = m_transList.size();
            int idx = 0;
            while(true){
                synchronized(this){
                    if(idx >= nTrans){
                        System.out.println(m_name + ": No new transactions. Blocked and wait... ");
                        blockAndWait();
                    }

                    MyTransaction curr = m_transList.get(idx);
                    System.out.println(m_name + ": Start processing Transaction from " + curr);

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

                ++idx;
            }
        }catch(RemoteException re){
            System.out.println(m_name + ": RemoteException: " + re.getMessage());
        }
        catch(Exception e){
            System.out.println(m_name + ": Exception: " + e.getMessage());
            e.printStackTrace();
        }

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
            case UPDATE:
                canProcess = true;
            default:
                break;
        }

        return canProcess;
    }

    private boolean checkTransactionStatus(MyTransaction tras) throws RemoteException {
        // commit if transaction done without aborting
        if(m_requestAbort){
            m_requestAbort = false;
            System.out.println(m_name + ": Transaction " + tras.m_id + " boarted. ");
            return false;
        }

        tras.commit();
        m_leadInterface.releaseLock(tras);
        System.out.println(m_name + ": Transaction " + tras.m_id + " commited. ");
        MyDatabase.instance().readAll(m_name);
        return true;
    }

    private void getTransactionSequence(String path) throws Exception {
        m_transList = new ArrayList<MyTransaction>();

        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            for(String line; (line = br.readLine()) != null; ) {
                long nowStamp = MyUtils.getTimestamp();
                MyTransaction mt = new MyTransaction(nowStamp, m_name);
                
                String[] segs = line.split(",");

                /*
                    r-Banana, x-Banana-2, w-Banana
                    read   -> r - <target name> 
                    update -> x - <target name> - <value> 
                    write  -> w - <target name>
                */

                for(String s : segs){
                    MyAction act = new MyAction(mt.m_id);
                    char type = s.charAt(0);
                    String[] items = s.split("-");
                    switch(type){
                        case 'r':
                            act.setType(ActionType.READ);
                            act.setTarget(items[1]);
                            break;
                        case 'x':
                            act.setType(ActionType.UPDATE);
                            act.setTarget(items[1]);
                            act.setValue(Integer.parseInt(items[2]));
                            break;
                        case 'w':
                            act.setType(ActionType.READ);
                            act.setTarget(items[1]);
                            break;
                        default:
                            break;
                    }
                    mt.addAction(act);
                    
                }
                System.out.println(mt.toString());
                m_transList.add(mt);

            }
        }catch(Exception ex){
            System.out.println(m_name + ": read transaction failed: " + ex.getMessage());
        }
    }

    public synchronized void blockAndWait(){
        try{
            m_isBlocked = true;
            while(m_isBlocked){
                Thread.sleep(DETECTION_INTERVAL_MS);
            }
        }catch(Exception e){
            System.out.println(m_name + ": throw exception: " + e.getMessage());
			e.printStackTrace();
        }
    }


    /*
        rmi function calls overrided
    */

    @Override
	public void abortTransaction() throws RemoteException {
		m_requestAbort = true;
		System.out.println(m_name + ": transaction aborted");
        unblock();
	}

    @Override
    public void unblock(){
        m_isBlocked = false;
        System.out.println(m_name + ": unblocked");
    }

    @Override
    public void HelloWorker(String name) throws RemoteException{
        System.out.println("Work Node Says Hello " + name);
    }


}