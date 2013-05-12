__author__ = "Anton Rau"
__email__ = "contact@antonrau.net"
__copyright__ = "Copyright 2009, Anton Rau"
__license__ = "Apache License 2.0"
__version__ = "2009.05.14"
__status__ = "Development"

import xmlrpclib
import os

from google.appengine.api import memcache
from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.ext.webapp import util
from google.appengine.ext.webapp import template


def requestData(barcode):
	server = xmlrpclib.ServerProxy('http://www.upcdatabase.com/rpc')
	return server.lookupUPC(barcode)


class WidgetPage(webapp.RequestHandler):
	def get(self):
		barcode = self.request.get('barcode')
		data = requestData(barcode)

		if type(data) == dict:
			if data['found']:
				description = data['description']
				size = data['size']
				country = data['issuerCountry']
			
				template_values = {
						   'description': description,
						   'size': size,
						   'country': country,
						   }
		
				path = os.path.join(os.path.dirname(__file__), 'widget.html')
				self.response.out.write(template.render(path, template_values))
			else:
				path = os.path.join(os.path.dirname(__file__), '404.html')
				self.response.out.write(template.render(path, None))
		else:
			path = os.path.join(os.path.dirname(__file__), '404.html')
			self.response.out.write(template.render(path, None))
				
				
class DetailsPage(webapp.RequestHandler):
	def get(self):
		barcode = self.request.get('barcode')
		data = requestData(barcode)

		if type(data) == dict:
			if data['found']:
				description = data['description']
				size = data['size']
				country = data['issuerCountry']
			
				template_values = {
						   'description': description,
						   'size': size,
						   'country': country,
						   }
		
				path = os.path.join(os.path.dirname(__file__), 'details.html')
				self.response.out.write(template.render(path, template_values))
			else:
				self.response.out.write("Product not found.")
		
		
		
		
def main():
	application = webapp.WSGIApplication([
		('/services/upc/widget', WidgetPage),
		('/services/upc/details', DetailsPage),
		], debug=True)
	util.run_wsgi_app(application)

if __name__ == '__main__':
	main()