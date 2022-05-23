
import java.io.Serializable;
import java.util.*;

public class Configuration implements Serializable {

    private String m_path;
    private Hashtable<String, String> m_config;

    public Configuration(String path){
        m_path = path;
    }

    private void readConfigFromFile(){

    }


}