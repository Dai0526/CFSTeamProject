package distributedsyncsimulator.shared;
import java.util.*;

public class MyLock{

    public enum LockType{
        UNKNOWN,
        READ,
        WRITE,
        READ_WRITE
    };

    public String m_target;
    public LockType m_type = LockType.UNKNOWN;
    public UUID m_tansId;

    public MyLock(UUID tid, String target){
        this.m_target = target;
        this.m_tansId = tid;
    }

    public MyLock(LockType t, UUID tid, String target){
        this.m_type = t;
        this.m_target = target;
        this.m_tansId = tid;
    }

    public void setLock(LockType type){
        m_type = type;
    }

    public void updateLock(LockType newType){
        if (m_type == LockType.READ_WRITE || m_type == newType) {
		    return;
	    }
        // chage lock type to rw only when 
        //  1. r + w 
        //  2. w + r 
	    m_type = LockType.READ_WRITE;
    }

    public static String getTypeStr(LockType lt){
        return lt.name();
    }

    public String toString(){
        String lkStr = String.format("MyLock [%s, %s, %s]", getTypeStr(m_type), m_target, m_tansId);
        return lkStr;
    }

}