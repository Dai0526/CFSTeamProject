package distributedsyncsimulator.leadnode;

import java.util.*;

import distributedsyncsimulator.utilities.*;
import distributedsyncsimulator.shared.*;
import static distributedsyncsimulator.utilities.Constants.*;

public class TwoPhaseLockManager{

    private Hashtable<String, ArrayList<MyLock>> m_locks;
    private Hashtable<String, ArrayList<MyAction>> m_acts;


    public TwoPhaseLockManager(){
        m_locks = new Hashtable<String, ArrayList<MyLock>>();
        m_acts = new Hashtable<String, ArrayList<MyAction>>();
    }


    public ArrayList<String> releaseLocks(MyTransaction trans){
        return new ArrayList<String>();
    }

    public boolean acquireLocks(MyAction act){
        return true;
    }

    public ArrayList<String> abort(UUID transId){
        return new ArrayList<String>();
    }

    public MyLock getLock(UUID transId, String target){
        return new MyLock(UUID.randomUUID(), "TODO");
    }

    public ArrayList<MyLock> getLocks(UUID transId){
        return new ArrayList<MyLock>();
    }

    public void addLock(MyLock lock){

    }

    public void checkLock(MyLock lock){

    }
}