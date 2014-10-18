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
from stream import CountViews

import jinja2
import os
import json


JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)

class  ViewSinglePage(webapp2.RequestHandler): 
    def get(self):                                                           
        stream_name=re.findall('streamname%3D(.*)',self.request.url)[0]      
        stream=Stream.query(Stream.name==stream_name, Stream.author==users.get_current_user()).fetch()[0]
        pictures=db.GqlQuery("SELECT *FROM Picture " + "WHERE ANCESTOR IS :1 " +"ORDER BY uploaddate DESC LIMIT 3" , db.Key.from_path('User',users.get_current_user().nickname(),'Stream',stream_name))	        
        showmoreurl=urllib.urlencode({'showmore': stream.name+"=="+users.get_current_user().nickname()}) 
        geoviewurl=urllib.urlencode({'geoview': stream.name+"=="+users.get_current_user().nickname()})
        template_values={
            "stream_name": stream_name,
            "pictures": pictures,
            "showmoreurl": showmoreurl,
            "geoviewurl": geoviewurl,
        }
        template=JINJA_ENVIRONMENT.get_template("viewsinglestream1.html")
        self.response.write(template.render(template_values))
        
class PicAPI(webapp2.RequestHandler):
    def post(self):   
        stream_name=self.request.get("stream_name")
        stream=Stream.query(Stream.name==stream_name, Stream.author==users.get_current_user()).fetch()[0] 
        pictures=db.GqlQuery("SELECT *FROM Picture " + "WHERE ANCESTOR IS :1 " +"ORDER BY uploaddate DESC LIMIT 3" , db.Key.from_path('User',users.get_current_user().nickname(),'Stream',stream_name))
        keys=[]
        for picture in pictures:
            keys.append(str(picture.key()))
        
        keys=json.dumps(keys)
        self.response.headers['Content-Type']="application/json"
        self.response.write(keys)
                          

class Image(blobstore_handlers.BlobstoreDownloadHandler):
    def get(self):     
        print self.request.get('img_id')
        picture = db.get(self.request.get('img_id'))
        blob_info = blobstore.BlobInfo.get(picture.imgkey)
        self.send_blob(blob_info)


class Upload(blobstore_handlers.BlobstoreUploadHandler):
    def post(self):        
        original_url=self.request.headers['Referer']
        imgs = self.get_uploads('files[]')
        if len(imgs) != 0:     
            stream_name=re.findall('=(.*)',original_url)[0]  
            stream=Stream.query(Stream.name==stream_name, Stream.author==users.get_current_user()).fetch()[0]  
            for img in imgs:         
                picture=Picture(parent=db.Key.from_path('Stream',stream_name))
                stream.lastnewdate=  picture.uploaddate
                stream.numberofpictures=stream.numberofpictures+1
                stream.total=stream.total+1         
                picture.id=str(stream.total)
                picture.imgkey=str(img.key())
                picture.put()
            stream.put()
        self.redirect(original_url)

class DeletePictures(webapp2.RequestHandler):
    def post(self): 
        original_url=self.request.headers['Referer']
        stream_name=re.findall('=(.*)%3D%3D',original_url)[0]
        stream=Stream.query(Stream.name==stream_name, Stream.author==users.get_current_user()).fetch()[0]
        dellsts=self.request.get_all("status")
        pictures=db.GqlQuery("SELECT * FROM Picture " +"WHERE ANCESTOR IS :1 AND imgkey IN :2",db.Key.from_path('User',users.get_current_user().nickname(),'Stream',stream_name),dellsts)
        for picture in pictures:
            blobstore.delete(picture.imgkey)
        db.delete(pictures)
        #stream.numberofpictures=stream.numberofpictures-len(dellsts)
        #stream.put()
        self.redirect(original_url)

