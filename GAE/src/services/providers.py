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

import appengine_admin


class Provider(db.Model):
	name = db.StringProperty(required=True)
	description = db.TextProperty()
	logo = db.BlobProperty()
	widget_url = db.LinkProperty()
	details_url = db.LinkProperty()
	created = db.DateTimeProperty("Created", auto_now_add=True)
	updated = db.DateTimeProperty("Updated", auto_now=True)
	
## Admin views ##
class AdminProvider(appengine_admin.ModelAdmin):
	model = Provider
	listFields = ('name', 'description', 'logo')
	editFields = ('name', 'description', 'logo', 'widget_url', 'details_url')
	readonlyFields = ('created', 'updated')
	
# Register to admin site
appengine_admin.register(AdminProvider)
	

class ProvidersPage(webapp.RequestHandler):
	def get(self):
		providers = self.get_providers()
		self.response.out.write(providers)	
	
	def get_providers(self):
		providers = memcache.get("providers")
		if providers is not None:
			return providers
		else:	
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
	

class GenerateTestData(webapp.RequestHandler):
	def get(self):
		Provider(name="Carbon",
				 description="Carbon footprint tracking",
				 widget_url="http://carbon-14.appspot.com/services/carbon/widget",
				 details_url="http://carbon-14.appspot.com/services/carbon/details").put()
				 
		Provider(name="Energy",
				 description="Energy tracking",
				 widget_url="http://carbon-14.appspot.com/services/energy/widget",
				 details_url="http://carbon-14.appspot.com/services/energy/details").put()
				 
		Provider(name="APriori",
				 description="Product rating system",
				 widget_url="http://carbon-14.appspot.com/services/apriori/widget",
				 details_url="http://carbon-14.appspot.com/services/apriori/details").put()
				 
		Provider(name="UPC",
				 description="UPC barcodes database",
				 widget_url="http://carbon-14.appspot.com/services/upc/widget",
				 details_url="http://carbon-14.appspot.com/services/upc/details").put()

	
def main():
	application = webapp.WSGIApplication([
		('/services/providers/get', ProvidersPage),
		('/services/providers/generate', GenerateTestData),
		], debug=True)
	util.run_wsgi_app(application)

if __name__ == '__main__':
	main()
	