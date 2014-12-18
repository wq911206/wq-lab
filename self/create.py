import cgi
from google.appengine.api import users
from google.appengine.api import urlfetch

from google.appengine.ext import ndb
import webapp2
import json
import urllib
from datetime import datetime
from datetime import date
from datetime import timedelta

from card import Card,Event,Notification,User



class CreateContactAPI(webapp2.RequestHandler):
    def post(self):
        userdata=json.loads(self.request.body)
        #print userdata        
        result=dict()
        result["createtime"]=CreateContact(userdata)
        result=json.dumps(result)
        self.response.headers['Content-Type']="application/json"
        self.response.write(result)
        
def CreateContact(data):
    card=Card()
    card.firstname=data["firstname"]
    card.lastname=data["lastname"]
    card.author=data["author"]
    card.company=data["company"]
    #phones=data["phones"].split("**")
    #for phone in phones:
        #card.phone.append(phone)
    card.phone=data["phones"].split("**")
    card.email=data["emails"].split("**")
    card.address=data["addresses"].split("**")
    card.url=data["urls"].split("**")
    card.put()
    return str(card.createtime)

class AddContactAPI(webapp2.RequestHandler):
    def post(self):
        userdata=json.loads(self.request.body)
        AddContact(userdata)
        
def AddContact(data):
    if(data["author"]==data["subscriber"]):
        return
    format="%Y-%m-%d %H:%M:%S.%f"
    createtime=datetime.strptime(data["createtime"],format)
    cards=Card.query(Card.author==data["author"],Card.createtime==createtime).fetch()
    if(len(cards)>0):
        card=cards[0]
        subscriber=data["subscriber"]
        if(not (subscriber in card.subscribers)):
            card.subscribers.append(data["subscriber"])
            card.put()

class ModifyCardAPI(webapp2.RequestHandler):
    def post(self):
        userdata=json.loads(self.request.body)
        ModifyCard(userdata)

def ModifyCard(data):
    format="%Y-%m-%d %H:%M:%S.%f"
    createtime=datetime.strptime(data["createtime"],format)
    cards=Card.query(Card.author==data["author"],Card.createtime==createtime).fetch()
    if (len(cards)>0):
        card=cards[0]
        card.firstname=data["firstname"]
        card.lastname=data["lastname"]
        card.author=data["author"]
        card.company=data["company"]
        card.phone=data["phones"].split("**")
        card.email=data["emails"].split("**")
        card.address=data["addresses"].split("**")
        card.url=data["urls"].split("**")
        card.put()

class GetOwnCardsAPI(webapp2.RequestHandler):
    def post(self):
        data=json.loads(self.request.body)
        username=data["author"]
        cards=Card.query(Card.author==username).fetch()
        result=GetCards(cards)
        self.response.headers['Content-Type']="application/json"
        self.response.write(result)

class GetContactsAPI(webapp2.RequestHandler):
    def post(self):
        data=json.loads(self.request.body)
        username=data["author"]
        temp=Card.query().fetch()
        cards=[]
        for card in temp:
            if(username in card.subscribers):
                cards.append(card)
        result=GetCards(cards)
        self.response.headers['Content-Type']="application/json"
        self.response.write(result)   

def GetCards(cards): 
    data=[]
    for card in cards:
        tmp=dict()
        tmp["firstname"]=card.firstname
        tmp["lastname"]=card.lastname
        tmp["company"]=card.company
        tmp["createtime"]=str(card.createtime)
        tmp["author"]=card.author
        
        tmp["phones"]="**".join(card.phone)
        tmp["addresses"]="**".join(card.address)
        tmp["emails"]="**".join(card.email)
        tmp["urls"]="**".join(card.url)
        data.append(tmp)
    
    result=dict()
    result["cards"]=data
    result=json.dumps(result)
    return result

class DeleteCardAPI(webapp2.RequestHandler):
    def post(self):
        data=json.loads(self.request.body)
        format="%Y-%m-%d %H:%M:%S.%f"
        createtime=datetime.strptime(data["createtime"],format)
        cards=Card.query(Card.author==data["author"],Card.createtime==createtime).fetch()
        if(len(cards)>0):
            cards[0].key.delete()

