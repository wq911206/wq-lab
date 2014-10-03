import cgi
import urllib
import re

from google.appengine.api import users
from google.appengine.ext import ndb
from google.appengine.ext import db
from google.appengine.api import mail

import webapp2

from google.appengine.ext import blobstore
from google.appengine.ext.webapp import blobstore_handlers

from google.appengine.api import images
from google.appengine.api import urlfetch

from stream import Stream
from stream import Picture
from stream import Global
from stream import CountViews


TRENDING_TEMPLATE ="""\
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
    		<td>Trending</td>
    		<td><a href="social">Social</a></td>
  		</tr>
  	</table>
  	<h2>Top 3 Trending Streams</h2>
</body>
</html>
"""


class Trending(webapp2.RequestHandler):
    def get(self):
        
        self.response.write(TRENDING_TEMPLATE)
        gl=Global.query(Global.name=="global").fetch()
        if(len(gl)>0):
            gl=gl[0]
            #self.response.write(gl.count)
            #self.response.write(gl.limit)
        #streams=Stream.query(Stream.author==users.get_current_user()).order(-Stream.views).fetch(3)
        counts=CountViews.query(ancestor=ndb.Key('User',users.get_current_user().nickname())).order(-CountViews.numbers).fetch(3)

        self.response.write('<table style = "width:70%">')
        for count in counts:
            stream=Stream.query(Stream.author==users.get_current_user(), Stream.name==count.name).fetch()
            if(len(stream)>0):
                stream=stream[0]
                self.response.out.write('<td><div style = "position:relative;"><a href="%s"><img src="%s" ></img><div style = "position: absolute; left:0px; top:0px">%s</div></a></div><div>%s views in the past hour</div></td>' % (stream.url,stream.coverurl,stream.name,count.numbers))
         
        self.response.write('</table>')
        
        self.response.write('<form action="/update" method="post">')
        self.response.write('<input type="radio" name="frequency" value="no">No reports<br>')
        self.response.write('<input type="radio" name="frequency" value="5m">Every 5 minutes<br>')
        self.response.write('<input type="radio" name="frequency" value="1h">Every 1 hour<br>')
        self.response.write('<input type="radio" name="frequency" value="1d">Every day<br>')        
        self.response.write('<input type="submit" value="Update rate"></form>')
        
        gl=Global.query(Global.name=="global").fetch()
        if(len(gl)>0):
            gl=gl[0]
            fre=gl.limit
            if fre==0:
                str="No reports"
            if fre==1:
                str="Every 5 Minutes"
            if fre==12:
                str="Every 1 hour"
            if fre==288:
                str="Every day"
            self.response.write("Current Update Frequency: "+ str)
                

class Update(webapp2.RequestHandler):
    def post(self):
        original_url=self.request.headers['Referer']
        frequency=self.request.get("frequency")
        gl=Global.query(Global.name=="global").fetch()
        self.response.write(gl)
        if len(gl)<1:
            gl=Global(name="global",count=0,limit=0)
        else:
            gl=gl[0]
        
        #self.response.write(frequency)
        if frequency=="no":
            gl.limit=0
        if frequency=="5m":
            gl.limit=1
        if frequency=="1h":
            gl.limit=12
        if frequency=="1d":
            gl.limit=288
        #self.response.write(gl)
        gl.put()
        self.redirect(original_url)

class Task(webapp2.RequestHandler):
    def get(self):
        #if users.get_current_user():
        gl=Global.query(Global.name=="global").fetch()
        if(len(gl)>0):
            gl=gl[0]
            gl.count=gl.count+1
            if(gl.count==gl.limit):
                gl.count=0
                if users.get_current_user():
                    default_context = "Stream Trending Updated\n\n"
                    emailSubject = "UserID: " + users.get_current_user().nickname()
                    emailSender = "wq911206@gmail.com"
                    emailReceiver="wangqi911206@gmail.com"
                    mail.send_mail(sender = emailSender, to = emailReceiver, subject = emailSubject, body = default_context)
            gl.put()
    
class Clean(webapp2.RequestHandler):
    def get(self):        
        #if users.get_current_user():
            #print "wq"
        counts=CountViews.query().fetch()
        for count in counts:
            count.numbers=0
            count.put()      

application = webapp2.WSGIApplication([
    ('/trending', Trending),  
    ('/update', Update), 
    ('/task', Task),
    ('/clean', Clean),
], debug=True)