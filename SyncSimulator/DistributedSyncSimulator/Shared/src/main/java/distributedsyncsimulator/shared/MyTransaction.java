
package distributedsyncsimulator.shared;

//import java.sql.*;
import java.util.*;
import java.io.Serializable;


public class MyTransaction implements Serializable {

    public UUID m_id;
    public long m_timeCreated;
    public String m_workName;

    public ArrayList<MyAction> m_actions;
    public HashMap<String, Integer> m_candidates; // saves intermediate values for a transaction
    public HashMap<String, Integer> m_cache; // cache the miuns and addition changes

    public MyTransaction(UUID id){
        m_id = id;
    }

    public MyTransaction(UUID id, String worker){
        m_id = id;
        m_workName = worker;
    }

    public MyTransaction(long time, String worker){
        m_id = UUID.randomUUID();
        m_timeCreated = time;
        m_workName = worker;

        m_actions = new ArrayList<>();
        m_candidates = new HashMap<String, Integer>();
        m_cache = new HashMap<String, Integer>();
    }

    public void exec() throws Exception {
        for(MyAction act : m_actions){
            execSingleAct(act);
        }
    }

    public void execSingleAct(MyAction act) throws Exception {
        System.out.println("Exec Act " + act.toString());
        String key = act.m_target;
        MyAction.ActionType type = act.m_actType;
        
        switch(type){
            case READ:
                int val = MyDatabase.instance().read(key);
                m_candidates.put(key, val);
                break;
            case WRITE:
                if(!m_cache.containsKey(key)){
                    m_cache.put(key, 0);
                }
                m_candidates.put(key, m_cache.get(key));
                //MyDatabase.instance().write(key, act.m_value);
                break;
            case ADD:
                int valAdded = MyDatabase.instance().read(key) + act.m_value;
                m_cache.put(key, valAdded);
                //MyDatabase.instance().add(key, act.m_value);
                break;
            case MINUS:
                int valMinused = MyDatabase.instance().read(key) - act.m_value;
                m_cache.put(key, valMinused);
                //MyDatabase.instance().minus(key, act.m_value);
                break;
            default:
                break;
        }
        System.out.println("Done Exec " + act.toString());
    }

    public void addAction(MyAction act){
        m_actions.add(act);
    }

    public void setWorkerName(String id){
        m_workName = id;
    }

    // put all intermediate values to the database
    public void commit(){
        Iterator cmtItr = m_candidates.entrySet().iterator();
        while(cmtItr.hasNext()){
            Map.Entry element = (Map.Entry)cmtItr.next();
            MyDatabase.instance().write((String)element.getKey(), (Integer)element.getValue());
        }

        // print database info
        MyDatabase.instance().readAll(m_workName);
    }

    public ArrayList<MyAction> getWrites(){
        ArrayList<MyAction> writes = new ArrayList<MyAction>();

        if(m_actions.size() <= 0){
            return writes;
        }

        for(MyAction act : m_actions){
            if(act.m_actType == MyAction.ActionType.WRITE){
                writes.add(act);
            }
        }

        return writes;
    }

    private String getActsStr(){
        StringBuilder sb = new StringBuilder();
        for(MyAction act : m_actions){
            sb.append("\r\n\t").append(act.toString());
        }

        return sb.toString();
    }

    public String toString(){
        String trasStr = String.format("MyTransaction [%s, %s, %s]", m_id.toString(), m_workName, getActsStr());
        return trasStr;
    }
}