__author__ = "Anton Rau"
__email__ = "contact@antonrau.net"
__copyright__ = "Copyright 2009, Anton Rau"
__license__ = "Apache License 2.0"
__version__ = "2009.05.14"
__status__ = "Development"

import os

from google.appengine.api import memcache
from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.ext.webapp import util
from google.appengine.ext.webapp import template
from google.appengine.api import urlfetch

from xml.dom.minidom import parse, parseString


def requestData(barcode):
	url = "http://cocoa.ethz.ch:8081/RecommendationServer-war/ProductDataGatewayAndroid1?barcode=" + barcode + "&user=2"
	return urlfetch.fetch(url)

def getText(nodelist):
	rc = ""
	for node in nodelist:
		if node.nodeType == node.TEXT_NODE:
			rc = rc + node.data
	return rc

def handle404(self):
	path = os.path.join(os.path.dirname(__file__), '404.html')
	self.response.out.write(template.render(path, None))

class WidgetPage(webapp.RequestHandler):
	def get(self):
		barcode = self.request.get('barcode')
		result = requestData(barcode)
		
		if result.status_code == 200:
			dom = parseString(result.content)
			
			productId = int(getText(dom.getElementsByTagName("productId")[0].childNodes))
			if (productId == 0):
				handle404(self)
			else:
				productOverallScore = float(getText(dom.getElementsByTagName("productOverallScore")[0].childNodes))
				
				rating = int(productOverallScore)
				diff = productOverallScore - rating				
				showHalfStar = False
				
				if (diff < 0.25):
					showHalfStar = False
				elif ((diff >= 0.25) and (diff < 0.75)):
					showHalfStar = True
				else:
					rating = rating + 1
					showHalfStar = False
				
				template_values = {
							'rating': range(rating),
							'showHalfStar': showHalfStar,
							 }
			
				path = os.path.join(os.path.dirname(__file__), 'widget.html')
				self.response.out.write(template.render(path, template_values))
		else:
			handle404(self)
				
				
class DetailsPage(webapp.RequestHandler):
	def get(self):
		barcode = self.request.get('barcode')
		result = requestData(barcode)
		
		if result.status_code == 200:
			dom = parseString(result.content)
			
			productId = int(getText(dom.getElementsByTagName("productId")[0].childNodes))
			if (productId == 0):
				handle404(self)
			else:
				productId = int(getText(dom.getElementsByTagName("productId")[0].childNodes))
				productName = getText(dom.getElementsByTagName("productName")[0].childNodes)
				productOverallScore = float(getText(dom.getElementsByTagName("productOverallScore")[0].childNodes))
				totalNoOfRatings = int(getText(dom.getElementsByTagName("totalNoOfRatings")[0].childNodes))
				ownScore = float(getText(dom.getElementsByTagName("ownScore")[0].childNodes))
				
				rating = int(productOverallScore)
				diff = productOverallScore - rating				
				showHalfStar = False
				
				if (diff < 0.25):
					showHalfStar = False
				elif ((diff >= 0.25) and (diff < 0.75)):
					showHalfStar = True
				else:
					rating = rating + 1
					showHalfStar = False
				
				template_values = {
							'productId': productId,
							'productName': productName,
							'productOverallScore': productOverallScore,
							'totalNoOfRatings': totalNoOfRatings,
							'ownScore': ownScore,
							'rating': range(rating),
							'showHalfStar': showHalfStar,
							 }
			
				path = os.path.join(os.path.dirname(__file__), 'details.html')
				self.response.out.write(template.render(path, template_values))
		else:
			handle404(self)
		
		
		
		
def main():
	application = webapp.WSGIApplication([
		('/services/rating/widget', WidgetPage),
		('/services/rating/details', DetailsPage),
		], debug=True)
	util.run_wsgi_app(application)

if __name__ == '__main__':
	main()
