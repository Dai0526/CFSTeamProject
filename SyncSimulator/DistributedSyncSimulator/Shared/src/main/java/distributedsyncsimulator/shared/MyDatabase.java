package distributedsyncsimulator.shared;

import java.util.*;

// singletone class for database manager
public class MyDatabase
{
    private static MyDatabase m_instance;
        
    private static HashMap<String, Integer> m_db;

    private MyDatabase(){
        m_db = new HashMap<String, Integer>();
    }

    public static synchronized MyDatabase instance(){
        if(m_instance == null){
            m_instance = new MyDatabase();
        }    

        return m_instance;
    }


    // if not exist, add item and init value 0
    public static int read(String key){
            
        if(m_db.get(key) == null){
            m_db.put(key, 0);
        }

        return m_db.get(key);
    }

    public static int write(String key, int val){
        m_db.put(key, val);
        return val;
    }

    public static int readAll(String nodeName){
        Iterator dbItr = m_db.entrySet().iterator();
        System.out.println(nodeName + "'s DB info are as follow: ");
        while(dbItr.hasNext()){
            Map.Entry element = (Map.Entry)dbItr.next();
            System.out.println("\t Key = " + element.getKey() + ", val = " + element.getValue());
        }

        return m_db.size();
    }
}