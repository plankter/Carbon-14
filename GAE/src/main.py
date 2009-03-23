import cgi
import os

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db
from google.appengine.ext.webapp import template


class MainPage(webapp.RequestHandler):
    def get(self):
        
        user = users.get_current_user()

        if users.get_current_user():
          url = users.create_logout_url(self.request.uri)
          url_linktext = 'Logout'
        else:
          url = users.create_login_url(self.request.uri)
          url_linktext = 'Login'
          
        updates = []
        account = None
        if user:
            account_query = UserAccount.all()
            account_query.filter('user = ', users.get_current_user())
            result_set = account_query.fetch(1)
            if len(result_set) > 0:
                account = account_query.fetch(1)[0]
            
            if account:
                updates = []
                for service in account.dynamic_properties():
                    url = getattr(account, service)
                    feed = GenericFeed(url, service)
                    updates.extend(feed.entries())
            else:
                account = UserAccount()
                account.user = user
                account.put()
            updates.sort(key=attrgetter('timestamp'), reverse=True)
    
        template_values = {
          'account': account,
          'updates': updates,
          'url': url,
          'url_linktext': url_linktext,
          }
    
        path = os.path.join(os.path.dirname(__file__), 'templates/index.html')
        self.response.out.write(template.render(path, template_values))


application = webapp.WSGIApplication(
                                     [('/', MainPage)],
                                      debug=True)

def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()