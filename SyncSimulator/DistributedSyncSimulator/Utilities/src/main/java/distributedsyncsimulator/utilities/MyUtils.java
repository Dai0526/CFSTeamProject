
package distributedsyncsimulator.utilities;


import java.util.Timer;
import java.util.TimerTask;
import java.util.*;
import java.text.SimpleDateFormat;

public class MyUtils{
    
    public static String getTimestamp()
	{
		return "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date()) + "]";
	}

	public static HashMap<String, Integer> getNodeInfo(String configPath){
		return new HashMap<String, Integer>();
	}

}