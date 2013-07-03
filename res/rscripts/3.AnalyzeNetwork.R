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
# on the social graph.
#
#####################################################################################

##################################################
# init
##################################################
library(igraph)
folder <- paste("/home/vlabatut/eclipse/workspaces/Extraction/Database/googleplus/")

# load network
#prefix <- "noisolates"
prefix <- "giantcomp"
net.file <- paste(folder,prefix,".edgelist",sep="")
g <- read.graph(net.file,format="edgelist")
#write.graph(g,paste(net.file,".net",sep=""),format="pajek")


# load properties
prop.file <- paste(folder,prefix,".properties.txt",sep="")
prop.table <- as.matrix(read.table(prop.file))
com.file <- paste(folder,prefix,".communities.txt",sep="")
com.table <- as.matrix(read.table(com.file))


##################################################
# process degrees
##################################################
for(mode in c("all","in","out","knn"))
{	gc()
	if(mode=="knn")
		p <- graph.knn(graph=g)$knn
	else
		p <- degree(g,mode=mode)
	rowname <- paste("degree.",mode,sep="")
	write.table(x=p,file=paste(folder,prefix,".",rowname,".txt",sep=""),row.names=FALSE,col.names=FALSE)
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
# process distances
##################################################
for(mode in c("undirected","directed"))
{	gc()
	if(mode=="undirected")
		p <- shortest.paths(graph=g,mode="all")
	else
		p <- shortest.paths(graph=g,mode="out")
	rowname <- paste("degree.",mode,sep="")
	write.table(x=p,file=paste(folder,prefix,".",rowname,".txt",sep=""),row.names=FALSE,col.names=FALSE)
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
# process diameters
##################################################
for(mode in c("directed", "undirected"))
{	if(mode=="undirected")
		p <- diameter(graph=g, directed=FALSE)
	else
		p <- diameter(graph=g, directed=TRUE)
	rowname <- paste("diameter.",mode,sep="")
	prop.table[rowname,"mean"] <- p
	prop.table[rowname,"stdev"] <- 0
}
write.table(x=prop.table,file=prop.file)

##################################################
# process centralities
##################################################
for(mode in c("betweenness.directed","betweenness.undirected","closeness"))
{	gc()
	if(mode=="betweenness.undirected")
		p <- betweenness(graph=g,directed=FALSE) / vcount(g)
	else if(mode=="betweenness.directed")
		p <- betweenness(graph=g,directed=TRUE) / vcount(g)
	else if(mode=="closeness")
		p <- closeness(graph=g, mode="all")
	rowname <- paste("degree.",mode,sep="")
	write.table(x=p,file=paste(folder,prefix,".",rowname,".txt",sep=""),row.names=FALSE,col.names=FALSE)
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
# process communities
##################################################
date()
# infomap
	algo.name <- "infomap"
	comstruct <- infomap.community (graph=g, e.weights=NULL, v.weights=NULL, nb.trials=1, modularity=TRUE)
# label propagation
	algo.name <- "labelpropagation"
	comstruct <- label.propagation.community(graph=g, weights=NULL, initial=NULL, fixed=NULL)
# walktrap
	algo.name <- "walktrap"
	walktrap.community(graph=g, weights=NULL, steps=4, merges=FALSE, modularity=TRUE, membership=TRUE)
# louvain
	algo.name <- "louvain"
	multilevel.community(graph=g, weights=NULL)
# fastgreedy
	algo.name <- "fastgreedy"
	g <- as.undirected(g, mode="collapse")	
	comstruct <- fastgreedy.community(graph=g, merges=FALSE, modularity=TRUE, membership=TRUE, weights=NULL)

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

# external Oslom
# cd /home/vlabatut/eclipse/workspaces/Networks/CommunityDetection/algorithms/oslom/
# ./oslom_dir -f ~/eclipse/workspaces/Extraction/Database/googleplus/giantcomp.edgelist -uw -r 1 -hr 0 -cp 0.5

# external louvain
# cd /home/vlabatut/eclipse/workspaces/Networks/CommunityDetection/algorithms/louvain/
# convert edgelist to binary:	./convert.out -i ~/eclipse/workspaces/Extraction/Database/googleplus/giantcomp.edgelist -o graph.bin
# detect communities: 			./community.out graph.bin -v -l -1 -v > graph.tree
# faster detection:				./community.out graph.bin -v -l -1 -q 0.0001 > graph.tree
# display resulting tree:		./hierarchy.out graph.tree

# external hierarchical infomap
# cd /home/vlabatut/eclipse/workspaces/Networks/CommunityDetection/algorithms/infohiermap/directed/
# ./infomap.out 1 ~/eclipse/workspaces/Extraction/Database/googleplus/giantcomp.net 1 1

# external infomap
# cd /home/vlabatut/eclipse/workspaces/Networks/CommunityDetection/algorithms/infomap/directed
# ./infomap.out 1 ~/eclipse/workspaces/Extraction/Database/googleplus/giantcomp.net 1

# external copra
# cd /home/vlabatut/eclipse/workspaces/Networks/CommunityDetection/algorithms/copra
# java -Xms31g -Xmx31g -cp copra.jar COPRA ~/eclipse/workspaces/Extraction/Database/googleplus/giantcomp.edgelist

# external MCL
# mcl ~/eclipse/workspaces/Extraction/Database/googleplus/giantcomp.edgelist -I 2.0