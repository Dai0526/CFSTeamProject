# CFSTeamProject

[![Java 17 Ubuntu x64](https://github.com/Dai0526/CFSTeamProject/actions/workflows/Java17_Ubuntu_.yml/badge.svg)](https://github.com/Dai0526/CFSTeamProject/actions/workflows/Java17_Ubuntu_.yml)

[![Java 17 MacOS x64](https://github.com/Dai0526/CFSTeamProject/actions/workflows/Java17_MacOS_x64.yml/badge.svg)](https://github.com/Dai0526/CFSTeamProject/actions/workflows/Java17_MacOS_x64.yml)

[![Java 17 Windows x64](https://github.com/Dai0526/CFSTeamProject/actions/workflows/Java17_Win_x64_.yml/badge.svg)](https://github.com/Dai0526/CFSTeamProject/actions/workflows/Java17_Win_x64_.yml)

## Distributed Environment setup
1. Docker Container
  - CentOS
  - Total 3 containers
    - 1 master
    - 2 worker(sharding)

2. Communication Protocol
  - gRPC

3. Database (optional)
  - sqlite


## Dev Environment
1. Infrastructure
  - C++ 17

2. Compiling
  - makefile
  - visual studio
  
3. Ploting and Data analysis
  - Python 3


## Design and Modules

1. Master Node
  - rpc
  - job coordinator
  - run logger

2. Worker Node
  - rpc
  - sync factory
    - 2PL
    - wounded wait
    - timestamped
    - more
  - Algorithm is configurable

3. Algorithm
  - syncAlgoBase
  - syncAlgoImpls

4. logger
  - singleton
  - sync queue
  - format
    - timestamp
    - ip + port
    - hostname
    - what happened
  - timed event
    - ready
    - waiting
    - dispatched
  - event flag
    - Success
    - Abort
    - Failed
    - Warning

5. Runner
  - run 3 thread, each will read its own operations from file and keep send request to the 'database'

## Sanity Check
  1. a small set of data to check correctness
  2. large amount of operations to check performances
