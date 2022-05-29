package distributedsyncsimulator.leadnode;

import java.util.*;

import distributedsyncsimulator.utilities.*;
import distributedsyncsimulator.shared.*;
import static distributedsyncsimulator.utilities.Constants.*;

public class SanpshotIsolationManager extends SyncManagerBase {
    public SanpshotIsolationManager() throws Exception {
        super();
        // prevent calling
        throw new Exception("Sanpshot Isolation Not Impelentation Error");
    }


    public ArrayList<String> releaseLocks(MyTransaction trans){
        MyLog.instance().log("Start Release Lock for Transaction " + trans.m_id + NEWLINE);
    
        
        return new ArrayList<String>();
    }

    public LockStatus acquireLocks(MyAction act){

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

}