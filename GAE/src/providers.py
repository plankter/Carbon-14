'''
Created on Apr 8, 2009

@author: Anton Rau
'''

import os

from google.appengine.api import memcache
from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.ext.webapp import util
from google.appengine.ext.webapp import template


class Provider(db.Model):
    name = db.StringProperty(required=True)
    description = db.TextProperty()
    logo = db.BlobProperty()
    widget_url = db.LinkProperty()
    details_url = db.LinkProperty()
    

class ProvidersPage(webapp.RequestHandler):
    def get(self):
        providers = self.get_providers()
        self.response.out.write(providers)
        
    
    
    def get_providers(self):
        providers = memcache.get("providers")
        if providers is not None:
            return providers
        else:
            provider = Provider(name="UPC Database",
                              description="If you're interested in the various forms of the UPC code, how the numbers are issued, UPC bar codes, etc, then this is the place to be.",
                              widget_url="http://localhost:8080/services/upc/widget",
                              details_url="http://10.0.2.2:8080/services/upc/widget?")
            provider.put()
            
            providers = self.render_providers()
            if not memcache.add("providers", providers):
                logging.error("Memcache set failed.")
            return providers

    def render_providers(self):
        providers = Provider.all().fetch(100)
        
        template_values = {
                           'providers': providers,
                           }
        
        path = os.path.join(os.path.dirname(__file__), 'providers.xml')
        result = template.render(path, template_values)
        return result
    

    
def main():
    application = webapp.WSGIApplication([
        ('/providers', ProvidersPage),
        ], debug=True)
    util.run_wsgi_app(application)

if __name__ == '__main__':
    main()
    