'''
Created on Apr 8, 2009

@author: Anton Rau
'''

from google.appengine.api import memcache
from google.appengine.ext import db


class Service(db.Model):
    name = db.StringProperty(required=True)
    description = db.TextProperty()
    logo = db.BlobProperty()
    data_url = db.LinkProperty()
    widget_url = db.LinkProperty()
    details_url = db.LinkProperty()
    

class ServicesPage(webapp.RequestHandler):
    def get(self):
        
        
        

        template_values = {
                           'user': user,
                           'admin': admin,
                           'logout_url': logout_url,
                           }
        
        path = os.path.join(os.path.dirname(__file__), 'templates/index.html')
        self.response.out.write(template.render(path, template_values))
        
    
    
    def get_services(self):
        services = memcache.get("services")
        if services is not None:
            return services
        else:
            services = self.render_services()
            if not memcache.add("services", services, 10):
                logging.error("Memcache set failed.")
            return services

    def render_services(self):
        results = db.GqlQuery("SELECT * "
                              "FROM Service "
                              "ORDER BY date DESC").fetch(10)
        output = StringIO.StringIO()
        for result in results:
            output.write("")            
            output.write("<blockquote>%s</blockquote>" %
                         cgi.escape(result.content))
        return output.getvalue()
    
    