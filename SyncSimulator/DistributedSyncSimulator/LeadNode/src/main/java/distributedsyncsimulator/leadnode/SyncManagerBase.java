import distributedsyncsimulator.ifc.*;
import distributedsyncsimulator.utilities.*;
import distributedsyncsimulator.shared.*;
import static distributedsyncsimulator.utilities.Constants.*;

import java.util.*;
public abstract class SyncManagerBase {

    private HashMap<String, ArrayList<MyLock>> m_locks;
	private HashMap<String, ArrayList<MyAction>> queueTable;

    public SyncManagerBase(){

    }

    public ArrayList<String> releaseLocks(MyTransaction trans){
        return new ArrayList<String>();
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