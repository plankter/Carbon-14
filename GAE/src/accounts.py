import os
# import logging
# import operator

from google.appengine.ext import webapp
from google.appengine.ext.webapp import util
from google.appengine.ext.webapp import template

from google.appengine.api import users
# from google.appengine.api import memcache

# from django.utils import simplejson

import aggrogator

#Accounts Module
class MainPage(webapp.RequestHandler):
    def get(self):
        # get the current user
        user = users.get_current_user()

        # is user an admin?
        admin = users.is_current_user_admin();

        # create user account if haven't already
        account = aggrogator.DB.getAccount(user)
        if account is None:
            account = aggrogator.Account(user=user)
            account.put()

        # create logout url
        logout_url = users.create_logout_url(self.request.uri)

        all_accounts = aggrogator.Account.all()

        template_values = {
            'account': account,
            'admin': admin,
            'logout_url': logout_url,
            'all_accounts': all_accounts,
            }
        path = os.path.join(os.path.dirname(__file__), 'templates/accounts.html')
        self.response.out.write(template.render(path, template_values))

class Subscribe(webapp.RequestHandler):
    def post(self):
        # get the current user
        user = users.get_current_user()
        email = self.request.get('email')

        aggrogator.DB.create_subscription(user, email)

def main():
    app = webapp.WSGIApplication([
        ('/accounts/', MainPage),
        ('/accounts/subscribe', Subscribe),
        ], debug=True)
    util.run_wsgi_app(app)

if __name__ == '__main__':
    main()
