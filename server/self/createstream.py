import cgi
import urllib

from google.appengine.api import users
from google.appengine.ext import ndb
from google.appengine.api import mail
import webapp2

from stream import Stream
from stream import CountViews

import jinja2
import os


JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)



class  CreateStreamPage(webapp2.RequestHandler): 
    def get(self):         
        template=JINJA_ENVIRONMENT.get_template("createstream.html")
        self.response.write(template.render())

class  CreateStream(webapp2.RequestHandler):        
    def post(self):
        stream_name=self.request.get("streamname")
        if len(stream_name)==0:
            stream_name="untitledstream"
        stream_tags=self.request.get("streamtags").split(',')
        stream_subscribers=self.request.get("subscribers").split(';')
        stream_url=self.request.get("url")
        emailContext = self.request.get("context")
        emailSubject = "Stream Update Info with UserID: " + users.get_current_user().nickname()
        emailSender = users.get_current_user().email()
        
        
        streams=Stream.query(Stream.name==stream_name, Stream.author==users.get_current_user()).fetch()
        if (len(streams)<1):
            stream=Stream()
            count=CountViews(parent=ndb.Key('User',users.get_current_user().nickname()))
            stream.name=stream_name
            count.name=stream_name
            count.numbers=0
            count.totalviews=0
            count.put()
            stream.author=users.get_current_user()
            stream.author_name=users.get_current_user().nickname()
            stream.url=urllib.urlencode({'streamname': stream.name})
            stream.guesturl=urllib.urlencode({'showmore': stream.name+"=="+users.get_current_user().nickname()})
            default_context = "Notice: " + users.get_current_user().nickname() + " add a new stream named '" + stream_name +"' and the link to the stream is: "+"http://wq911206.appspot.com/"+stream.guesturl+"\n\n"
    
            if len(stream_tags)>0:
                stream.tag=stream_tags
            if len(stream_subscribers[0])>0:
                stream.subscribers=stream_subscribers
                for emailReceiver in stream.subscribers:
                    mail.send_mail(sender = emailSender, to = emailReceiver, subject = emailSubject, body = default_context + emailContext)
    
            if len(stream_url)>0:
                stream.coverurl=stream_url
            else:
                stream.coverurl="http://i01.i.aliimg.com/wsphoto/v0/848486955_2/20cm-lovely-Meng-Qiqi.jpg"
            
            
            stream.put()
            self.redirect('/management',permanent=False)
        else:
            self.redirect('/error', permanent = False)
    
    
       
application = webapp2.WSGIApplication([
    ('/sign', CreateStream),
    ('/createstream', CreateStreamPage),
    
], debug=True)