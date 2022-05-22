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

public class WorkNode implements WorkerIFC, Runnable {

    public String m_name;
    private String m_host = "127.0.0.1";
    private int m_nPort = 2345;

    public static void main(String[] args){
        WorkNode wn = new WorkNode("127.0.0.1", 2345);
        wn.run();
    }

    private LeaderIFC m_leadInterface;

    private boolean m_isBlocked = false;
    public boolean m_requestAbort = false;

    public WorkNode(String ip, int port){
        try{
            m_host = ip;
            m_nPort = port;
            Registry reg = LocateRegistry.getRegistry(ip, port);
            m_leadInterface = (LeaderIFC) reg.lookup(Constants.LEAD_NODE_NAME);
            m_name = "worker1";

            WorkerIFC workerInterface = (WorkerIFC) UnicastRemoteObject.exportObject(this, 0);
            reg.bind(m_name, workerInterface);


            System.out.println("WorkNode Ready!");


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
                m_leadInterface.releaseLock(new MyTransaction(n));
                Thread.sleep(1000);
                ++n;
            }
        }catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
        }

    }

    @Override
	public void abortTransaction() throws RemoteException {
		
		System.out.println("WorkNode Process abortTransaction()");
	}

    public void unblock(){
        System.out.println("WorkNode Process unblock()");
    }

    public void HelloWorker(String name) throws RemoteException{
        System.out.println("Work Node Says Hello " + name);
    }
}