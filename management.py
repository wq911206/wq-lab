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

import jinja2
import os


JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)


class  ManagementPage(webapp2.RequestHandler): 
    def get(self):              
        if not users.get_current_user():
            self.redirect('/',permanent=False)
        
        dellsts=self.request.get_all("status")                
        if(len(dellsts)>0 and users.get_current_user()):
            streams=Stream.query(Stream.name.IN(dellsts), Stream.author==users.get_current_user()).fetch()
            counts=CountViews.query(CountViews.name.IN(dellsts), ancestor=ndb.Key('User', users.get_current_user().nickname())).fetch()
            for stream in streams:
                pictures=db.GqlQuery("SELECT * FROM Picture " +"WHERE ANCESTOR IS :1",db.Key.from_path('User',users.get_current_user().nickname(),'Stream',stream.name))
                for picture in pictures:
                    blobstore.delete(picture.imgkey)
                db.delete(pictures)                  
            ndb.delete_multi(ndb.put_multi(streams))
            ndb.delete_multi(ndb.put_multi(counts))
        
        dellsts=self.request.get_all("status1")        
        if(len(dellsts)>0):
            streams=Stream.query(Stream.name.IN(dellsts)).fetch()            
            for stream in streams:
                if(users.get_current_user() and users.get_current_user().nickname() in stream.subscribers):
                    stream.subscribers.remove(users.get_current_user().nickname())
                    stream.put()
       
        infos=[]
        streams=Stream.query().fetch()
        if(users.get_current_user()):
            for stream in streams:
                if(users.get_current_user().nickname() in stream.subscribers):
                    count=CountViews.query(CountViews.name==stream.name,ancestor=ndb.Key('User',stream.author_name)).fetch()[0]
                    pictures=db.GqlQuery("SELECT * FROM Picture " +"WHERE ANCESTOR IS :1",db.Key.from_path('User',users.get_current_user().nickname(),'Stream',stream.name))
                    numberofpictures=0
                    for picture in pictures:
                        numberofpictures=numberofpictures+1
                    tmp=(stream.guesturl,stream.name,stream.lastnewdate,numberofpictures,count.totalviews,stream.name)
                    infos.append(tmp)
        
        streams=Stream.query(Stream.author==users.get_current_user()).order(-Stream.creattime).fetch()
        infos1=[]
        for stream in streams:
            numberofpictures=0
            pictures=db.GqlQuery("SELECT * FROM Picture " +"WHERE ANCESTOR IS :1",db.Key.from_path('User',users.get_current_user().nickname(),'Stream',stream.name))
            for picture in pictures:
                numberofpictures=numberofpictures+1
            tmp=(stream.url,stream.name,stream.lastnewdate,numberofpictures,stream.name)
            infos1.append(tmp)
        logout_url=users.create_logout_url(self.request.url)
        template_values={
            "infos1": infos1,
            "infos": infos,
            "logout_url": logout_url,
        }
        template=JINJA_ENVIRONMENT.get_template("management.html")
        self.response.write(template.render(template_values))
        
                    

            

class DeleteStreams(webapp2.RequestHandler):
    def get(self): 
        original_url=self.request.headers['Referer']
        dellsts=self.request.get_all("status")                
        if(len(dellsts)>0):
            streams=Stream.query(Stream.name.IN(dellsts), Stream.author==users.get_current_user()).fetch()
            for stream in streams:
                pictures=db.GqlQuery("SELECT * FROM Picture " +"WHERE ANCESTOR IS :1",db.Key.from_path('User',users.get_current_user().nickname(),'Stream',stream.name))
                db.delete(pictures)                  
            ndb.delete_multi(ndb.put_multi(streams))
        self.redirect(original_url) 
       
application = webapp2.WSGIApplication([
    ('/management', ManagementPage),
    ('/delstream', DeleteStreams),   
], debug=True)