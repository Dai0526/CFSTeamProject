
package distributedsyncsimulator.utilities;

import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.*;
import java.text.SimpleDateFormat;


public class MyUtils{
    
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");

    public static String getTimestampStr()
	{
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return DATE_FORMAT.format(timestamp);
	}

	public static String getTimeStampStr(long time)
	{
		Timestamp timestamp = new Timestamp(time);
		return DATE_FORMAT.format(timestamp);
	}

	public static long getTimestamp(){
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return timestamp.getTime();
	}

	public static HashMap<String, Integer> getNodeInfo(String configPath){
		return new HashMap<String, Integer>();
	}

}