class SubscribeStream(webapp2.RequestHandler):
    def post(self): 
        original_url=self.request.headers['Referer']
        
        stream_name=re.findall('=(.*)%3D%3D',original_url)
        if(len(stream_name)<1):
            stream_name=re.findall('%3D(.*)%3D%3D',original_url)[0]
        else:
            stream_name=stream_name[0]
        
        user_name=re.findall('%3D%3D(.*)\?',original_url)
        if(len(user_name)<1):
            user_name=re.findall('%3D%3D(.*)',original_url)[0]
        else:
            user_name=user_name[0]
        
        user_name=user_name.split('%40')
        if(len(user_name)>1):
            user_name=user_name[0]+'@'+user_name[1]
        else:
            user_name=user_name[0]
        
        stream=Stream.query(Stream.name==stream_name, Stream.author_name==user_name).fetch()[0]
        
        if users.get_current_user():
            stream.subscribers.append(users.get_current_user().nickname())
        stream.put()
        self.redirect(original_url)
        

class ShowPictures(webapp2.RequestHandler):
    def get(self): 
        
        stream_name=re.findall('%3D(.*)%3D%3D',self.request.url)[0]
        user_name=re.findall('%3D%3D(.*)',self.request.url)[0]
        index=0
        stream=Stream.query(Stream.name==stream_name, Stream.author_name==user_name).fetch()[0]
        count=CountViews.query(CountViews.name==stream.name,ancestor=ndb.Key('User',stream.author_name)).fetch()[0]
        count.numbers=count.numbers+1
        count.totalviews=count.totalviews+1
        count.put()
        pictures=db.GqlQuery("SELECT * FROM Picture " +"WHERE ANCESTOR IS :1 "+"ORDER BY uploaddate DESC",db.Key.from_path('User',users.get_current_user().nickname(),'Stream',stream_name))
        
        infos=[]
        url=""
        status=(0,0)
        if(users.get_current_user() and stream.author==users.get_current_user()):
            status=(1,1)
            for picture in pictures:
                infos.append((picture.key(),picture.imgkey,index))
                index=index+1
                if(index==4):
                    index=0 
            url=urllib.urlencode({'streamname': stream.name})
        
        else:
            for picture in pictures:
                infos.append((picture.key(),0,index))
                index=index+1
                if(index==4):
                    index=0           	
            if(users.get_current_user()):
                status=(1,0)
            else:
                self.redirect(users.create_login_url(self.request.url))
        template_values={"stream_name": stream_name,"infos":infos,"url":url,"status":status}
        template=JINJA_ENVIRONMENT.get_template("showmore.html")
        self.response.write(template.render(template_values))
            
class GeoView(webapp2.RequestHandler):
    def get(self): 
        stream_name=re.findall('%3D(.*)%3D%3D',self.request.url)[0]
        user_name=re.findall('%3D%3D(.*)',self.request.url)[0]
        stream=Stream.query(Stream.name==stream_name, Stream.author_name==user_name).fetch()[0]
        pictures=db.GqlQuery("SELECT * FROM Picture " +"WHERE ANCESTOR IS :1 "+"ORDER BY uploaddate DESC",db.Key.from_path('User',users.get_current_user().nickname(),'Stream',stream_name))
        length=0
        info=[]
        for picture in pictures:
            length=length+1
            info.append(str(picture.key()))

        url=urllib.urlencode({'streamname': stream.name})
        template_values={"info": info,"length":length, "stream_name": stream_name, "url": url}
        template=JINJA_ENVIRONMENT.get_template("geoview.html")
        self.response.write(template.render(template_values))    

class GeoAPI(webapp2.RequestHandler):
    def post(self):  
        stream_name=self.request.get("stream_name")
        min=self.request.get("min")
        min=int(min)
        max=self.request.get("max")
        max=int(max)
        pictures=db.GqlQuery("SELECT * FROM Picture " +"WHERE ANCESTOR IS :1",db.Key.from_path('User',users.get_current_user().nickname(),'Stream',stream_name)) 
        keys=[]
        for picture in pictures:
            keys.append(str(picture.key()))
        
        keys=json.dumps(keys[min:max])
        self.response.headers['Content-Type']="application/json"
        self.response.write(keys)     
        
application = webapp2.WSGIApplication([
    ('/upload', Upload),
    ('/showmore.*', ShowPictures),
    ('/delpic', DeletePictures),
    ('/subscribe', SubscribeStream),
    ('/img.*', Image),
    ('/stream.*', ViewSinglePage), 
    ('/geoview.*', GeoView), 
    ('/geoapi',GeoAPI),
    ('/picapi',PicAPI), 
], debug=True)