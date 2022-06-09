start rmiregistry -J-Djava.rmi.server.codebase=file:..\bin\
timeout 1

start cmd.exe /k java -classpath ..\bin -Djava.rmi.server.hostname=localhost distributedsyncsimulator.leadnode.LeadNode 0
timeout 1

start cmd.exe /k java -classpath ..\bin distributedsyncsimulator.worknode.WorkNode 001 ..\Transactions\test.txt 
start cmd.exe /k java -classpath ..\bin distributedsyncsimulator.worknode.WorkNode 002 ..\Transactions\test.txt