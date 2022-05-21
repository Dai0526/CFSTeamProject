package distributedsyncsimulator.ifc;

import java.rmi.*;

public interface LeadIFC extends Remote{

    public void getLock(MyAction act);
}