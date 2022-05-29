package distributedsyncsimulator.shared;

import static distributedsyncsimulator.utilities.Constants.*;

import java.util.*;
import java.lang.*;
// singletone class for database manager
public class MyDatabase
{
    private static MyDatabase m_instance;
        
    private static HashMap<String, Integer> m_db;

    private MyDatabase(){
        m_db = new HashMap<String, Integer>();
        MyLog.instance().log("DB instance Created" + NEWLINE);
    }

    public static synchronized MyDatabase instance(){
        if(m_instance == null){
            m_instance = new MyDatabase();
        }    

        return m_instance;
    }

    public static void add(String key, int value){
        if(m_db.get(key) == null){
            m_db.put(key, value);
            return;
        }

        m_db.put(key, m_db.get(key) + value);
    }

    public static void minus(String key, int value){
        if(m_db.get(key) == null){
            m_db.put(key, -value);
            return;
        }

        m_db.put(key, m_db.get(key) - value);
    }

    // if not exist, add item and init value 0
    public static int read(String key){
            
        if(m_db.get(key) == null){
            m_db.put(key, 0);
        }

        return m_db.get(key);
    }

    public static void write(String key, int val){
        m_db.put(key, val);
        MyLog.instance().log("DB write [" + key + ", " + val + "]"+ NEWLINE);
    }

    public static int readAll(String nodeName){

        StringBuilder sb = new StringBuilder();
        sb.append(nodeName + "'s DB info are as follow: " + NEWLINE);
        sb.append("\t DB size = " + m_db.size() + NEWLINE);
        Iterator dbItr = m_db.entrySet().iterator();
        
        while(dbItr.hasNext()){
            Map.Entry element = (Map.Entry)dbItr.next();
            sb.append("\t Key = " + element.getKey() + ", val = " + element.getValue() + NEWLINE);
        }

        MyLog.instance().log(sb.toString() + NEWLINE);
        return m_db.size();
    }
}