# CFSTeamProject

[![Linux x64](https://github.com/Dai0526/CFSTeamProject/actions/workflows/Java17_Ubuntu_.yml/badge.svg)](https://github.com/Dai0526/CFSTeamProject/actions/workflows/Java17_Ubuntu_.yml)
[![MacOS x64](https://github.com/Dai0526/CFSTeamProject/actions/workflows/Java17_MacOS_x64.yml/badge.svg)](https://github.com/Dai0526/CFSTeamProject/actions/workflows/Java17_MacOS_x64.yml)
[![Windows x64](https://github.com/Dai0526/CFSTeamProject/actions/workflows/Java17_Win_x64_.yml/badge.svg)](https://github.com/Dai0526/CFSTeamProject/actions/workflows/Java17_Win_x64_.yml)

## **Modules**

|- Shared  
|- Interface  
|- Utilities  
|- LeadNode  
|- WorkNode  


|- Transactions
|- log
|- 

## **Design**

TODO
****

### **Distributed Environment setup**
Java Remote Method Invocation (**RMI**)
  - Server
  - Client

### **Virtualized In-Memory Database**
Though the distributed environment is designed based on the distributed database system, the focus is on the Synchronization Algorithm simulation. Thus, it is not necessary to implement a middleware to communicate with real database. Instead, we can abstact the concpet of database by simulated it with a key-value in memory DB.

[MyDatabaseManager.java](SyncSimulator\DistributedSyncSimulator\Shared\src\main\java\distributedsyncsimulator\shared\MyDatabase.java)
* Designed as a singleton, such that one worker node can only monitor/operate one databse
* Use a hashmap to simulate the key-value db
  * key - String
  * value - integer
* It support 4 operations
  1. read
  2. write
  3. add
  4. minus

****

## **Input Data - Transactions**

A transaction can contains several operations, and each action is seperated by delimeter `-`
1. read - r
2. write - w
3. plus - p
4. minus - m

The pattern for **read** and **write** operations are as follow:  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`<r/w>-<key>`.  
For example, `r-Apple` means read Apple's value.  


The pattern for **plus** and **minus** operations are as follow:  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`<p/m>-<key>-<value>`.  
For example, `p-Apple-9` means add 9 to Apple.

A typic transaction example is as follow:
```
r-Apple,p-Apple-9,w-Apple,r-Orange,p-Orange-9,w-Orange
```

There are two sets of input Transactions:  
1. **trans<#>.txt**
    * the number means the worker number. e.g. trans1.txt will be consumed by worker1
    * It is generate by script.
    * Current version is designed by purpose to avoid deadlock
2. **test.txt**
    * this short transaction lists are design for sanity check
    * It is used to check if the program is running as designed.
****
## **Output data - Log**

[MyLog.java](SyncSimulator\DistributedSyncSimulator\Shared\src\main\java\distributedsyncsimulator\shared\MyDatabase.java)
* A singleton
* synchornous queue to save logs
* A object called ThreadHandler to fetch messages from sync_queue
  * print to screen
  * write to log file

The log file will be saved in `SyncSimulator\DistributedSyncSimulator\log`  
Each node will generated its own logs. For example, if the test runs with 1 `LeadNode` and 2 `WorkerNode`, the log files will be like something similiar to the following:  
  * LeadNode Log  
      * Pattern: _LeadNode_timestamp.log_
      * Example: _LeadNode_20220527_00-52-13265.log_
  * WorkerNode Log
      * Pattern: _WorkerNodeNNN_timestamp.log_
      * Example: 
        * _WorkerNode001_20220527_00-52-32456.log_ 
        * _WorkerNode002_20220527_00-52-32457.log_

****
## **Usage**

### I. **Prerequsite**
Install **gradle** and **Java**, and set the environment proper. You can verify the installation by enter the following command in cmd. It should be able to show the verison infomations for both.
```bash
> gradle --version

------------------------------------------------------------
Gradle 7.4.2
------------------------------------------------------------

Build time:   2022-03-31 15:25:29 UTC
Revision:     540473b8118064efcc264694cbcaa4b677f61041

Kotlin:       1.5.31
Groovy:       3.0.9
Ant:          Apache Ant(TM) version 1.10.11 compiled on July 10 2021
JVM:          17.0.2 (Eclipse Adoptium 17.0.2+8)
OS:           Windows 10 10.0 amd64

> java --version
openjdk 17.0.2 2022-01-18
OpenJDK Runtime Environment Temurin-17.0.2+8 (build 17.0.2+8)
OpenJDK 64-Bit Server VM Temurin-17.0.2+8 (build 17.0.2+8, mixed mode, sharing)

```

### **II. Compile**
1. Launch CMD and naviagte to _SyncSimulator\DistributedSyncSimulator_
    ```bash
    > cd SyncSimulator\DistributedSyncSimulator
    ```
2. Build project using gradle 
    ```bash
    > gradle build
    ```

3. The compiled class file is located at `bin` file at _SyncSimulator\DistributedSyncSimulator\bin\_

### **III. Run**
0. Launch CMD and naviagte to _SyncSimulator\DistributedSyncSimulator_
1. Launch LeadNode
    * Manually enter command in cmd
    ```bash
    > java -classpath bin -Djava.rmi.server.hostname=localhost distributedsyncsimulator.leadnode.LeadNode
    ```
    * In windows, you can run script [StartLeadNode.bat](SyncSimulator\DistributedSyncSimulator\StartLeadNode.bat)
    ```bash
    > StartLeadNode.bat
    ```
2. Launch Worker Node
    * Launch a single worker node by using command
      ```bash
      # launch worker node name WorkerNode001, and comsumes transaction test.txt
      > start cmd.exe /k java -classpath bin distributedsyncsimulator.worknode.WorkNode 001 Transactions\test.txt
      ```
    * In windows, you can run script [StartWorkeNode.bat](SyncSimulator\DistributedSyncSimulator\StartWorkeNode.bat)
      ```bash
      > StartLeadNode.bat
      ```

    * In windows, you can run script [StartWorkeNodes.bat](SyncSimulator\DistributedSyncSimulator\StartWorkeNodes.bat) to launch 2 WorkerNodes:
      * WorkerNode001
        * consumes trans1.txt 
      * WorkerNode001
        * consumes trans2.txt
      ```bash
      > StartLeadNodes.bat
      ```

3. Output and Log
The log file will be saved in `SyncSimulator\DistributedSyncSimulator\log`  
Each node will generated its own logs. For example, if the test runs with 1 LeadNode and 2 WorkerNode, the log files will be like something similiar to the following:  
    * LeadNode Log  
      * Pattern: _LeadNode_<timestamp>.log
      * Example: _LeadNode_20220527_00-52-13265.log_
    * WorkerNode Log
      * Pattern: _WorkerNode<worker#>_<timestamp>.log
      * Example: 
        * _WorkerNode001_20220527_00-52-32456.log_ 
        * _WorkerNode002_20220527_00-52-32457.log_





****
## Tools
* Programming Language:
  * Java 17
  * Python 3
  * Batch
* 3rd Party Library: N/A
* Build Tool
  * Gradle 7.4
* OS tested
  * Windows 10 x64

