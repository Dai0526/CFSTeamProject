package distributedsyncsimulator.shared;


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
    private long m_tanscationId;
    private ActionType m_actType = ActionType.UNKNOWN;
    private int m_value;

    public MyAction(long id, String target, ActionType type){
        m_actType = type;
        m_tanscationId = id;
        m_target = target;
    }

    public MyAction(long id, String target, ActionType type, int value){
        m_actType = type;
        m_tanscationId = id;
        m_target = target;
        m_value = value;
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
        String actStr = String.format("MyAction [%s, %s, %d, %d]", getTypeStr(m_actType), m_target, m_tanscationId, m_value);
        return actStr;
    }

}