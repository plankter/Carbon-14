__author__ = "Anton Rau"
__email__ = "contact@antonrau.net"
__copyright__ = "Copyright 2009, Anton Rau"
__license__ = "Apache License 2.0"
__version__ = "2009.05.14"
__status__ = "Development"

import os

from google.appengine.api import users
from google.appengine.api import memcache
from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.ext.webapp import util
from google.appengine.ext.webapp import template

import appengine_admin


def handle404(self):
	path = os.path.join(os.path.dirname(__file__), '404.html')
	self.response.out.write(template.render(path, None))


def requestData(barcode):
	product = Product.gql("WHERE code = :1", barcode).get()
	return product


class WidgetPage(webapp.RequestHandler):
	def get(self):
		barcode = self.request.get('barcode')
		result = requestData(barcode)
		
		if result is not None:
			template_values = {
						'product': result,
						 }
		
			path = os.path.join(os.path.dirname(__file__), 'widget.html')
			self.response.out.write(template.render(path, template_values))
		else:
			handle404(self)
				
				
class DetailsPage(webapp.RequestHandler):
	def get(self):
		barcode = self.request.get('barcode')
		result = requestData(barcode)
		
		if result is not None:
			template_values = {
						'product': result,
						'url': '/services/energy/submit?barcode=' + barcode,
						 }
		
			path = os.path.join(os.path.dirname(__file__), 'details.html')
			self.response.out.write(template.render(path, template_values))
		else:
			handle404(self)
			
		
def main():
	application = webapp.WSGIApplication([
		('/services/energy/widget', WidgetPage),
		('/services/energy/details', DetailsPage),
		], debug=True)
	util.run_wsgi_app(application)

if __name__ == '__main__':
	main()