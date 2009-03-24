import os
import logging
import operator

from google.appengine.ext import webapp
from google.appengine.ext.webapp import util
from google.appengine.ext.webapp import template

from google.appengine.api import users
from google.appengine.api import memcache

from django.utils import simplejson

import aggrogator
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
        
        path = os.path.join(os.path.dirname(__file__), 'templates/index.html')
        self.response.out.write(template.render(path, template_values))
        
        

class AddService(webapp.RequestHandler):
    def post(self):
        user = users.get_current_user()
        service = self.request.get('service')
        username = self.request.get('username')

        if service and username:
            logging.info("AddService: service = " + service + " and username = " + username + " for account = " + user.email())
            feed = aggrogator.DB.getFeed(service, username)
            if feed is None:
                feed = aggrogator.Feed(service=service, username=username)
                aggrogator.GenericFeed.update(feed)
            if feed.is_saved():
                account = aggrogator.DB.getAccount(user)
                accountFeed = aggrogator.AccountFeed.gql("WHERE account = :1 AND feed = :2", account, feed).get()
                if accountFeed is None:
                    aggrogator.AccountFeed(account=account, feed=feed).put()
                    # update cache
                    aggrogator.Cache.setUserServices(account)

class GetUserServices(webapp.RequestHandler):
    def get(self):
        user = users.get_current_user()

        # get the user's services from the cache
        #userServices = aggrogator.Cache.getUserServices(user)
        userServices = aggrogator.Aggrogator.get_services(user.email())

        stats = memcache.get_stats()
        self.response.headers['content-type']  = 'application/json'
        self.response.out.write(simplejson.dumps({'stats': stats, 'userServices': userServices}))

class GetEntries(webapp.RequestHandler):
    def get(self):
        service = self.request.get('service')
        username = self.request.get('username')

        # get the feed's entries from the cache
        entries = aggrogator.Cache.getEntries(service, username)

        # sort entries with most recent up front
        entries.sort(key=operator.attrgetter('timestamp'), reverse=True)

        stats = memcache.get_stats()
        self.response.headers['content-type']  = 'application/json'
        self.response.out.write(simplejson.dumps({'stats': stats, 'entries': [entry.to_dict() for entry in entries]}))
        
class AggroWebService(webapp.RequestHandler):
    def get(self):
        self.response.headers['content-type'] = 'text/xml'
        username = self.request.get('username')
        entries = aggrogator.Aggrogator.get_feed(username)
        str = u"""<?xml version="1.0" encoding="utf-8"?><entries>"""
        for entry in entries:
            str += entry.to_xml()
        str += "</entries>"
        self.response.out.write(str)
        
        
class FillTestData(webapp.RequestHandler):
    def get(self):
        category = model.ProductCategory(name="Beverages")
        category.put()
        
        producer = model.Producer(name="The Coca-Cola Company")
        producer.put()
        
        fanta = model.Product(name="Fanta", producer=producer, category=category)
        fanta.put()
        
        sprite = model.Product(name="Sprite", producer=producer, category=category)
        sprite.put()
        
        
        
def main():
    application = webapp.WSGIApplication(
        [
            ('/', MainPage),
            ('/addService', AddService),
            ('/getEntries', GetEntries),
            ('/api', AggroWebService),
            ('/getUserServices', GetUserServices),
            ('/fillTestData', FillTestData),
        ],
        debug=True)
    util.run_wsgi_app(application)
    

if __name__ == '__main__':
    main()
