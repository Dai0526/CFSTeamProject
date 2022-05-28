
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

    public ArrayList<String> releaseLocks(MyTransaction trans) throws Exception{
        throw new Exception("release Locks not implementated");
    }

    public boolean acquireLocks(MyAction act) throws Exception{
        throw new Exception("release Locks not implementated");
    }
}