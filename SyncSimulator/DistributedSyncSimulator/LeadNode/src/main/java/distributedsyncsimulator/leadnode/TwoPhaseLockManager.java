package distributedsyncsimulator.leadnode;

import java.util.*;

import distributedsyncsimulator.utilities.*;
import distributedsyncsimulator.shared.*;
import static distributedsyncsimulator.utilities.Constants.*;

public class TwoPhaseLockManager extends SyncManagerBase {

    // private HashMap<String, ArrayList<MyLock>> m_locks;
    // private HashMap<String, ArrayList<MyAction>> m_acts;


    public TwoPhaseLockManager(){
        super();
    }


    public ArrayList<String> releaseLocks(MyTransaction trans){
        MyLog.instance().log("Start Release Lock for Transaction " + trans.m_id + NEWLINE);
    
        ArrayList<String> workers = new ArrayList<String>();
        ArrayList<MyLock> locks = new ArrayList<MyLock>(); // save locks that need to be released
        
        // find each lock item's key
        MyLog.instance().log("\tFind lock for each item which is acquired by the same transaction" + NEWLINE);
        Set<String> items = m_locks.keySet();
        for(String target : items){
            MyLog.instance().log("\t\tFind Lock for " + target + NEWLINE);
            ArrayList<MyLock> locked = m_locks.get(target);
            for(MyLock lk : locked){
                if(lk.m_tansId.compareTo(trans.m_id) == 0){
                    MyLog.instance().log("\t\t\tAdd Lock: " + lk + NEWLINE);
                    locks.add(lk);
                }
            }
        }

        // realse each locked locks
        MyLog.instance().log("\tStart Release Locks" + NEWLINE);
        for(MyLock curr : locks){
            // remove from record
            String key = curr.m_target;
            ArrayList<MyLock> targetLock = m_locks.get(key);

            MyLog.instance().log("\tTry Release Lock: " + curr + NEWLINE);
            for(MyLock lk : targetLock){
                if(lk.m_tansId.compareTo(curr.m_tansId) != 0){
                    continue;
                }

                m_locks.get(key).remove(lk);
                MyLog.instance().log("\t\tFound Lock in record, remove it" + NEWLINE);

                if(m_locks.get(key).size() == 0){
                    m_locks.remove(key);
                }

                break; // find the target lock, then stop;
            }


            // find linked actions
            MyAction head = getHeadAction(key);
            if(head == null){
                MyLog.instance().log("\tNo linked Actions found for this lock, skip" + NEWLINE);
                continue;
            }

            MyLock headLock = head.getLock();
            if(!checkLock(headLock)){
                continue;
            }

            MyLock prev = getLock(headLock.m_tansId, headLock.m_target);
            
            if(prev == null){
                //System.out.println("DEBUG - No prev lock, insert new one");
                MyLog.instance().log("\tNext Action has not been locked, acquire a new lock for it" + NEWLINE);
                setLock(headLock);
            }else{
                //System.out.println("DEBUG - found new one, updated");
                MyLog.instance().log("\tNext Action has a locked, update the lock" + NEWLINE);
                prev.updateLock(headLock.m_type);
            }

            //System.out.println("DEBUG - add woker to workers list");
            MyLog.instance().log("\tAdd Worker Node to Reacord" + NEWLINE);
            workers.add(trans.m_workName);
        }

        MyLog.instance().log("\tFinsh Releasing lock" + NEWLINE);

        return workers;
    }

    public LockStatus acquireLocks(MyAction act){

        MyLog.instance().log("Start Acqure Lock for act = " + act + NEWLINE);

        LockStatus stats = LockStatus.REJECT;
        String target = act.m_target;

        MyLock lk = act.getLock();
        if(checkLock(lk)){
            MyLock prev = getLock(act.m_tanscationId, target);
            if(prev == null){
                MyLog.instance().log("\tNo prev lock, set new lock" + NEWLINE);
                setLock(lk);
            }else{
                MyLog.instance().log("\tfind prev lock, updated Lock" + NEWLINE);
                prev.updateLock(lk.m_type);  
            }
            stats = LockStatus.PERMITTED;

        }else{
            // add new act to queues
            MyLog.instance().log("\tPut lock to waiting queue" + NEWLINE);
            if(!m_acts.containsKey(target)){
                m_acts.put(target, new ArrayList<MyAction>());
            }
            m_acts.get(target).add(act);
            stats = LockStatus.REJECT;
        }
        
        return stats;
    }

    private MyAction getHeadAction(String target){
        // System.out.println("DEBUG - Start getHeadAction " + target);
        MyLog.instance().log("Get Next Action from Queue" + NEWLINE);
        MyAction act = null;
        if(!m_acts.containsKey(target)){
            return act;
        }

        ArrayList<MyAction> acts = m_acts.get(target);
        if(acts.size() == 0){
            m_acts.remove(target);
            return act;
        }


        if(!checkLock(acts.get(0).getLock())){
            return act;
        }

        act = acts.remove(0);
        if(acts.size() == 0){
            m_acts.remove(target);
        }
        
        return act;
    }

    /*
        Check locks to be acquired
            1. if no locks ever been given(not found in records), reutrn true
            2. if found in records, check record's lock type
                a. read lock -> 
                    i. should from same transaction, AND
                    ii. the acqiure lock can only be WRITE or READ_WRITE
                b. Write Lock
                    i. should from same transaction             
    */
    private boolean checkLock(MyLock lk){
        MyLog.instance().log("Start Compare Lock " + lk + NEWLINE);

        String target = lk.m_target;
        ArrayList<MyLock> locks = m_locks.get(target);

        if(locks == null){
            MyLog.instance().log("\tIt's a new Lock, Can be acquired" + NEWLINE);
            return true;
        }

        switch(lk.m_type){
            case READ:
                for(MyLock curr : locks){
                    if(curr.m_tansId.compareTo(lk.m_tansId) != 0 && curr.m_type.compareTo(MyLock.LockType.READ) > 0){
                        MyLog.instance().log("\tCannot acquire, different transaction try to lock the same item with Lock Level higher than READ (READ, WRITE Conflict)." + NEWLINE);
                        return false;
                    }
                }
                break;
            case WRITE:
                //System.out.println("DEBUG - Compare Lock, it is a WRITE TYPE");
                for(MyLock curr : locks){
                    if(curr.m_tansId.compareTo(lk.m_tansId) != 0){
                        //System.out.println("DEBUG - two transaction hold the same lock, wrong");
                        MyLog.instance().log("\tCannot acquire, cannot have more than 1 transasction write to one item" + NEWLINE);
                        return false;
                    }
                }
                break;
            default:
                return false;
        }
        //System.out.println("DEBUG - Compare Lock return true");
        MyLog.instance().log("\tLock is acquirable" + NEWLINE);
        return true;
    }

    public ArrayList<String> abort(MyTransaction trans){
        ArrayList<String> unblockedNodes = null;
        try{
            cleanTransaction(trans);
            unblockedNodes = releaseLocks(trans);

        }catch(Exception ex){
            MyLog.instance().log("Abort Transaction failed: " + ex.getMessage() + NEWLINE);
        }
        return unblockedNodes;
    }

    public void cleanTransaction(MyTransaction trans){
        // Not Implemented
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


}