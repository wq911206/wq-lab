import cgi
import urllib

from google.appengine.api import users
from google.appengine.ext import ndb
from google.appengine.ext import db
import webapp2
from stream import Stream
from stream import Picture

from google.appengine.ext import blobstore
from google.appengine.ext.webapp import blobstore_handlers

from google.appengine.api import images


VIEW_PAGE_TEMPLATE ="""\
<!DOCTYPE html>
<html>
<body>
	<h1>Connex.us</h1>
	<table style="width:50%">
		<tr>
			<td><a href="management">Manage</td>
			<td><a href="createstream">Create</a></td>	
			<td>View</a></td>
    		<td><a href="search">Search</a></td>
    		<td><a href="trending">Trending</a></td>
    		<td><a href="social">Social</a></td>
  		</tr>
  	</table>
  	<h2>View All Streams</h2>
    </body>
</html>
"""

class viewStreams(webapp2.RequestHandler):
  def get(self):
    self.response.write(VIEW_PAGE_TEMPLATE)
    streams=Stream.query(Stream.author==users.get_current_user()).order(-Stream.creattime).fetch()
    self.response.write('<table style = "width:100%">')
    index=0
    for stream in streams:
      #stream=Stream.query(Stream.name==stream.name).fetch()[0]
        if index==0:
            self.response.write("<tr>")
        self.response.out.write('<td><a href="%s"><div style = "position:relative;"><img src="%s" ></img><div style = "position: absolute; left:0px; top:0px">%s</div></div></a></td>' % (stream.url,stream.coverurl,stream.name))       
        if index==3:
            self.response.write("</tr>")
        index=index+1
    self.response.write("</table>")

application = webapp2.WSGIApplication([
    ('/viewallstream', viewStreams),   
], debug=True)