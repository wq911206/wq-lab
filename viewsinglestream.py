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


VIEW_SINGLE_STREAM_TEMPLATE ="""\
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
    		<td><a href="trending">Trending</a></td>
    		<td><a href="social">Social</a></td>
  		</tr>
  	</table>
  	
</body>
</html>
"""


class  ViewSinglePage(webapp2.RequestHandler): 
    def get(self):                                           
        self.response.write(VIEW_SINGLE_STREAM_TEMPLATE)
                
        stream_name=re.findall('%3D(.*)',self.request.url)[0]

        self.response.write('<h2 >%s</h2>' %stream_name)        
        stream=Stream.query(Stream.name==stream_name, Stream.author==users.get_current_user()).fetch()[0]

        pictures=db.GqlQuery("SELECT *FROM Picture " + "WHERE ANCESTOR IS :1 " +"ORDER BY uploaddate DESC LIMIT 3" , db.Key.from_path('Stream',stream_name))

        self.response.write('<table border="1" style="width:100%"><tr>')
        for picture in pictures: 
            self.response.out.write('<td><img src="img?img_id=%s"></img></td>' %picture.key())

        self.response.write('</tr></table>')	
        
        #urluser=urllib.urlencode({'user': users.get_current_user()})
        #urlstream=urllib.urlencode({'showmore': stream.name})
        url=urllib.urlencode({'showmore': stream.name+"=="+users.get_current_user().nickname()})
        self.response.write('<form action="%s" ,method="post"><input type="submit" value="More Pictures"></form>' %url)
                    
        upload_url = blobstore.create_upload_url('/upload')
        self.response.out.write('<form action="/upload" method="post" enctype="multipart/form-data">')
        self.response.out.write("""Upload File: <input type="file"  name="file" ><br> <input type="submit" name="submit" value="Submit"> </form>""")
        
             
class Image(webapp2.RequestHandler):
    def get(self):     
        picture = db.get(self.request.get('img_id'))
        self.response.out.write(picture.image)


class Upload(webapp2.RequestHandler):
    def post(self): 
        original_url=self.request.headers['Referer'] 
        img=self.request.get('file')
        if len(img) != 0:     
            stream_name=re.findall('=(.*)',original_url)[0]  
            stream=Stream.query(Stream.name==stream_name, Stream.author==users.get_current_user()).fetch()[0]           
            picture=Picture(parent=db.Key.from_path('Stream',stream_name))
            stream.lastnewdate=  picture.uploaddate
            stream.numberofpictures=stream.numberofpictures+1
            stream.total=stream.total+1         
            picture.id=str(stream.total)
            img=images.resize(img,300,300)        
            picture.image=db.Blob(img)
            picture.put()
            #if stream.coverurl==None and stream.numberofpictures==1:
                #stream.coverurl="img?img_id=%s" %picture.key()
            stream.put()
        self.redirect(original_url)

class DeletePictures(webapp2.RequestHandler):
    def post(self): 
        original_url=self.request.headers['Referer']
        stream_name=re.findall('=(.*)%3D%3D',original_url)[0]
        stream=Stream.query(Stream.name==stream_name, Stream.author==users.get_current_user()).fetch()[0]
        dellsts=self.request.get_all("status")
        pictures=db.GqlQuery("SELECT * FROM Picture " +"WHERE ANCESTOR IS :1 AND id IN :2",db.Key.from_path('Stream',stream_name),dellsts)
        db.delete(pictures)
        stream.numberofpictures=stream.numberofpictures-len(dellsts)
        stream.put()
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
        #self.response.write(users.get_current_user().nickname())
        #self.response.write(user_name)
        self.redirect(original_url)
        

class ShowPictures(webapp2.RequestHandler):
    def get(self): 
        #self.response.write(users.get_current_user())
        stream_name=re.findall('%3D(.*)%3D%3D',self.request.url)[0]
        user_name=re.findall('%3D%3D(.*)',self.request.url)[0]
        #self.response.write(stream_name)
        #self.response.write(user_name)
        #self.response.write(self.request.url)
        
        #self.response.write(user_name)
        self.response.write('<h2 >%s</h2>' %stream_name)
        index=0
        stream=Stream.query(Stream.name==stream_name, Stream.author_name==user_name).fetch()[0]
        count=CountViews.query(CountViews.name==stream.name,ancestor=ndb.Key('User',stream.author_name)).fetch()[0]
        #count==CountViews.query().fetch()[0]
        count.numbers=count.numbers+1
        count.put()
        #self.response.write(count.numbers)
        #stream=Stream.query(Stream.name==stream_name).fetch()[0]
        #self.response.write(stream.author)
        #self.response.write(users.User(user_name))
        #self.response.write(stream.author_name==user_name)
        pictures=db.GqlQuery("SELECT * FROM Picture " +"WHERE ANCESTOR IS :1 "+"ORDER BY uploaddate DESC",db.Key.from_path('Stream',stream_name))
        
        if(users.get_current_user() and stream.author==users.get_current_user()):
            self.response.write('<form action="delpic" method="post"><table border="1" style="width:100%">')
            for picture in pictures:
                if(index==0):
                    self.response.write("<tr>")
                self.response.out.write('<td><img src="img?img_id=%s"></img><input type="checkbox" name="status", value="%s"></td>' %(picture.key(),picture.id))
                if(index==3):
                    self.response.write("</tr>")
                index=index+1
            self.response.write('</table>')	
            self.response.write('<input type="submit" value="Delete Selected"></form>')
            url=urllib.urlencode({'streamname': stream.name}) 
            self.response.write('<a href="%s">Go Back</a>'% url) 
        
        else:
            self.response.write('<table border="1" style="width:100%">')
            for picture in pictures:
                if(index==0):
                    self.response.write("<tr>")
                self.response.out.write('<td><img src="img?img_id=%s"></img></td>' %picture.key())
                if(index==3):
                    self.response.write("</tr>")
                index=index+1
            self.response.write('</table>')            	
            if(users.get_current_user()):
                self.response.write('<form action="subscribe" method="post"><input type="submit" value="Subscribe"></form>') 
            else:
                self.redirect(users.create_login_url(self.request.url))
                #self.response.write(self.request.url)
                #self.response.write('<form action="%s" method="get"><input type="submit" value="Subscribe"></form>' %users.create_login_url(self.request.url))      
        
        
        
application = webapp2.WSGIApplication([
    ('/upload', Upload),
    ('/showmore.*', ShowPictures),
    ('/delpic', DeletePictures),
    ('/subscribe', SubscribeStream),
    ('/img.*', Image),
    ('/stream.*', ViewSinglePage),   
], debug=True)