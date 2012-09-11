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
# This R script uses the igraph library v0.6 to perform various analyses
# on the whole social graph. Then, the giant component is extracted (more precisely:
# the list of nodes, which is recorded. The resulting file must then be processed
# with a Java tool to actually get the corresponding subnetwork itself).
# Note: written for igraph v0.6+
#
#####################################################################################

##################################################
# init
##################################################
library(igraph)
#folder <- paste("/home/vlabatut/eclipse/workspaces/Extraction/GooglePlusParser/res/networks/")
folder <- paste("/home/vlabatut/eclipse/workspaces/Extraction/Database/googleplus/")

# load network
net.file <- paste(folder,"edges.table",sep="")
g <- read.graph(net.file,format="edgelist")

# load properties
prop.file <- paste(folder,"whole.properties.txt",sep="")
prop.table <- as.matrix(read.table(prop.file))


##################################################
# process components
##################################################
nbr <- no.clusters(graph=g, mode="weak") # result: 115686 components
components <- cluster.distribution(graph=g, cumulative=FALSE, mul.size=FALSE, mode="weak")
temp <- components*nbr # largest: 30871966 nodes
indices <- which(temp>0) - 1
t <- cbind(indices,temp[indices])
write.table(x=t,file=paste(folder,"components.txt",sep=""),row.names=FALSE,col.names=FALSE)

components <- clusters(graph=g, mode="weak")
giantcomp.index <- which.max(components$csize)
indices <- which(components$membership==giantcomp.index)
write.table(x=indices,file=paste(folder,"subcomponent.txt",sep=""),row.names=FALSE,col.names=FALSE)
#g2 <- induced.subgraph(graph=g, vids=indices, impl="auto")


##################################################
# process degrees
##################################################
for(mode in c("all","in","out"))
{	p <- degree(g,mode=mode)
	rowname <- paste("degree.",mode,sep="")
	write.table(x=p,file=paste(folder,rowname,".txt",sep=""),row.names=FALSE,col.names=FALSE)
	temp <- rownames(prop.table)
	if(length(which(temp==rowname))==0)
	{	prop.table <- rbind(prop.table,c(mean(p),sd(p)))
		rownames(prop.table) <- c(temp,rowname)
	}
	else
	{	prop.table[rowname,"mean"] <- mean(p)
		prop.table[rowname,"stdev"] <- sd(p)
	}
}
write.table(x=prop.table,file=prop.file)
