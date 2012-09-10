#
# Google+ Network Extractor
# Copyright 2011 Vincent Labatut 
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
# on the social graph.
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
prop.file <- paste(folder,"properties.txt",sep="")
prop.table <- as.matrix(read.table(prop.file))
com.file <- paste(folder,"communities.txt",sep="")
com.table <- as.matrix(read.table(com.file))

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

##################################################
# process communities with infomap
##################################################
date()
# infomap
	comstruct <- infomap.community (graph=g, e.weights=NULL, v.weights=NULL, nb.trials=1, modularity=TRUE)
	algo.name <- "infomap"
# label propagation
	comstruct <- label.propagation.community(graph=g, weights=NULL, initial=NULL, fixed=NULL)
	algo.name <- "labelpropagation"
# walktrap
	walktrap.community(graph=g, weights=NULL, steps=4, merges=FALSE, modularity=TRUE, membership=TRUE)
	algo.name <- "walktrap"
# louvain
	multilevel.community(graph=g, weights=NULL)
	algo.name <- "louvain"
# fastgreedy
	g <- as.undirected(g)	
	comstruct <- fastgreedy.community(graph=g, merges=FALSE, modularity=TRUE, membership=TRUE, weights=NULL)
	algo.name <- "fastgreedy"

algo.file <- paste(folder,algo.name,".clu",sep="")
write.table(x=comstruct$membership,file=paste(folder,algo.name,".clu",sep=""),row.names=FALSE,col.names=FALSE)
temp <- rownames(com.table)
if(length(which(temp==algo.name))==0)
{	com.table <- rbind(com.table,c(length(comstruct),comstruct$modularity))
	rownames(com.table) <- c(temp,algo.name)
}else
{	com.table[rowname,"size"] <- length(comstruct)
	com.table[rowname,"modularity"] <- comstruct$modularity
}
write.table(x=com.table,file=com.file)


# ./oslom_dir -f ~/eclipse/workspaces/Extraction/Database/googleplus/edges.table -uw -r 1 -hr 0 -cp 0.5