
package distributedsyncsimulator.leadnode;

import distributedsyncsimulator.ifc.*;
import distributedsyncsimulator.utilities.*;
import distributedsyncsimulator.shared.*;
import static distributedsyncsimulator.utilities.Constants.*;

import java.util.*;
public abstract class SyncManagerBase {

    protected HashMap<String, ArrayList<MyLock>> m_locks;
	protected HashMap<String, ArrayList<MyAction>> m_acts;

    public SyncManagerBase(){
        m_locks = new HashMap<String, ArrayList<MyLock>>();
        m_acts = new HashMap<String, ArrayList<MyAction>>();
    }

    public MyLock getLock(UUID transId, String target){
        //System.out.println("DEBUG - getLock to looking for lock in records");
        ArrayList<MyLock> lks = m_locks.get(target);
        if(lks == null){
            //System.out.println("DEBUG - not found, reutrn null");
            return null;
        }

        for(MyLock lk : lks){
            if(lk.m_tansId.compareTo(transId) == 0){
                //System.out.println("DEBUG - found, return it " + lk);
                return lk;
            }
        }
        //System.out.println("DEBUG - not found, reutrn null");
        return null;
    }

    public boolean setLock(MyLock lock){
        //System.out.println("DEBUG - Start set lock");
        String target = lock.m_target;

        if(!m_locks.containsKey(target)) {
            //System.out.println("DEBUG - Not found, add a new one");
            m_locks.put(target, new ArrayList<MyLock>());
        }
        //System.out.println("DEBUG - adding lock to map, lock = " + lock);
	    return m_locks.get(target).add(lock);
    }

    // need Impl
    public ArrayList<String> releaseLocks(MyTransaction trans) throws Exception{
        throw new Exception("release Locks not implementated");
    }

    // need Impl
    public boolean acquireLocks(MyAction act) throws Exception{
        throw new Exception("release Locks not implementated");
    }
}