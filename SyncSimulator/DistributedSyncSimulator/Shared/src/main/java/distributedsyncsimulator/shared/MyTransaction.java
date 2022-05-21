
package distributedsyncsimulator.shared;

import java.util.*;
import java.io.Serializable;


public class MyTransaction implements Serializable {

    public long m_id;
    public long m_workerId;
    public ArrayList<MyAction> m_actions;
    public HashMap<String, Integer> m_commits;
    public HashMap<String, Integer> m_changes;

    public MyTransaction(long id){
        m_id = id;
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

    public void setWorkerId(long id){
        m_workerId = id;
    }

    private String getActsStr(){
        StringBuilder sb = new StringBuilder();
        for(MyAction act : m_actions){
            sb.append("\t").append(act.toString()).append("line.separator");
        }

        return sb.toString();
    }

    public String toString(){
        String trasStr = String.format("MyLock [%d, %d, %s]", m_id, m_workerId, getActsStr());
        return trasStr;
    }
}