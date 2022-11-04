# Distributed File Store

To use:

Create a controller : java Controller controllerPort fileReplicationFactor timeoutTime(ms) rebalancePeriod(s)

Create N distributed stores : java Dstore storePort controllerPort timeoutTime(ms) storeFolderName

Add files to be distributed to the downloads folder



To test

Linux : java -cp client.jar:. ClientMain controllerPort timeoutTime(ms)

Windows : java -cp client.jar;. ClientMain controllerPort timeoutTime(ms)
