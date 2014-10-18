import cgi
import urllib
import re
import os
import sys
import os.path
import json

from google.appengine.api import users
from google.appengine.ext import ndb
from google.appengine.ext import db

import webapp2

import jinja2
import os

from google.appengine.api import images
from google.appengine.api import urlfetch

from stream import Stream
from stream import Picture

SEARCH_PAGE_TEMPLATE ="""\
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Connex.us</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="http://bootstrap.ninghao.net/assets/css/bootstrap.css" rel="stylesheet">
    <style type="text/css">
      body {
        padding-top: 20px;
        padding-bottom: 60px;
      }

      /* Custom container */
      .container {
        margin: 0 auto;
        max-width: 1000px;
      }
      .container > hr {
        margin: 60px 0;
      }

      /* Main marketing message and sign up button */
      .jumbotron {
        margin: 80px 0;
        text-align: center;
      }
      .jumbotron h1 {
        font-size: 100px;
        line-height: 1;
      }
      .jumbotron .lead {
        font-size: 24px;
        line-height: 1.25;
      }
      .jumbotron .btn {
        font-size: 21px;
        padding: 14px 24px;
      }

      /* Supporting marketing content */
      .marketing {
        margin: 60px 0;
      }
      .marketing p + h4 {
        margin-top: 28px;
      }


      /* Customize the navbar links to be fill the entire space of the .navbar */
      .navbar .navbar-inner {
        padding: 0;
      }
      .navbar .nav {
        margin: 0;
        display: table;
        width: 100%;
      }
      .navbar .nav li {
        display: table-cell;
        width: 1%;
        float: none;
      }
      .navbar .nav li a {
        font-weight: bold;
        text-align: center;
        border-left: 1px solid rgba(255,255,255,.75);
        border-right: 1px solid rgba(0,0,0,.1);
      }
      .navbar .nav li:first-child a {
        border-left: 0;
        border-radius: 3px 0 0 3px;
      }
      .navbar .nav li:last-child a {
        border-right: 0;
        border-radius: 0 3px 3px 0;
      }
    </style>
    <link href="http://bootstrap.ninghao.net/assets/css/bootstrap-responsive.css" rel="stylesheet">
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.css">
      <script src="js/jquery-1.10.2.js"></script>
      <script src="js/jquery-ui.js"></script> 
      <script>
        $(document).ready(function (){
          $.ajax({
            type: 'POST',
            dataType: "json",
            url: '/api',
            success: function(newOrder){
            var availableTagsh = newOrder.nameList;
            $( "#GuangyuLin" ).autocomplete({
              source: availableTagsh
            });
            }
          })
        }); 
        </script>

  </head>
<body>
  <div class="container">

      <div class="masthead">
        <h3 class="muted">Connex.us</h3>
        <div class="navbar">
          <div class="navbar-inner">
            <div class="container">
              <ul class="nav">
                <li><a href="management">Manage</a></li>
                <li><a href="createstream">Create</a></li>
                <li><a href="viewallstream">View</a></li>
                <li class="active"><a href="search">Search</a></li>
                <li><a href="trending">Trending</a></li>
                <li><a href="social">Social</a></li>
              </ul>
            </div>
          </div>
        </div><!-- /.navbar -->
      </div>

  	<form action="/showsearch" method="get">
        <input type="search" name="searchStream" placeholder="Lucknow" id="GuangyuLin"></br>
        <input type="submit" value="Search">
    </form>
</body>
</html>
"""

JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)
 

class searchView(webapp2.RequestHandler):
    def get(self):
        self.response.write(SEARCH_PAGE_TEMPLATE)
        

class showSearch(webapp2.RequestHandler):
    def get(self):
        url = self.request.url
        stream_name = re.findall('searchStream=(\S+)',url)
        if len(stream_name) > 0:
            #self.response.write(url)
        #else:
            stream_name = re.findall('searchStream=(\S+)',url)[0]
            streams = Stream.query().fetch()
            nameList = list()
            for stream in streams:
                nameList.append(stream.name)
            
            index = list()
            for i in xrange(len(nameList)):
                index.append(LCS(nameList[i], stream_name))
            tmp = zip(index, nameList)
            tmp.sort(reverse = True)
            #we only show five most relation streams
            if len(tmp) < 5:
                showNum = len(tmp)
            else:
                showNum = 5
            #self.response.write(SEARCH_PAGE_TEMPLATE)
            #self.response.write('<p>%d results for <b>%s</b>,<br>  click on image to view stream</p>' % (showNum,stream_name))
            infos=[]
            for i in xrange(showNum):
                stream = Stream.query(Stream.name==tmp[i][1]).fetch()[0]
                ##self.response.write(stream.numberofpictures)
                pictures=db.GqlQuery("SELECT * FROM Picture " +"WHERE ANCESTOR IS :1",db.Key.from_path('User',users.get_current_user().nickname(),'Stream',stream.name))
                numberofpictures=0
                for picture in pictures:
                  numberofpictures=numberofpictures+1

                if numberofpictures > 0:
                    pictures=db.GqlQuery("SELECT * FROM Picture " +"WHERE ANCESTOR IS :1 "+"ORDER BY uploaddate DESC",db.Key.from_path('User',users.get_current_user().nickname(),'Stream',stream.name))                   
                    tmp1=(stream.url, "img?img_id="+str(pictures[0].key()),stream.name)
                    #self.response.out.write('<td><div style = "position:relative;"><a href = "%s"><img src="img?img_id=%s" ></img><div style = "position: absolute; left:150px; top:20px"></a>%s</div></div></td>' % (stream.url, pictures[0].key(),stream.name))
                else:
                    #self.response.out.write('<td><div style = "position:relative;"><a href = "%s"><img src="http://www.estatesale.com/img/no_image.gif" ></img><div style = "position: absolute; left:150px; top:20px"></a>%s</div></div></td>' % (stream.url, stream.name))
                    tmp1=(stream.url, "http://www.estatesale.com/img/no_image.gif",stream.name)
                infos.append(tmp1)
            
            template_values={
                "showNum": showNum,
                "infos": infos,
                "stream_name": stream_name,
            }
            
            template=JINJA_ENVIRONMENT.get_template("search.html")
            self.response.write(template.render(template_values))

class API(webapp2.RequestHandler):
  def post(self):
    streams = Stream.query(Stream.author==users.get_current_user()).fetch()
    nameList = list()
    for stream in streams:
      nameList.append(stream.name)
    ss={"nameList":nameList}
    
    self.response.headers['Content-Type'] = 'application/json'
    ss=json.dumps(ss)
    self.response.write(ss)

def LCS(stringa, stringb):
    x = list()
    y = list()
    for  i in xrange(len(stringa)):
        x.append(stringa[i])
    for j in xrange(len(stringb)):
        y.append(stringb[j])
    if (len(x) == 0 or len(y) == 0):
        return 0
    else:
        a = x[0]
        b = y[0]
        if (a == b):
            return LCS(x[1:], y[1:])+1
        else:
            return cxMax( LCS(x[1:], y), LCS(x, y[1:] )  )

def cxMax(a, b):
    if (a>=b):
        return a
    else:
        return b

application = webapp2.WSGIApplication([
    ('/search', searchView),
    ('/showsearch', showSearch),
    ('/api', API),
], debug=True)