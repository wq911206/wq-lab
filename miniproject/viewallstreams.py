import cgi
import urllib
import re

from google.appengine.api import users
from google.appengine.ext import ndb
from google.appengine.ext import db

import webapp2

from google.appengine.ext import blobstore
from google.appengine.ext.webapp import blobstore_handlers

from google.appengine.api import images
from google.appengine.api import urlfetch

from stream import Stream
from stream import Picture


VIEW_ALL_STREAMS_TEMPLATE ="""\
<!DOCTYPE html>
<html>
<body>
	<h1>Connex.us</h1>
	
	<table style="width:50%">
		<tr>
			<td><a href="management">Manage</a></td>
			<td><a href="createstream">Create</a></td>	
			<td><a href="viewallstream">View</a></td>
    		<td><a href="search">Search</a></td>
    		<td><a href="trenging">Trending</a></td>
    		<td><a href="social">Social</a></td>
  		</tr>
  	</table>
  	
</body>
</html>
"""

class  ViewAllStreamPage(webapp2.RequestHandler): 
    def get(self): 
        self.response.write(VIEW_ALL_STREAMS_TEMPLATE)
        self.response.write('<h2>View All Streams</h2>')
        streams=Stream.query().order(Stream.creattime).fetch()
        
        index=0
        self.response.write('<table border="1" style="width:100%">')
        for stream in streams:
            if(index==0):
                self.response.write('<tr>')
            #self.response.write('<td><img src="%s" title="wq"></td>'%stream.coverurl)
            self.response.write('<td><div style="background-image:url(%s) ; height: 300px; width: 300px;background-attachment: fixed;border: 1px solid black;">wq</div></td>'%stream.coverurl)
            if(index==3):
                self.response.write("</tr>")
            index=index+1
        self.response.write('</table>')
        
application = webapp2.WSGIApplication([
    ('/viewallstream', ViewAllStreamPage),
], debug=True)