package distributedsyncsimulator.shared;

import static distributedsyncsimulator.utilities.Constants.*;
import distributedsyncsimulator.utilities.*;

import java.util.*;
import java.io.Serializable;

public class MyAction implements Serializable {

    public enum ActionType{
        UNKNOWN,
        READ,
        WRITE,
        ADD,
        MINUS
    };

    public String m_target;
    public UUID m_tanscationId;
    public ActionType m_actType = ActionType.UNKNOWN;
    public int m_value;
    public String m_workerName;
    public long m_timestamp;
    public long m_transTimestamp;

    public MyAction(UUID id, String workerName){
        m_tanscationId = id;
        m_workerName = workerName;
        m_timestamp = MyUtils.getTimestamp();
    }

    public MyAction(UUID id, String target, ActionType type){
        m_actType = type;
        m_tanscationId = id;
        m_target = target;
        m_timestamp = MyUtils.getTimestamp();
    }

    public MyAction(UUID id, String target, ActionType type, int value){
        m_actType = type;
        m_tanscationId = id;
        m_target = target;
        m_value = value;
        m_timestamp = MyUtils.getTimestamp();
    }

    public void setTransTimestamp(long ts){
        m_timestamp = ts;
    }

    public void setTarget(String t){
        m_target = t;
    }

    public void setType(ActionType t){
        m_actType = t;
    }

    public void setValue(int v){
        m_value = v;
    }

    /*
        return a lock with corresponding lock type
     */
    public MyLock getLock(){
        MyLock lk = new MyLock(m_tanscationId, m_target);

        switch(m_actType){
            case READ:
                lk.setLock(MyLock.LockType.READ);
                break;
            case WRITE:
                lk.setLock(MyLock.LockType.WRITE);
                break;
            default:
                MyLog.instance().log("Only READ and WRITE Action can acquire lock, but current = " + getTypeStr(m_actType) + NEWLINE);
                return null;
        }

       return lk; 
    }

    public static String getTypeStr(ActionType at){
        return at.name();
    }

    public String toString(){
        String actStr = String.format("MyAction [%s, %s, %s, %d]", getTypeStr(m_actType), m_target, m_tanscationId.toString(), m_value);
        return actStr;
    }

}