class DeleteContactsAPI(webapp2.RequestHandler):
    def post(self):  
        data=json.loads(self.request.body)
        author=data["author"]
        contacts=data["contact"].split("**")
        times=data["createtime"].split("**")
        
        format="%Y-%m-%d %H:%M:%S.%f"
        createtimes=[]
        for time in times:
            createtimes.append(datetime.strptime(time,format))
        
        cards=Card.query(Card.author.IN(contacts),Card.createtime.IN(createtimes)).fetch()

        for card in cards:
            if(author in card.subscribers):
                card.subscribers.remove(author)
                card.put()


        
class GetOwnEventsAPI(webapp2.RequestHandler):
    def post(self):
        data=json.loads(self.request.body)
        username=data["author"]
        events=Event.query(Event.author==username).fetch()
        result=GetEvents(events)
        self.response.headers['Content-Type']="application/json"
        self.response.write(result)

class GetOtherEventsAPI(webapp2.RequestHandler):
    def post(self):
        data=json.loads(self.request.body)
        username=data["author"]
        temp=Event.query().fetch()
        events=[]
        for event in temp:
            if(username in event.subscribers):
                events.append(event)
        result=GetEvents(events)
        self.response.headers['Content-Type']="application/json"
        self.response.write(result)   

def GetEvents(events): 
    data=[]
    for event in events:
        tmp=dict()
        tmp["title"]=event.title
        tmp["author"]=event.author
        tmp["date"]=str(event.date)
        tmp["location"]=event.location
        tmp["organizer"]=event.organizer
        tmp["description"]=event.description
        tmp["createtime"]=str(event.createtime)
        tmp["subscribers"]=len(event.subscribers)
        data.append(tmp)
    
    result=dict()
    result["events"]=data
    result=json.dumps(result)
    #print result
    return result
    
class CreateEventAPI(webapp2.RequestHandler):
    def post(self):
        userdata=json.loads(self.request.body)
        #print userdata
        result=CreateEvent(userdata)
        self.response.headers['Content-Type']="application/json"
        self.response.write(result)
        
def CreateEvent(data):
    event=Event()
    event.title=data["title"]
    event.author=data["author"]
    event.date=datetime.strptime(data["date"],"%Y-%m-%d %H:%M")
    event.location=data["location"]
    event.organizer=data["organizer"]
    event.description=data["description"]
    
    event.latitude=data["latitude"]
    event.longitude=data["longitude"]
    
    event.put()
    result=dict()
    result["createtime"]=str(event.createtime)
    result["subscribers"]=len(event.subscribers)
    result=json.dumps(result)
    return result

class ModifyEventAPI(webapp2.RequestHandler):
    def post(self):
        userdata=json.loads(self.request.body)
        ModifyEvent(userdata)

def ModifyEvent(data):
    format="%Y-%m-%d %H:%M:%S.%f"
    createtime=datetime.strptime(data["createtime"],format)
    events=Event.query(Event.author==data["author"],Event.createtime==createtime).fetch()
    if (len(events)>0):
        event=events[0]
        event.title=data["title"]
        event.date=datetime.strptime(data["date"],"%Y-%m-%d %H:%M")
        event.location=data["location"]
        event.organizer=data["organizer"]
        event.description=data["description"]
        event.put()

class DeleteEventAPI(webapp2.RequestHandler):
    def post(self):
        data=json.loads(self.request.body)
        format="%Y-%m-%d %H:%M:%S.%f"
        createtime=datetime.strptime(data["createtime"],format)
        events=Event.query(Event.author==data["author"],Event.createtime==createtime).fetch()
        if(len(events)>0):
            events[0].key.delete() 

class SubscribeEventAPI(webapp2.RequestHandler):
    def post(self):
        userdata=json.loads(self.request.body)
        SubscribeEvent(userdata)
        
