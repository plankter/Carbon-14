'''
Created on Apr 9, 2009

@author: Anton
'''

import xmlrpclib
import os

from google.appengine.api import memcache
from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.ext.webapp import util
from google.appengine.ext.webapp import template

class WidgetPage(webapp.RequestHandler):
    def get(self):
        barcode = self.request.get('barcode')
        
        server = xmlrpclib.ServerProxy('http://www.upcdatabase.com/rpc')
        result = server.lookupUPC(barcode)

        if type(result) == dict:
            if result['found']:
                description = result['description']
                size = result['size']
            
                template_values = {
                           'description': description,
                           'size': size,
                           }
        
                path = os.path.join(os.path.dirname(__file__), 'widget.html')
                self.response.out.write(template.render(path, template_values))
        
        
        
        
def main():
    application = webapp.WSGIApplication([
        ('/services/upc/widget', WidgetPage),
        ], debug=True)
    util.run_wsgi_app(application)

if __name__ == '__main__':
    main()