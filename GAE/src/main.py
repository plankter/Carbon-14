import os
import logging
import operator

from google.appengine.ext import webapp
from google.appengine.ext.webapp import util
from google.appengine.ext.webapp import template
from google.appengine.api import users
from google.appengine.api import memcache

from django.utils import simplejson

import model

class MainPage(webapp.RequestHandler):
    def get(self):
        # get the current user
        user = users.get_current_user()

        # is user an admin?
        admin = users.is_current_user_admin();

        # create user account if haven't already
        account = model.Account.getAccount(user)
        if account is None:
            account = model.Account(user=user)
            account.put()

        # create logout url
        logout_url = users.create_logout_url(self.request.uri)

        template_values = {
                           'user': user,
                           'admin': admin,
                           'logout_url': logout_url,
                           }
        
        path = os.path.join(os.path.dirname(__file__), 'main.html')
        self.response.out.write(template.render(path, template_values))
        
        
class FillTestData(webapp.RequestHandler):
    def get(self):
        category = model.ProductCategory(name="Beverages")
        category.put()
        
        producer = model.Producer(name="The Coca-Cola Company")
        producer.put()
        
        fanta = model.Product(code="0000040822938", name="Fanta Orange", producer=producer, category=category)
        fanta.description = "Orange Soft Drink with Sugar and Sweeteners"
        fanta.put()
        
        sprite = model.Product(code="0000497000064", name="Sprite", producer=producer, category=category)
        sprite.put()
        
        
        
def main():
    application = webapp.WSGIApplication(
        [
            ('/', MainPage),
            ('/fillTestData', FillTestData),
        ],
        debug=True)
    util.run_wsgi_app(application)
    

if __name__ == '__main__':
    main()