def SubscribeEvent(data):
    if(data["author"]==data["subscriber"]):
        return
    format="%Y-%m-%d %H:%M:%S.%f"
    createtime=datetime.strptime(data["createtime"],format)
    events=Event.query(Event.author==data["author"],Event.createtime==createtime).fetch()
    if(len(events)>0):
        event=events[0]
        subscriber=data["subscriber"]
        if(not (subscriber in event.subscribers)):
            event.subscribers.append(data["subscriber"])
            event.put() 

class UnSubscribeEventAPI(webapp2.RequestHandler):
    def post(self):
        userdata=json.loads(self.request.body)
        UnSubscribeEvent(userdata)

def UnSubscribeEvent(data):
    if(data["author"]==data["subscriber"]):
        return
    format="%Y-%m-%d %H:%M:%S.%f"
    createtime=datetime.strptime(data["createtime"],format)
    events=Event.query(Event.author==data["author"],Event.createtime==createtime).fetch()
    if(len(events)>0):
        event=events[0]
        subscriber=data["subscriber"]
        if(subscriber in event.subscribers):
            event.subscribers.remove(subscriber);
            event.put() 

            
class ShareContactNotificationAPI(webapp2.RequestHandler):
    def post(self):
        print self.request.body
        destinations=self.request.get("destination").split("**")
        destinations=sorted(set(destinations))
        sender=self.request.get("sender")
        
        format="%Y-%m-%d %H:%M:%S.%f"
        createtime=datetime.strptime(self.request.get("createtime"),format)
        cards=Card.query(Card.author==self.request.get("author"),Card.createtime==createtime).fetch()
        
        if(len(cards)>0):
            tmp=dict()
            card=cards[0]
            tmp["firstname"]=card.firstname
            tmp["lastname"]=card.lastname
            tmp["company"]=card.company
            tmp["createtime"]=str(card.createtime)
            tmp["author"]=card.author
        
            tmp["phones"]="**".join(card.phone)
            tmp["addresses"]="**".join(card.address)
            tmp["emails"]="**".join(card.email)
            tmp["urls"]="**".join(card.url)
            tmp=json.dumps(tmp)
            for destination in destinations:
                CreateNotification(0,sender,destination,tmp)

class ShareEventNotificationAPI(webapp2.RequestHandler):
    def post(self):
        print self.request.body
        destinations=self.request.get("destination").split("**")
        destinations=sorted(set(destinations))
        sender=self.request.get("sender")
        format="%Y-%m-%d %H:%M:%S.%f"
        createtime=datetime.strptime(self.request.get("createtime"),format)
        events=Event.query(Event.author==self.request.get("author"),Event.createtime==createtime).fetch()
        
        if(len(events)>0):
            tmp=dict()
            event=events[0]
            tmp["title"]=event.title
            tmp["author"]=event.author
            tmp["date"]=str(event.date)
            tmp["location"]=event.location
            tmp["organizer"]=event.organizer
            tmp["description"]=event.description
            tmp["createtime"]=str(event.createtime)
            tmp["subscribers"]=len(event.subscribers)
            tmp=json.dumps(tmp)
            for destination in destinations:
                CreateNotification(1,sender,destination,tmp)

def CreateNotification(typ, send, receiver, msg):
    users=User.query(User.author==receiver).fetch()
    if(len(users)==0):
        user=User()
        user.author=receiver
    else:
        user=users[0]
    
    notification=Notification()
    notification.type=typ
    notification.sender=send
    notification.message=msg
    notification.flag=False
    user.notifications.append(notification)
    user.put()

class GetNotificationsAPI(webapp2.RequestHandler):
    def post(self):
        data=json.loads(self.request.body)
        username=data["author"]
        users=User.query(User.author==username).fetch()
        if(len(users)==0):
            user=User()
            user.author=username
        else:
            user=users[0]
        
        notifications=user.notifications
        result=dict()
        data=[]
        for notification in notifications:
            tmp=dict()
            tmp["type"]=notification.type
            tmp["message"]=notification.message
            tmp["sender"]=notification.sender
            tmp["receivetime"]=str(notification.receivetime)
            tmp["flag"]=notification.flag
            data.append(tmp)
        
        result["notification"]=data
        result=json.dumps(result)
        self.response.headers['Content-Type']="application/json"
        self.response.write(result)   
    
