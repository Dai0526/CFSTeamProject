package distributedsyncsimulator.shared;

import distributedsyncsimulator.utilities.*;
import java.util.concurrent.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.*;
import java.nio.file.*;
import static java.nio.file.StandardOpenOption.*;
public class MyLog{

    private static MyLog m_instance;
    private static SynchronousQueue<String> m_syncQueue;
    private static String m_filepath;
    protected static boolean m_isExit = false;
    private static LogHandler m_handler; 

    private static FileOutputStream m_outs = null;

    public MyLog(String name){
        try{
            m_syncQueue = new SynchronousQueue<String>();
            m_filepath = MyUtils.getCurrentDir() + "//" + name + "_" + MyUtils.getTimestampStr() + ".txt";

            m_outs = new FileOutputStream(m_filepath);
            
            
            System.out.println("Log Created at " + m_filepath);
            m_handler = new LogHandler();
            m_handler.setDaemon(true);
            m_handler.start();     

        }catch(IOException ioe){
            System.out.println("Log Created fail: " + ioe.getMessage());
        }
        catch(Exception e){
            System.out.println("Log Created fail: " + e.getMessage());
        }


        System.out.println("Log Instance Created");
    }

    public static void log(String msg){
        try {
            m_syncQueue.put(msg);
        } catch (InterruptedException iex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Unexpected interruption");
        }
    }

    static class LogHandler extends Thread {

        public LogHandler() { }
        
        public void run(){
            System.out.println("Start Handle msg in log");
            try {
                while (!m_isExit || !m_syncQueue.isEmpty()) {
                    String msg = m_syncQueue.take();
                    handleLog(msg);
                }
                m_outs.close();
                System.out.println("Log file closed");
            }
            catch(IOException ioe){
                System.out.println("Exception: " + ioe.getMessage());
                return;
            }
            catch (Exception ex) {
                return;
            }
        }

        public void handleLog(String msg) throws IOException{
            String timedMsg = String.format("[%s]  %s", MyUtils.getTimestampStr(), msg);
            System.out.println(timedMsg);
            m_outs.write(timedMsg.getBytes());
            m_outs.flush();
        }
    }


}