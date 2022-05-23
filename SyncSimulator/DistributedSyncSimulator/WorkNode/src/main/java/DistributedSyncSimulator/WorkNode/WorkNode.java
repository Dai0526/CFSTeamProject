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
import java.util.*;


public class WorkNode implements WorkerIFC, Runnable {

    public static void main(String[] args){
        //assume
        int id = Integer.parseInt(args[0]); 
        String transFilePath =  args[1];

        WorkNode wn = new WorkNode(HOST_IP, DEFAULT_LEAD_PORT, id);
        wn.run();
    }

    private LeaderIFC m_leadInterface;

    private boolean m_isBlocked = false;
    public boolean m_requestAbort = false;

    public String m_name;
    public int m_id = 1;
    private String m_host;
    private int m_nPort;

    public WorkNode(String ip, int port, int id){
        try{
            m_host = ip;
            m_nPort = port;
            m_id = id;
            m_name = String.format("%s%s", WORK_NODE_NAME, id);

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
            int n = 0;
            while(n < 5){
                m_leadInterface.HelloLead(m_name);
                Thread.sleep(1000);
                m_leadInterface.acquireLock(new MyAction(n, "Tian", ActionType.READ));
                Thread.sleep(1000);
                m_leadInterface.releaseLock(new MyTransaction(n, MyUtils.getTimestamp()));
                Thread.sleep(1000);
                ++n;
            }
        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
        }

    }


    public synchronized void blockAndWait(){
        try{
            m_isBlocked = true;
            while(m_isBlocked){
                System.out.println(m_name + " is blocked. Waiting ..");
                Thread.sleep(DETECTION_INTERVAL_MS);
            }
        }catch(Exception e){
            System.out.println(m_name + " throw exception: " + e.getMessage());
			e.printStackTrace();
        }
    }

    @Override
	public void abortTransaction() throws RemoteException {
		m_requestAbort = true;
		System.out.println(m_name + " transaction aborted");
        unblock();
	}

    @Override
    public void unblock(){
        m_isBlocked = false;
        System.out.println(m_name + " unblocked");
    }

    @Override
    public void HelloWorker(String name) throws RemoteException{
        System.out.println("Work Node Says Hello " + name);
    }
}