# OPTICS-Plugin
Clustering plugin for FlowJo built around the OPTICS algorithm

This is an example of a population plugin for FlowJo. 
The OPTICS algorithm re-orders the events and assigns a reachability distance
in order to identify the structure of the clusters in the data. From this, the
data can be sorted through manually, or run through a cluster detection algorithm.
This implementation currently just splits the clusters based on peaks in the data,
but other approaches are available.
