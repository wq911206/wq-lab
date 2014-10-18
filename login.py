import cgi
import urllib

from google.appengine.api import users
from google.appengine.ext import ndb
import webapp2

LOGIN_PAGE_TEMPLATE ="""\
<html>
<body>
<h1>Welcome to Connexus!</h1>
<h2>Share the world!</h2>
<a href="%s">%s</a>
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
            self.response.write(LOGIN_PAGE_TEMPLATE % (url, url_linktext))


application = webapp2.WSGIApplication([
    ('/', LoginPage),
], debug=True)