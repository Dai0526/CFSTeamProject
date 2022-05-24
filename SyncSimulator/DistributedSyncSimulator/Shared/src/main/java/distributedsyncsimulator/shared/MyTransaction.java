
package distributedsyncsimulator.shared;

//import java.sql.*;
import java.util.*;
import java.io.Serializable;


public class MyTransaction implements Serializable {

    public UUID m_id;
    public long m_timeCreated;
    public String m_workName;

    public ArrayList<MyAction> m_actions;
    public HashMap<String, Integer> m_commits; // saves intermediate values for a transaction
    public HashMap<String, Integer> m_changes;


    public MyTransaction(long time, String worker){
        m_id = UUID.randomUUID();
        m_timeCreated = time;
        m_workName = worker;

        m_actions = new ArrayList<>();
        m_commits = new HashMap<String, Integer>();
        m_changes = new HashMap<String, Integer>();
    }

    public void exec() throws Exception {
        for(MyAction act : m_actions){
            execSingleAct(act);
        }
    }

    public void execSingleAct(MyAction act) throws Exception {
        // TODO
        System.out.println("Exec Act " + act.toString());
    }

    public void addAction(MyAction act){
        m_actions.add(act);
    }

    public void setWorkerName(String id){
        m_workName = id;
    }

    // put all intermediate values to the database
    public void commit(){
        Iterator cmtItr = m_commits.entrySet().iterator();
        while(cmtItr.hasNext()){
            Map.Entry element = (Map.Entry)cmtItr.next();
            MyDatabase.instance().write((String)element.getKey(), (Integer)element.getValue());
        }
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