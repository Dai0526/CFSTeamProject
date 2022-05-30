#!/bin/sh
# start RMI
osascript -e 'tell app "Terminal" to do script "cd ~/Project/COEN283/DistributedSyncSimulator/LaunchScript && rmiregistry -J-Djava.rmi.server.codebase=file:bin/"'
sleep 1

# start lead
osascript -e 'tell app "Terminal" to do script "cd ~/Project/COEN283/DistributedSyncSimulator/LaunchScript && java -classpath ../bin -Djava.rmi.server.hostname=localhost distributedsyncsimulator/leadnode/LeadNode 0"'
sleep 1

# start worker
osascript -e 'tell app "Terminal" to do script "cd ~/Project/COEN283/DistributedSyncSimulator/LaunchScript && java -classpath ../bin distributedsyncsimulator.worknode.WorkNode 001 ../Transactions/trans1.txt"'
osascript -e 'tell app "Terminal" to do script "cd ~/Project/COEN283/DistributedSyncSimulator/LaunchScript && java -classpath ../bin distributedsyncsimulator.worknode.WorkNode 002 ../Transactions/trans2.txt"'


