package distributedsyncsimulator.shared;

import java.util.*;
import java.io.Serializable;

public class MyAction implements Serializable {

    public enum ActionType{
        UNKNOWN,
        READ,
        WRITE,
        UPDATE,
        DELETE
    };

    private String m_target;
    private UUID m_tanscationId;
    private ActionType m_actType = ActionType.UNKNOWN;
    private int m_value;

    public MyAction(UUID id){
        m_tanscationId = id;
    }

    public MyAction(UUID id, String target, ActionType type){
        m_actType = type;
        m_tanscationId = id;
        m_target = target;
    }

    public MyAction(UUID id, String target, ActionType type, int value){
        m_actType = type;
        m_tanscationId = id;
        m_target = target;
        m_value = value;
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
    public MyLock getLock() throws Exception {
        MyLock lk = new MyLock(m_tanscationId, m_target);

        switch(m_actType){
            case READ:
                lk.setLock(MyLock.LockType.READ);
                break;
            case WRITE:
                lk.setLock(MyLock.LockType.WRITE);
            default:
                throw new Exception("Only READ and WRITE Action can acquire lock, but current act type is " + getTypeStr(m_actType));
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