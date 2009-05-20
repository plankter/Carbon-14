__author__ = "Anton Rau"
__email__ = "contact@antonrau.net"
__copyright__ = "Copyright 2009, Anton Rau"
__license__ = "Apache License 2.0"
__version__ = "2009.05.14"
__status__ = "Development"


import os
import logging
import operator

from google.appengine.ext import webapp
from google.appengine.ext.webapp import util
from google.appengine.ext.webapp import template
from google.appengine.api import users
from google.appengine.api import memcache

import appengine_admin

from django.utils import simplejson
from services import providers

class MainPage(webapp.RequestHandler):
	def get(self):
		# get the current user
		user = users.get_current_user()

		# is user an admin?
		admin = users.is_current_user_admin();

		# create logout url
		logout_url = users.create_logout_url(self.request.uri)

		template_values = {
						   'user': user,
						   'admin': admin,
						   'logout_url': logout_url,
						   }
		
		path = os.path.join(os.path.dirname(__file__), 'main.html')
		self.response.out.write(template.render(path, template_values))
		
		
class Reset(webapp.RequestHandler):
	def get(self):
		memcache.flush_all()
		

		
def main():
	application = webapp.WSGIApplication(
		[
			('/', MainPage),
			('/reset', Reset),
			(r'^(/admin)(.*)$', appengine_admin.Admin),
		],
		debug=True)
	util.run_wsgi_app(application)
	

if __name__ == '__main__':
	main()
