package distributedsyncsimulator.shared;

import static distributedsyncsimulator.utilities.Constants.*;

import java.util.*;
import java.io.Serializable;

public class MyConfiguration implements Serializable {

    public HashMap<String, String> m_nodeMap; // name, ip
    public HashMap<String, String> m_leads;
    public HashMap<String, String> m_workers;


    public MyConfiguration(){
        m_nodeMap = new HashMap<String, String>();
        m_leads = new HashMap<String, String>();
        m_workers = new HashMap<String, String>();

        init();
    }

    public void init(){
        
        m_nodeMap.put("LeadNode", "127.0.0.1");
        m_nodeMap.put("WorkerNode001", "127.0.0.1");
        m_nodeMap.put("WorkerNode002", "127.0.0.1");

        m_leads.put("LeadNode", "127.0.0.1");
        m_workers.put("WorkerNode001", "127.0.0.1");
        m_workers.put("WorkerNode002", "127.0.0.1");
    }

    public HashMap<String, String> getWorkers(){
        return m_workers;
    }

}