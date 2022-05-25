package distributedsyncsimulator.leadnode;

import java.util.*;

import distributedsyncsimulator.utilities.*;
import distributedsyncsimulator.shared.*;
import static distributedsyncsimulator.utilities.Constants.*;

public class TwoPhaseLockManager{

    private HashMap<String, ArrayList<MyLock>> m_locks;
    private HashMap<String, ArrayList<MyAction>> m_acts;


    public TwoPhaseLockManager(){
        m_locks = new HashMap<String, ArrayList<MyLock>>();
        m_acts = new HashMap<String, ArrayList<MyAction>>();
    }


    public ArrayList<String> releaseLocks(MyTransaction trans){
        ArrayList<String> workers = new ArrayList<String>();
        
        // find lock for each item in db
        ArrayList<MyLock> locks = new ArrayList<MyLock>();
        System.out.println("DEBUG - init locks" );
        Set<String> items = m_locks.keySet();
        for(String target : items){
            System.out.println("DEBUG - Search " + target);
            ArrayList<MyLock> locked = m_locks.get(target);
            for(MyLock lk : locked){
                if(lk.m_tansId.compareTo(trans.m_id) == 0){
                    locks.add(lk);
                }
            }
        }
        System.out.println("DEBUG - lock size = " + locks.size());
        // realse each locked locks
        for(MyLock curr : locks){
            // remove from record
            String key = curr.m_target;
            ArrayList<MyLock> targetLock = m_locks.get(key);

            for(MyLock lk : targetLock){
                if(lk.m_tansId.compareTo(curr.m_tansId) != 0){
                    continue;
                }
                m_locks.get(key).remove(lk);

                if(m_locks.get(key).size() == 0){
                    m_locks.remove(key);
                }

                break; // find the target lock, then stop;
            }


            // find linked actions
            MyAction head = getHeadAction(key);
            if(head == null){
                System.out.println("DEBUG - getHeadAction null ");
                continue;
            }

            MyLock headLock = head.getLock();
            if(!compareLock(headLock)){
                continue;
            }

            MyLock prev = getLock(headLock.m_tansId, headLock.m_target);
            
            if(prev == null){
                System.out.println("DEBUG - No prev lock, insert new one");
                setLock(headLock);
            }else{
                System.out.println("DEBUG - found new one, updated");
                prev.updateLock(headLock.m_type);
            }

            System.out.println("DEBUG - add woker to workers list");
            workers.add(trans.m_workName);
        }

        return workers;
    }


    private MyAction getHeadAction(String target){
        System.out.println("DEBUG - Start getHeadAction " + target);
        MyAction act = null;
        if(!m_acts.containsKey(target)){
            return act;
        }

        ArrayList<MyAction> acts = m_acts.get(target);

        if(acts.size() == 0){
            m_acts.remove(target);
            return act;
        }


        if(!compareLock(acts.get(0).getLock())){
            return act;
        }

        act = acts.remove(0);
        if(acts.size() == 0){
            m_acts.remove(target);
        }
        

        return act;
    }

    private boolean compareLock(MyLock lk){
        System.out.println("DEBUG - Start Compare Lock " + lk);
        String target = lk.m_target;
        ArrayList<MyLock> locks = m_locks.get(target);

        if(locks == null){
            System.out.println("DEBUG - Lock not found " + lk);
            return true;
        }

        switch(lk.m_type){
            case READ:
                System.out.println("DEBUG - Compare Lock, it is a READ TYPE");
                for(MyLock curr : locks){
                    if(curr.m_tansId.compareTo(lk.m_tansId) != 0 && curr.m_type.compareTo(MyLock.LockType.READ) > 0){
                        return false;
                    }
                }
                break;
            case WRITE:
                System.out.println("DEBUG - Compare Lock, it is a WRITE TYPE");
                for(MyLock curr : locks){
                        System.out.println("DEBUG - curr lock transId = " + curr.m_tansId);
                        System.out.println("DEBUG - mylock lock transId = " + lk.m_tansId);
                    if(curr.m_tansId.compareTo(lk.m_tansId) != 0){
                        System.out.println("DEBUG - two transaction hold the same lock, wrong");
                        return false;
                    }
                }
                break;
            default:
                return false;
        }
        System.out.println("DEBUG - Compare Lock return true");
        return true;
    }

    public boolean acquireLocks(MyAction act){
        System.out.println("DEBUG - Start Acqure Lock for act = " + act);
        boolean stats = false;
        MyLock lk = act.getLock();
        System.out.println("DEBUG - get lock from act");
        String target = act.m_target;
        if(compareLock(lk)){
            MyLock prev = getLock(act.m_tanscationId, target);
            if(prev == null){
                System.out.println("DEBUG - no prev lock, set new lock");
                setLock(lk);
            }else{
                System.out.println("DEBUG - find prev lock, updated Lock for act " + act);
                prev.updateLock(lk.m_type);  
            }
            stats = true;

        }else{
            // add new act to queues
            if(!m_acts.containsKey(target)){
                System.out.println("DEBUG - Key not exist, add a new entry for it");
                m_acts.put(target, new ArrayList<MyAction>());
            }
            m_acts.get(target).add(act);
            stats = false;
        }
        System.out.println("DEBUG - Finish Acqure Lock for act = " + act);
        return stats;
    }

    public ArrayList<String> abort(MyTransaction trans){
        ArrayList<String> unblockedNodes = null;
        try{
            cleanTransaction(trans);
            unblockedNodes = releaseLocks(trans);

        }catch(Exception ex){
            System.out.print("Abort Transaction failed: " + ex.getMessage());
        }
        return unblockedNodes;
    }

    public void cleanTransaction(MyTransaction trans){

    }

    public MyLock getLock(UUID transId, String target){
        System.out.println("DEBUG - getLock to looking for lock in records");
        ArrayList<MyLock> lks = m_locks.get(target);
        if(lks == null){
            System.out.println("DEBUG - not found, reutrn null");
            return null;
        }

        for(MyLock lk : lks){
            if(lk.m_tansId.compareTo(transId) == 0){
                System.out.println("DEBUG - found, return it " + lk);
                return lk;
            }
        }
        System.out.println("DEBUG - not found, reutrn null");
        return null;
    }


    public boolean setLock(MyLock lock){
        System.out.println("DEBUG - Start set lock");
        String target = lock.m_target;

        if(!m_locks.containsKey(target)) {
            System.out.println("DEBUG - Not found, add a new one");
            m_locks.put(target, new ArrayList<MyLock>());
        }
        System.out.println("DEBUG - adding lock to map, lock = " + lock);
	    return m_locks.get(target).add(lock);
    }


}