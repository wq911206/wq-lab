import cgi
import urllib

from google.appengine.api import users
from google.appengine.ext import ndb
import webapp2

LOGIN_PAGE_TEMPLATE ="""\
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Connex.us</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="css/bootstrap.css" rel="stylesheet">
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
    <link href="css/bootstrap-responsive.css" rel="stylesheet">
    <link rel="stylesheet" href="css/jquery-ui.css">
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
<div class="jumbotron">
<h1>Welcome to Connexus!</h1>
<h2>Share the world!</h2>
</body>
</html>
"""


class LoginPage(webapp2.RequestHandler):
    def get(self):
        if users.get_current_user():
            self.redirect('management')
        else:
            url = users.create_login_url(self.request.url)
            url_linktext = 'Login'
            self.response.write(LOGIN_PAGE_TEMPLATE)

            self.response.write('<a class="btn btn-large btn-success" href="'+ url +'">'+ url_linktext + '</a></div>')


application = webapp2.WSGIApplication([
    ('/', LoginPage),
], debug=True)