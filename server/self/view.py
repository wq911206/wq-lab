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

import jinja2
import os

JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)


class viewStreams(webapp2.RequestHandler):
  def get(self):
    streams=Stream.query(Stream.author==users.get_current_user()).order(-Stream.creattime).fetch()
    index=0
    infos=[]
    for stream in streams:
        infos.append((stream.url,stream.coverurl,stream.name,index))
        index=index+1
        if(index==4):
            index=0
    
    template_values={"infos":infos}
    template=JINJA_ENVIRONMENT.get_template("view.html")
    self.response.write(template.render(template_values))

application = webapp2.WSGIApplication([
    ('/viewallstream', viewStreams),   
], debug=True)