class HandleNotificationsAPI(webapp2.RequestHandler):
    def post(self):
        data=dict()
        data["author"]=self.request.get("author")
        data["subscriber"]=self.request.get("subscriber")
        data["createtime"]=self.request.get("createtime")
        if(int(self.request.get("decision"))==1):   
            if(int(self.request.get("type"))==0):
                AddContact(data)
            if(int(self.request.get("type"))==1):
                SubscribeEvent(data)
        
        DeleteNotification(self.request.get("subscriber"),int(self.request.get("position")))

class HandleSystemNotificationsAPI(webapp2.RequestHandler):
    def post(self):  
        DeleteNotification(self.request.get("author"),int(self.request.get("position")))      
        
def DeleteNotification(author,position):
    users=User.query(User.author==author).fetch()
    if(len(users)>0):
        user=users[0]
        print user.notifications.pop(position)
        user.put()

class NearbyEventsAPI(webapp2.RequestHandler):
    def post(self):
        events=Event.query().fetch()
        latitude=float(self.request.get("latitude"))
        longitude=float(self.request.get("longitude"))
        data=[]
        for event in events:
            if(abs(event.latitude-latitude)<0.001 and abs(event.longitude-longitude)<0.001):
                print event.title
                tmp=dict()
                tmp["title"]=event.title
                tmp["author"]=event.author
                tmp["date"]=str(event.date)
                tmp["location"]=event.location
                tmp["organizer"]=event.organizer
                tmp["description"]=event.description
                tmp["createtime"]=str(event.createtime)
                tmp["subscribers"]=len(event.subscribers)
                tmp["latitude"]=event.latitude
                tmp["longitude"]=event.longitude
                data.append(tmp)
    
        result=dict()
        result["events"]=data
        result=json.dumps(result)
        self.response.headers['Content-Type']="application/json"
        self.response.write(result)

class SystemNofiticationAPI(webapp2.RequestHandler):
    def get(self):
        print "wq"
        today=date.today()
        events=Event.query().fetch()
        for event in events:
            eventtime=event.date
            if(eventtime.date()==today+timedelta(days=1)):
                print event.title
                subscribers=event.subscribers
                for subscriber in subscribers:
                    CreateNotification(2,"system",subscriber,"You have an event tomorrow: \n"+event.title)


class TestAPI(webapp2.RequestHandler):
    def get(self):
        print "wq"
        users=User.query().fetch()
        for user in users:
            CreateNotification(2,"system",user.author,"wq")

application = webapp2.WSGIApplication([
    ('/createcontact', CreateContactAPI),
    ('/addcontact', AddContactAPI),
    ('/getowncards', GetOwnCardsAPI),
    ('/getcontacts', GetContactsAPI),
    ('/modifycard', ModifyCardAPI),
    ('/deletecard', DeleteCardAPI),
    ('/deletecontacts', DeleteContactsAPI),
    
    ('/getownevents', GetOwnEventsAPI),
    ('/createevent', CreateEventAPI),
    ('/modifyevent', ModifyEventAPI),
    ('/deleteevent', DeleteEventAPI),
    ('/getsubscribedevents', GetOtherEventsAPI),
    ('/subscribetoevent', SubscribeEventAPI),
    ('/unsubscribeevent', UnSubscribeEventAPI),
    ('/nearbyevents', NearbyEventsAPI),
    
    ('/sharecontactnotification', ShareContactNotificationAPI),
    ('/shareeventnotification', ShareEventNotificationAPI),
    ('/getnotification', GetNotificationsAPI),
    ('/handlecontactnotification', HandleNotificationsAPI),
    ('/handlesystemnotification', HandleSystemNotificationsAPI),
    ('/systemnotification', SystemNofiticationAPI),
    
    ('/test', TestAPI),
], debug=True)