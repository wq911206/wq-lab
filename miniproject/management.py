import cgi
import urllib

from google.appengine.api import users
from google.appengine.ext import ndb
from google.appengine.ext import db
import webapp2
from stream import Stream
from stream import Picture
from stream import CountViews

from google.appengine.ext import blobstore
from google.appengine.ext.webapp import blobstore_handlers

from google.appengine.api import images

MANAGEMENT_PAGE_TEMPLATE ="""\
<!DOCTYPE html>
<html>
<body>
	<h1>Connex.us</h1>
	<table style="width:50%">
		<tr>
			<td>Manage</td>
			<td><a href="createstream">Create</a></td>	
			<td><a href="viewallstream">View</a></td>
    		<td><a href="search">Search</a></td>
    		<td><a href="trending">Trending</a></td>
    		<td><a href="social">Social</a></td>
  		</tr>
  	</table>
  	<h2>Streams I own</h2>
</body>
</html>
"""

class  ManagementPage(webapp2.RequestHandler): 
    def get(self):              
        dellsts=self.request.get_all("status")                
        if(len(dellsts)>0):
            streams=Stream.query(Stream.name.IN(dellsts), Stream.author==users.get_current_user()).fetch()
            for stream in streams:
                pictures=db.GqlQuery("SELECT * FROM Picture " +"WHERE ANCESTOR IS :1",db.Key.from_path('Stream',stream.name))
                db.delete(pictures)                  
            ndb.delete_multi(ndb.put_multi(streams))
        
        dellsts=self.request.get_all("status1") 
        #self.response.write(len(dellsts))               
        if(len(dellsts)>0):
            streams=Stream.query(Stream.name.IN(dellsts)).fetch()
            
            for stream in streams:
                if(users.get_current_user() and users.get_current_user().nickname() in stream.subscribers):
                    stream.subscribers.remove(users.get_current_user().nickname())
                    stream.put()
        
        self.response.write(users.get_current_user())
        self.response.write(MANAGEMENT_PAGE_TEMPLATE)
        streams=Stream.query(Stream.author==users.get_current_user()).order(-Stream.creattime).fetch()  
        
        self.response.write('<form action="/management" ,method="post"><table border="1" style="width:100%">')
        self.response.write('<tr><td>Name</td><td>Last New Picture</td><td>Number of Pictures</td><td>Delete</td></tr>')
        for stream in streams:
            self.response.write('<tr><td><a href="%s">%s</a></td><td>%s</td><td>%s</td><td><input type="checkbox" name="status", value="%s"></td></tr></form>' % (stream.url,stream.name,stream.lastnewdate,stream.numberofpictures,stream.name))
		
	self.response.write('</table>')	
	self.response.write('<input type="submit" value="Delete"></form>')
       
        self.response.write('<h2>Streams I Subscribe to</h2>')
        streams=Stream.query().fetch()        
        
        self.response.write('<form action="/management" ,method="post"><table border="1" style="width:100%">')
        self.response.write('<tr><td>Name</td><td>Last New Picture</td><td>Number of Pictures</td><td>Views</td><td>Delete</td></tr>')
        if(users.get_current_user()):
            for stream in streams:
                if(users.get_current_user().nickname() in stream.subscribers):
                    count=CountViews.query(CountViews.name==stream.name,ancestor=ndb.Key('User',stream.author_name)).fetch()[0]
                    self.response.write('<tr><td><a href="%s">%s</a></td><td>%s</td><td>%s</td><td>%s</td><td><input type="checkbox" name="status1", value="%s"></td></tr>' % (stream.guesturl,stream.name,stream.lastnewdate,stream.numberofpictures,count.numbers,stream.name))
		
	self.response.write('</table>')	
	self.response.write('<input type="submit" value="Delete"></form>')
        
        
        self.response.write('<br><a href=%s>Logout</a>'% users.create_logout_url(self.request.url))
        if not users.get_current_user():
            self.redirect('/',permanent=False)
        
            

class DeleteStreams(webapp2.RequestHandler):
    def get(self): 
        original_url=self.request.headers['Referer']
        dellsts=self.request.get_all("status")                
        if(len(dellsts)>0):
            streams=Stream.query(Stream.name.IN(dellsts), Stream.author==users.get_current_user()).fetch()
            for stream in streams:
                pictures=db.GqlQuery("SELECT * FROM Picture " +"WHERE ANCESTOR IS :1",db.Key.from_path('Stream',stream.name))
                db.delete(pictures)                  
            ndb.delete_multi(ndb.put_multi(streams))
        self.redirect(original_url) 
       
application = webapp2.WSGIApplication([
    ('/management', ManagementPage),
    ('/delstream', DeleteStreams),   
], debug=True)