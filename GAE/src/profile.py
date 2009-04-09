'''
Created on Mar 28, 2009

@author: Anton Rau
'''

import os
import logging
import operator

from google.appengine.ext import webapp
from google.appengine.ext.webapp import util
from google.appengine.ext.webapp import template
from google.appengine.api import users
from google.appengine.api import memcache

from django.utils import simplejson

import models

class ProfilePage(webapp.RequestHandler):
    @util.login_required
    def get(self):
        # get the current user
        user = users.get_current_user()

        # create user account if haven't already
        account = models.Account.getAccount(user)
        if account is None:
            account = models.Account(user=user)
            account.put()

        # create logout url
        logout_url = users.create_logout_url(self.request.uri)

        template_values = {
                           'user': user,
                           'logout_url': logout_url,
                           }
        
        path = os.path.join(os.path.dirname(__file__), 'profile.html')
        self.response.out.write(template.render(path, template_values))

def main():
    application = webapp.WSGIApplication(
        [
            ('/profile', ProfilePage),
        ],
        debug=True)
    util.run_wsgi_app(application)
    

if __name__ == '__main__':
    main()