import cgi
import urllib

from google.appengine.api import users
from google.appengine.ext import ndb
import webapp2


class Card(ndb.Model):    
    firstname=ndb.StringProperty()
    lastname=ndb.StringProperty()
    subscribers=ndb.StringProperty(repeated=True)
    address=ndb.StringProperty(repeated=True)
    company=ndb.StringProperty()
    email=ndb.StringProperty(repeated=True)
    phone=ndb.StringProperty(repeated=True)
    url=ndb.StringProperty(repeated=True)
    createtime=ndb.DateTimeProperty(auto_now_add=True)
    author=ndb.StringProperty()
    
class Event(ndb.Model):
    title=ndb.StringProperty()
    author=ndb.StringProperty()
    date=ndb.DateTimeProperty()
    location=ndb.StringProperty()
    organizer=ndb.StringProperty()
    description=ndb.TextProperty()
    createtime=ndb.DateTimeProperty(auto_now_add=True)
    subscribers=ndb.StringProperty(repeated=True)
    latitude=ndb.FloatProperty()
    longitude=ndb.FloatProperty()

class Notification(ndb.Model):
    type=ndb.IntegerProperty()
    message=ndb.TextProperty()
    sender=ndb.StringProperty()
    receivetime=ndb.DateTimeProperty(auto_now_add=True)
    flag=ndb.BooleanProperty()
    
class User(ndb.Model):
    author=ndb.StringProperty()
    notifications=ndb.StructuredProperty(Notification, repeated=True)