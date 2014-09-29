import cgi
import urllib
import re
import os
import sys
import os.path

from google.appengine.api import users
from google.appengine.ext import ndb
from google.appengine.ext import db

import webapp2



from google.appengine.api import images
from google.appengine.api import urlfetch

from stream import Stream
from stream import Picture

SEARCH_PAGE_TEMPLATE ="""\
<!DOCTYPE html>
<html>
<body>
	<h1>Connex.us</h1>
	<table style="width:50%">
		<tr>
			<td><a href="management">Manage</td>
			<td><a href="createstream">Create</a></td>	
			<td><a href="viewallstream">View</a></td>
    		<td>Search</a></td>
    		<td><a href="trending">Trending</a></td>
    		<td><a href="social">Social</a></td>
   		</tr>
  	</table>

  	<form action="/showsearch" method="get">
        <input type="search" name="searchStream" placeholder="Lucknow"></br>
        <input type="submit" value="Search">
    </form>
</body>
</html>
"""
 

class searchView(webapp2.RequestHandler):
    def get(self):
        self.response.write(SEARCH_PAGE_TEMPLATE)

class showSearch(webapp2.RequestHandler):
    def get(self):
        url = self.request.url
        stream_name = re.findall('searchStream=(\S+)',url)
        if len(stream_name) == 0:
            self.response.write(url)
        else:
            stream_name = re.findall('searchStream=(\S+)',url)[0]
            streams = Stream.query().fetch()
            nameList = list()
            for stream in streams:
                nameList.append(stream.name)
            
            index = list()
            for i in xrange(len(nameList)):
                index.append(LCS(nameList[i], stream_name))
            tmp = zip(index, nameList)
            tmp.sort(reverse = True)
            #we only show five most relation streams
            if len(tmp) < 5:
                showNum = len(tmp)
            else:
                showNum = 5
            self.response.write(SEARCH_PAGE_TEMPLATE)
            self.response.write('<p>%d results for <b>%s</b>,<br>  click on image to view stream</p>' % (showNum,stream_name))
            for i in xrange(showNum):
                stream = Stream.query(Stream.name==tmp[i][1]).fetch()[0]
                #self.response.write(stream.numberofpictures)
                if stream.numberofpictures > 0:
                    pictures=db.GqlQuery("SELECT * FROM Picture " +"WHERE ANCESTOR IS :1 "+"ORDER BY uploaddate DESC",db.Key.from_path('Stream',stream.name))                   
                    self.response.write('<table border="1" style="width:100%"><table style = "width:10%">')
                    self.response.out.write('<td><div style = "position:relative;"><a href = "%s"><img src="img?img_id=%s" ></img><div style = "position: absolute; left:150px; top:20px"></a>%s</div></div></td>' % (stream.url, pictures[0].key(),stream.name))
                    self.response.write('</table>')
                else:
                    self.response.out.write('<td><div style = "position:relative;"><a href = "%s"><img src="http://www.estatesale.com/img/no_image.gif" ></img><div style = "position: absolute; left:150px; top:20px"></a>%s</div></div></td>' % (stream.url, stream.name))

def LCS(stringa, stringb):
    x = list()
    y = list()
    for  i in xrange(len(stringa)):
        x.append(stringa[i])
    for j in xrange(len(stringb)):
        y.append(stringb[j])
    if (len(x) == 0 or len(y) == 0):
        return 0
    else:
        a = x[0]
        b = y[0]
        if (a == b):
            return LCS(x[1:], y[1:])+1
        else:
            return cxMax( LCS(x[1:], y), LCS(x, y[1:] )  )

def cxMax(a, b):
    if (a>=b):
        return a
    else:
        return b

application = webapp2.WSGIApplication([
    ('/search', searchView),
    ('/showsearch', showSearch),
], debug=True)