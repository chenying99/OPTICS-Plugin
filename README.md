# OPTICS-Plugin
Clustering plugin for FlowJo built around the OPTICS algorithm

This is an example of a population plugin for FlowJo. 

The OPTICS Algorithm was published in 1999 in a paper entitled "OPTICS: Ordering Points To Identify the Clustering Structure" by
Mihael Ankerst, Markus M. Breunig, Hans-Peter Kriegel, and Jörg Sander. 
Available here: http://fogo.dbs.ifi.lmu.de/Publikationen/Papers/OPTICS.pdf

The OPTICS algorithm re-orders the events and assigns a reachability distance in order to identify the structure of the clusters in the data. From this, the data can be sorted through manually, or run through a cluster detection algorithm. This implementation currently just splits the clusters based on peaks in the data, but other approaches are available.

One major weakness of this implementation is that it does not make use of a spatial index for the data points. A neighbor search currently takes O(n) time, and we need to do n neighbor searches. As Flow Cytometry data files can be extremely large, this is a problem that needs to be addressed in the future.

