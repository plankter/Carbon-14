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


class Service(db.Model):
    name = db.StringProperty(required=True)
    description = db.TextProperty()
    logo = db.BlobProperty()
    data_url = db.LinkProperty()
    widget_url = db.LinkProperty()
    details_url = db.LinkProperty()
    

class ServicesPage(webapp.RequestHandler):
    def get(self):
        services = self.get_services()
        self.response.out.write(services)
        
    
    
    def get_services(self):
        services = memcache.get("services")
        if services is not None:
            return services
        else:
            service = Service(name="UPC Database",
                              description="If you're interested in the various forms of the UPC code, how the numbers are issued, UPC bar codes, etc, then this is the place to be.",
                              data_url="http://www.upcdatabase.com/rpc",
                              widget_url="",
                              details_url="")
            service.put()
            
            services = self.render_services()
            if not memcache.add("services", services):
                logging.error("Memcache set failed.")
            return services

    def render_services(self):
        services = Service.all().fetch(100)
        
        template_values = {
                           'services': services,
                           }
        
        path = os.path.join(os.path.dirname(__file__), 'providers.xml')
        result = template.render(path, template_values)
        return result
    

    
def main():
    application = webapp.WSGIApplication([
        ('/services', ServicesPage),
        ], debug=True)
    util.run_wsgi_app(application)

if __name__ == '__main__':
    main()
    