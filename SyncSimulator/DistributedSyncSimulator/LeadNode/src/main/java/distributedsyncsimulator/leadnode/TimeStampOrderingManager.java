package distributedsyncsimulator.leadnode;

import java.util.*;

import distributedsyncsimulator.utilities.*;
import distributedsyncsimulator.shared.*;
import static distributedsyncsimulator.utilities.Constants.*;


public class TimeStampOrderingManager extends SyncManagerBase {

   

    public TimeStampOrderingManager(){
        super();
    }

    @Override
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
            if(checkLock(headLock, head).compareTo(LockStatus.PERMITTED) != 0){
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

    @Override
    public LockStatus acquireLocks(MyAction act){
        MyLog.instance().log("Start Acqure Lock for act = " + act + NEWLINE);


        String target = act.m_target;

        MyLock lk = act.getLock();
        LockStatus status = checkLock(lk, act);

        if(status.compareTo(LockStatus.PERMITTED) == 0){
            MyLock prev = getLock(act.m_tanscationId, target);
            if(prev == null){
                MyLog.instance().log("\tNo prev lock, set new lock" + NEWLINE);
                setLock(lk);
            }else{
                MyLog.instance().log("\tfind prev lock, updated Lock" + NEWLINE);
                prev.updateLock(lk.m_type);  
            }

        }
        else if(status.compareTo(LockStatus.IGNORE) == 0){

        }
        else if(status.compareTo(LockStatus.ROLLBACK) == 0){

        }
        else{
            // add new act to queues
            MyLog.instance().log("\tPut lock to waiting queue" + NEWLINE);
            if(!m_acts.containsKey(target)){
                m_acts.put(target, new ArrayList<MyAction>());
            }
            m_acts.get(target).add(act);
        }
        
        return status;
    }


    /*
        A transaction 
            try to READ X:
               If WT(X) > TS(T) then ROLLBACK
                Else If C(X) = false, then WAIT
                Else READ and update RT(X) to larger of TS(T) or RT(X)   
            try to WRITE X:
                If RT(X) > TS(T) then ROLLBACK
                Else if WT(X) > TS(T)
                    If C(X) = false then WAIT
                    ELSE IGNORE write (Thomas Write Rule)
                Otherwise, WRITE, and update WT(X)=TS(T), C(X)=false               
    */
    private LockStatus checkLock(MyLock lk, MyAction act){
        MyLog.instance().log("Start Compare Lock " + lk + NEWLINE);

        String target = lk.m_target;
        ArrayList<MyLock> locks = m_locks.get(target);

        if(locks == null){
            MyLog.instance().log("\tIt's a new Lock, Can be acquired" + NEWLINE);
            return LockStatus.PERMITTED;
        }

        switch(lk.m_type){
            case READ:
                for(MyLock curr : locks){
                    // same transaction, ok to give lock
                    if(curr.m_tansId.compareTo(lk.m_tansId) == 0){
                        continue;
                    }

                    // if there is a write lock on it, and write lock is older than read timestamp
                    if(curr.m_type.compareTo(MyLock.LockType.WRITE) == 0 && (curr.m_timestamp < act.m_transTimestamp)){
                        // rollback
                        MyLog.instance().log("\tRollback (Dirty Read). There is a WRITE lock at the item which is older than current Transaction's timestamp" + NEWLINE);
                        return LockStatus.ROLLBACK;
                    }
                }
                break;
            case WRITE:
                for(MyLock curr : locks){
                    // same transaction, ok to give lock
                    if(curr.m_tansId.compareTo(lk.m_tansId) == 0){
                        continue;
                    }

                    // if others aleady read it, you cannot change it as well
                    if(curr.m_type.compareTo(MyLock.LockType.READ) == 0 && (curr.m_timestamp < act.m_transTimestamp)){
                        // rollback
                        MyLog.instance().log("\tRollback (Dirty Read). There is a WRITE lock at the item which is older than current Transaction's timestamp" + NEWLINE);
                        return LockStatus.ROLLBACK;
                    }

                    // Thomas Write Rule
                    if(curr.m_type.compareTo(MyLock.LockType.WRITE) == 0 && (curr.m_timestamp < act.m_transTimestamp)){
                        // rollback
                        MyLog.instance().log("\tThomas Write Rule. Ignore and Continue" + NEWLINE);
                        return LockStatus.IGNORE;
                    }
                }
                break;
            default:
                return LockStatus.REJECT;
        }

        MyLog.instance().log("\tLock is acquirable" + NEWLINE);
        return LockStatus.PERMITTED;
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


        if(checkLock(acts.get(0).getLock(), acts.get(0)).compareTo(LockStatus.PERMITTED) != 0){
            return act;
        }

        act = acts.remove(0);
        if(acts.size() == 0){
            m_acts.remove(target);
        }
        
        return act;
    }

}