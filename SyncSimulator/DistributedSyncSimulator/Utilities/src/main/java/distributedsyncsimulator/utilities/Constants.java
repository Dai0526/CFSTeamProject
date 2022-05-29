package distributedsyncsimulator.utilities;

public final class Constants{

    public static enum LockStatus{
        PERMITTED(0),
        REJECT(1),
        ROLLBACK(2),
        IGNORE(3),
        ABORT(4);

        private final int value;

        LockStatus(int val){
            value = val;
        }

        public static LockStatus getValue(int value) {
            for(LockStatus e : LockStatus.values()) {
                if(e.value == value) {
                    return e;
                }
            }
            return LockStatus.ROLLBACK;// not found
        }
    };


    public static enum ActionStatus{
        PERMITTED(0),
        REJECT(1),
        ROLLBACK(2),
        IGNORE(3),
        ABORT(4);

        private final int value;

        ActionStatus(int val){
            value = val;
        }

        public static ActionStatus getValue(int value) {
            for(ActionStatus e : ActionStatus.values()) {
                if(e.value == value) {
                    return e;
                }
            }
            return ActionStatus.ROLLBACK;// not found
        }
    };

    public static final int DETECTION_INTERVAL_MS = 1000;
    public static final int RUN_INTERVAL_MS = 1000;
    public static final String LEAD_NODE_NAME = "LeadNode";
    public static final String WORK_NODE_NAME = "WorkerNode";

    public static final int DEFAULT_LEAD_PORT = 2345;
    public static final String HOST_IP = "127.0.0.1";

    public static final String NEWLINE = System.getProperty("line.separator");
}