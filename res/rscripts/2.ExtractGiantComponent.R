#
# Google+ Network Extractor
# Copyright 2011-2012 Vincent Labatut 
#
# This file is part of Google+ Network Extractor.
# 
# Google+ Network Extractor is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 2 of the License, or
# (at your option) any later version.
# 
# Google+ Network Extractor is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with Google+ Network Extractor.  If not, see <http://www.gnu.org/licenses/>.
# 
#####################################################################################
#
# This R script uses the igraph library v0.6 to extract the giant component (more 
# precisely: the list of nodes, which is recorded. The resulting file must then be 
# processed with a Java tool to actually get the corresponding subnetwork itself).
#
#####################################################################################

# init
library(igraph)
folder <- paste("/home/vlabatut/eclipse/workspaces/Extraction/Database/googleplus/")
net.file <- paste(folder,"noisolates.edgelist",sep="")

##################################################
# network renumbering (in the previous version of igraph, 
# everything was numbered from 0)
##################################################
# load the network as a table
t<-read.table(net.file)
# update the numbering
t <- t + 1

# recored the modified table in place of the old one
options(scipen=30) # avoid using the scientific notation in the edgelist file
write.table(x=t, file=net.file, row.names=FALSE, col.names=FALSE, sep="\t")
options(scipen=0)

# get rid of the now unnecessary table
rm(t)
gc()


##################################################
# process components
##################################################
# load the actual network
g <- read.graph(net.file,format="edgelist")

# process the (weak) components
nbr <- no.clusters(graph=g, mode="weak") # result: 115686 components
components <- cluster.distribution(graph=g, cumulative=FALSE, mul.size=FALSE, mode="weak")
temp <- components*nbr # largest one: 30871966 nodes
indices <- which(temp>0) - 1 # minus one, because the first value corresponds to components of size zero
t <- cbind(indices,temp[indices])
# record the component size distribution
write.table(x=t,file=paste(folder,"components.sizes.txt",sep=""),row.names=FALSE,col.names=FALSE)
# get rid of the now unnecessary table
rm(t)
gc()

# identify weak components
components <- clusters(graph=g, mode="weak")
# get the giant one
giantcomp.index <- which.max(components$csize)
indices <- which(components$membership==giantcomp.index)
# write the corresponding nodes in a specific file
write.table(x=indices,file=paste(folder,"giantcomp.nodes",sep=""),row.names=FALSE,col.names=FALSE)
#g2 <- induced.subgraph(graph=g, vids=indices, impl="auto") # this requires too much memory


##################################################
# >> here, the Java class SubgraphExtractor is used
# to filter the files generated above, resulting in
# a new edgelist file containing only the giant component.
##################################################


##################################################
# When load in igraph, the resulting edgelist will
# be interpreted as having many isolates, due to
# the fact igraph uses the same numbering.
# Those must be removed, and the network recorded
# gain without them
##################################################
# load network
net.file <- paste(folder,"giantcomp.edgelist",sep="")
g <- read.graph(net.file,format="edgelist")

# remove isolates
d <- degree(graph=g,mode="all")
g <- delete.vertices(graph=g,v=which(d<1))
write.graph(graph=g, file=net.file, format="edgelist")

