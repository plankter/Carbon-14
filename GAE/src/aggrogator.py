import datetime
import time
import logging
import pickle
import operator
from xml.sax.saxutils import escape

from google.appengine.ext import db

from google.appengine.api import users
from google.appengine.api import urlfetch
from google.appengine.api import memcache

import feedparser

SERVICE_TEMPLATES = {
    'twitter': "http://twitter.com/statuses/user_timeline/%s.rss",
    'del.icio.us': "http://del.icio.us/rss/%s",
    'last.fm': "http://ws.audioscrobbler.com/1.0/user/%s/recenttracks.rss",
    'YouTube': "http://www.youtube.com/rss/user/%s/videos.rss",
    }

# expiration time (in seconds) for feeds in the database and in the cache
DB_FEED_TIME = 100
CACHE_FEED_TIME = 199

class Account(db.Model):
    user = db.UserProperty(required=True)

class Feed(db.Model):
    service = db.StringProperty(required=True)
    username = db.StringProperty(required=True)
    content = db.TextProperty()
    timestamp = db.DateTimeProperty(auto_now=True)

class AccountFeed(db.Model):
    account = db.ReferenceProperty(Account, required=True, collection_name='feeds')
    feed = db.ReferenceProperty(Feed, required=True, collection_name='accounts')

class Subscribe(db.Model):
    subscriber = db.ReferenceProperty(Account, required=True, collection_name='subscriptions')
    subscribee = db.ReferenceProperty(Account, required=True, collection_name='subscribers')

class Entry:
    def __init__(self, service=None, username=None, title=None, link=None, content=None, timestamp=None):
        self.service = service
        self.username = username
        self.title = title
        self.link = link
        self.content = content
        self.timestamp = timestamp
    def to_dict(self):
        return self.__dict__
    def to_xml(self):
        str = """<entry>
                    <service>%s</service>
                    <username>%s</username>
                    <title>%s</title>
                    <link>%s</link>
                    <content><![CDATA[%s]]></content>
                    <timestamp>%s</timestamp>
                </entry>"""
        return str % (self.service, self.username, escape(self.title), self.link, self.content, self.timestamp)        

class DB:
    @staticmethod
    def getAccount(user):
        return Account.gql("WHERE user = :1", user).get()
    
    @classmethod
    def getAccountForEmail(cls, email):
        user = users.User(email)
        return cls.getAccount(user)

    @staticmethod
    def getFeed(service, username):
        return Feed.gql("WHERE service = :1 AND username = :2", service, username).get()
    
    @staticmethod
    def create_subscription(user, email):
        subscriber = DB.getAccount(user)
        subscribee = DB.getAccountForEmail(email)
        subscription = Subscribe.gql("WHERE subscriber = :1 AND subscribee = :2", subscriber, subscribee).get()
        if subscription is None:
            Subscribe(subscriber=subscriber, subscribee=subscribee).put()

class Cache:
    @staticmethod
    def setUserServices(account):
        userServices = [{'service': accountFeed.feed.service, 'username': accountFeed.feed.username} for accountFeed in account.feeds]
        if not memcache.set(account.user.email(), pickle.dumps(userServices)):
            logging.error('Cache set failed: userServices')
        return userServices
    @classmethod
    def getUserServices(cls, user):
        userServices_pickled = memcache.get(user.email())
        if userServices_pickled:
            userServices = pickle.loads(userServices_pickled)
        else:
            account = DB.getAccount(user)
            userServices = cls.setUserServices(account)
        return userServices

    @staticmethod
    def setEntries(feed):
        entries = GenericFeed.entries(feed)
        if not memcache.set("%s_%s" % (feed.service, feed.username), pickle.dumps(entries), CACHE_FEED_TIME):
            logging.error('Cache set failed: entries')
        return entries
    @classmethod
    def getEntries(cls, service, username):
        entries_pickled = memcache.get("%s_%s" % (service, username))
        if entries_pickled:
            entries = pickle.loads(entries_pickled)
        else:
            feed = DB.getFeed(service, username)
            entries = cls.setEntries(feed)
        return entries

class GenericFeed:
    @staticmethod
    def fetch(service, username):
        content = None
        # construct service url
        service_url = SERVICE_TEMPLATES[service] % username
        # fetch feed from service
        try:
            result = urlfetch.fetch(service_url)
            if result.status_code == 200:
                content = unicode(result.content, 'utf-8')
            else:
                logging.error("Error fetching content, HTTP status code = " + str(result.status_code))
        except urlfetch.DownloadError:
            logging.error("Exception caught: urlfetch.DownloadError")
        return content

    @classmethod
    def update(cls, feed):
        content = cls.fetch(feed.service, feed.username)
        if content:
            feed.content = content
            feed.put()

    @classmethod
    def entries(cls, feed):
        entries = []
        if (datetime.datetime.utcnow() - feed.timestamp) > datetime.timedelta(seconds=DB_FEED_TIME):
            cls.update(feed)
        feed_parsed = feedparser.parse(feed.content.encode('utf-8'))
        for item in feed_parsed.entries:
            entry = Entry()
            entry.service = feed.service
            entry.username = feed.username
            entry.title = item.title
            entry.link = item.link
            if item.summary:
                entry.content = item.summary
            else:
                entry.content = item.title
            entry.timestamp = time.mktime(item.updated_parsed)
            entries.append(entry)
        return entries

class Aggrogator:
    @staticmethod
    def get_feed(username):
        services = Aggrogator.get_services(username)
        entries = []
        for svc_tuple in set((svc['service'], svc['username']) for svc in services):
            entries.extend(Cache.getEntries(*svc_tuple))
        entries.sort(key=operator.attrgetter('timestamp'), reverse=True)
        return entries
    
    @staticmethod
    def get_services(username):
        accounts = []
        
        primary = DB.getAccountForEmail(username)
        accounts.append(primary)

        for subscription in primary.subscriptions:
            accounts.append(subscription.subscribee)
        services = []
        for account in accounts:
            services.extend(Cache.getUserServices(account.user))   
        return services             