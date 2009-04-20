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

import models
	

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
		providers = models.Provider.all().fetch(100)
		
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
	