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

import jinja2
import os


JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)




class Trending(webapp2.RequestHandler):
    def get(self):
        
        #self.response.write(TRENDING_TEMPLATE)
        gl=Global.query(Global.name=="global").fetch()
        if(len(gl)>0):
            gl=gl[0]

        counts=CountViews.query(ancestor=ndb.Key('User',users.get_current_user().nickname())).order(-CountViews.numbers).fetch(3)

        infos=[]
        for count in counts:
            stream=Stream.query(Stream.author==users.get_current_user(), Stream.name==count.name).fetch()
            if(len(stream)>0):
                stream=stream[0]
                tmp=(stream.url,stream.coverurl,stream.name,count.numbers)
                infos.append(tmp)        

        
        gl=Global.query(Global.name=="global").fetch()
        str="No reports"
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
            #self.response.write("Current Update Frequency: "+ str)
        
        template_values={
            "infos": infos,
            "str": str,
        }
        template=JINJA_ENVIRONMENT.get_template("trending.html")
        self.response.write(template.render(template_values))
                

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