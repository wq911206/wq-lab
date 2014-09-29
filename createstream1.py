import cgi
import urllib

from google.appengine.api import users
from google.appengine.ext import ndb
from google.appengine.api import mail
import webapp2

from stream import Stream
from stream import CountViews


CREATESTREAM_PAGE_TEMPLATE ="""\
<!DOCTYPE html>
<html>
<body>
  <h1>Connex.us</h1>
  <table style="width:70%">
    <tr>
      <td><a href="management">Manage</a></td>
      <td><b>Create</b></td>  
      <td><a href="viewallstream">View</a></td>
        <td><a href="search">Search</a></td>
        <td><a href="trenging">Trending</a></td>
        <td><a href="social">Social</a></td>
    </tr>
  </table>
    
  <form action="/sign" method="post">
  <table>
    <tr>
      <td><textarea name="streamname" rows="3" cols="60"></textarea></td>
      <td><textarea name="streamtags" rows="3" cols="60"></textarea></td>
    </tr>
    <tr>
      <td><h2>Name Your Stream</h2></td>
      <td><h2>Tag Your Stream</h2></td>
    </tr>
    <tr>
      <td><textarea name="subscribers" rows="3" cols="60" placeholder = "Input subscribers' emails"></textarea></td>
      <td><textarea name="url" rows="3" cols="60"></textarea></td>
    </tr>
    <tr>
      <td><textarea name="context" rows="3" cols="60" placeholder="Option message for invite" ></textarea></td>
      <td><h2>URL to to Cover Image</h2></td>
    </tr>
    <tr>
      <td><h2>Add Subscribers</h2></td>
    </tr>
  </table>
  <input type="submit" value="Create Stream"></div>
  </form>
	
</body>
</html>
"""

class  CreateStreamPage(webapp2.RequestHandler): 
    def get(self):         
        self.response.write(CREATESTREAM_PAGE_TEMPLATE)

class  CreateStream(webapp2.RequestHandler):        
    def post(self):
        stream_name=self.request.get("streamname")
        if len(stream_name)==0:
            stream_name="untitledstream"
        stream_tags=self.request.get("streamtags").split(',')
        stream_subscribers=self.request.get("subscribers").split(';')
        stream_url=self.request.get("url")
        emailContext = self.request.get("context")
        
        
        streams=Stream.query(Stream.name==stream_name, Stream.author==users.get_current_user()).fetch()
        if (len(streams)<1):
            stream=Stream()
            count=CountViews(parent=ndb.Key('User',users.get_current_user().nickname()))
            stream.name=stream_name
            count.name=stream_name
            count.numbers=0
            count.put()
            stream.numberofpictures=0
            #stream.views=0
            stream.total=0
    
            if len(stream_tags)>0:
                stream.tag=stream_tags
            if len(stream_subscribers[0])>0:
                stream.subscribers=stream_subscribers
                default_context = "Notice: " + users.get_current_user().nickname() + " add a new stream named '" + stream_name +"' and the link to the stream is"+stream.guesturl+"\n\n"
                emailSubject = "Stream Update Info with UserID: " + users.get_current_user().nickname()
                emailSender = users.get_current_user().email()
                for emailReceiver in stream.subscribers:
                    mail.send_mail(sender = emailSender, to = emailReceiver, subject = emailSubject, body = default_context + emailContext)
    
            if len(stream_url)>0:
                stream.coverurl=stream_url
            else:
                stream.coverurl="http://i01.i.aliimg.com/wsphoto/v0/848486955_2/20cm-lovely-Meng-Qiqi.jpg"
            
            stream.author=users.get_current_user()
            stream.author_name=users.get_current_user().nickname()
            stream.url=urllib.urlencode({'streamname': stream.name})
            stream.guesturl=urllib.urlencode({'showmore': stream.name+"=="+users.get_current_user().nickname()})
            stream.put()
            self.redirect('/management',permanent=False)
        else:
            self.redirect('/error', permanent = False)
    
    
       
application = webapp2.WSGIApplication([
    ('/sign', CreateStream),
    ('/createstream', CreateStreamPage),
    
], debug=True)