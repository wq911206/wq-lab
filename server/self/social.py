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



class  SocialPage(webapp2.RequestHandler): 
    def get(self):         
        template=JINJA_ENVIRONMENT.get_template("social.html")
        self.response.write(template.render())
    
       
application = webapp2.WSGIApplication([
    ('/social', SocialPage),
    
], debug=True)