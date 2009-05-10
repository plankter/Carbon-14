'''
Created on Apr 9, 2009

@author: Anton
'''

import os

from google.appengine.api import memcache
from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.ext.webapp import util
from google.appengine.ext.webapp import template
from google.appengine.api import urlfetch


def requestData(barcode):
	url = "http://cocoa.ethz.ch:8081/RecommendationServer-war/ProductDataGateway?barcode=" + barcode + "&user=2"
	return urlfetch.fetch(url)


class WidgetPage(webapp.RequestHandler):
	def get(self):
		barcode = self.request.get('barcode')
		result = requestData(barcode)
		
		if result.status_code == 200:
			data = result.content
			
			template_values = {
						'rating': range(5),
						 }
		
			path = os.path.join(os.path.dirname(__file__), 'widget.html')
			self.response.out.write(template.render(path, template_values))
		else:
			path = os.path.join(os.path.dirname(__file__), '404.html')
			self.response.out.write(template.render(path, None))
				
				
class DetailsPage(webapp.RequestHandler):
	def get(self):
		barcode = self.request.get('barcode')
		result = requestData(barcode)
		
		if result.status_code == 200:
			data = result.content
			
			template_values = {
						'rating': range(5),
						 }
		
			path = os.path.join(os.path.dirname(__file__), 'details.html')
			self.response.out.write(template.render(path, template_values))
		else:
			self.response.out.write("Product not found.")
		
		
		
		
def main():
	application = webapp.WSGIApplication([
		('/services/rating/widget', WidgetPage),
		('/services/rating/details', DetailsPage),
		], debug=True)
	util.run_wsgi_app(application)

if __name__ == '__main__':
	main()
