
Google+ Network Extractor - version 1

-----------------------------------------------------------------------

Copyright 2011-2012 Vincent Labatut
http:\\vincentlabatut.org
vincent.labatut@free.fr

Google+ Network Extractor is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation.

For source availability and license information see licence.txt

-----------------------------------------------------------------------

Description: This software was designed to retrieve the social network
behind the Google+ service. It was designed during summer 2011 and tested
starting from september 2011. At this time, there was no official documented 
G+ API, so I had to search the web for related programs. I found a very nice
piece of open source code by Jason Grey (cf. the parser package), which 
I used to access the relevant data: followers and followees. Note as of 2012,
the official Google+ API still does not document this feature (it focuses
on some personal data, by opposition to these relational ones).
I also needed to obtain the complete list of G+ users. For this, I used
some ideas from Paul Anthony and NgocChinh (cf. the explorer package). 

Here is how the program works: first it scans some publicly available Google 
files to retrieve all the Google ids (gid), which are unique 21 digits
numbers associated to each Google user (including people actually not using
Google+). Those are inserted in a Person table located in a H2 database.
Second, for each of these gid, it retrieves the lists of followers
and followees, also under the form of gid. These are inserted in a Relationship
table. To speed up the process, this part is multithreaded. Third, once all 
the data has been gathered, it exports the social network (basically the 
relationship table) under the form of an edgelist file (list of links, each 
one defined by the source and target nodes). 

The program was tested first to retrieve my ego-network (i.e. the people
around me, and those around them, etc.). Once the program was working, I 
started retrieving the whole G+ network. It took much longer than I thought
(around 2 months), for a network containing 80,080,893 nodes and 473,106,758 
links (not counting isolates). I initially had planned to retrieve the network 
every two months to track its evolution. However, Google eventually added a
captcha system, preventing me from reaching this goal (I started retrieving 
the November network, but could not finish). Finally, I decided to focus on 
what I had, and to just analyze the Sepbember-October network. The result is 
the following paper, which I ask you to cite if you use this software for 
academic purposes:
<TODO to be completed> 

Besides the functionalities exposed here, the software can also retrieve
other data from a gid, such as name, firstname, etc. I eventually did not
retrieve these because first I did not really need them, and second it was
making the whole process extremely long. 
Moreover, The res/rscript folder contains a R script able to convert
an edgelist file to a graphml file, so that you can play with it in 
Gephi (http://gephi.org/), for instance. The script requires the iGraph
library (http://igraph.sourceforge.net/). Note all those softwares are open source.

It is important knowing the captcha system does not completely prevent using
this software: you just cannot do too many requests from the same IP. It considerably
slows the process down, too much for my academic purposes... but it still is enough
to play with it a bit. Google might also raise this limit, one day, or provide
an academic access (which is not the case for this service, up to now).

Google might also change the structure of its JSON objects, or the way its (undocumented)
API is invoked, which would make the parser obsolete. Note I do not plan maintaining
this software and updating it if such changes were to happen. Please, do not contact
me for these matters. I think the Javadoc is complete enough to allow anyone adapting
the program to his needs. I would be interested to be informed of any use/modification,
though! 

If you plan to use this software to retreive the whole G+ network, note it
has an important limitation: gid are stored as string in the DB. Why strings
and not integers, since they are actually integers, you might ask? Well, the
DB was not able to store such large integers. A re-numbering of the modes
would have allowed to use much smaller values than those bloody 21 digits,
but I was not aware of that at this time. It turns out using strings was a
huge problem when I started processing the network (because of all the
memory it requires, of course). So I eventually had to renumber the nodes,
which took quite a while. I did not update the program though, since I did not
plan to use it again. In other words, this renumbering was performed completely
as a post-processing. The program still retrieves and store strings.

-----------------------------------------------------------------------

This product uses open source softwares:
  + H2 - Java SAL Database
    http://www.h2database.com
    Mozilla Public License
    Used to store the data retrived from Google+
	
  + Jersey (Sun/Oracle implementation of JAX-RS - Java API for RESTful Web Services)
    http://jersey.java.net/
    GPL2 License
    Used to query the Google+ service
    
  + Jettison - JSON StAX Implementation
    http://jettison.codehaus.org/
    Apache License v2 
    Used to handle the JSON objects
    
  + Log4j
    http://logging.apache.org/log4j
    Apache License v2 
    Used to log the program activity
  	
  + Some ideas from these blogs
    Paul Anthony: http://blog.webdistortion.com/2011/06/12/google-people-search-how-big-is-googles-social-network/
    NgocChinh: http://www.blackhatworld.com/blackhat-seo/black-hat-seo/315777-list-google-profiles-do-what-you-want.html
    Used to obtain all the Google+ ids

  + Some source code from Jason Grey in the following classes:
    GoogleJSONFilter
    GooglePlusParser
    https://plus.google.com/114074126238467553553/posts
    (A G+ post, incidentally!)  
