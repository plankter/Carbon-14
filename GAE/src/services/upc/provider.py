'''
Created on Apr 9, 2009

@author: Anton Rau
'''

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
		
		
		
		
def main():
	application = webapp.WSGIApplication([
		('/services/upc/widget', WidgetPage),
		('/services/upc/details', DetailsPage),
		], debug=True)
	util.run_wsgi_app(application)

if __name__ == '__main__':
	main()