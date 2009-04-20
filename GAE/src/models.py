'''
Created on Mar 23, 2009

@author: Anton Rau
'''

from google.appengine.ext import db
from google.appengine.ext import webapp
import appengine_admin


class Provider(db.Model):
	name = db.StringProperty(required=True)
	description = db.TextProperty()
	logo = db.BlobProperty()
	widget_url = db.LinkProperty()
	details_url = db.LinkProperty()
	whencreated = db.DateTimeProperty("Created", auto_now_add=True)
	whenupdated = db.DateTimeProperty("Updated", auto_now=True)
	
## Admin views ##
class AdminProvider(appengine_admin.ModelAdmin):
	model = Provider
	listFields = ('name', 'description', 'logo', 'widget_url', 'details_url')
	editFields = ('name', 'description', 'logo', 'widget_url', 'details_url')
	readonlyFields = ('whencreated', 'whenupdated')



class Account(db.Model):
	user = db.UserProperty("User", required=True)
	description = db.TextProperty("Description")
	averageCarbonFootprint = db.FloatProperty("Average Footprint")
	whencreated = db.DateTimeProperty("Created", auto_now_add=True)
	whenupdated = db.DateTimeProperty("Updated", auto_now=True)
	
	@staticmethod
	def getAccount(user):
		return Account.gql("WHERE user = :1", user).get()
		
	@classmethod
	def getAccountForEmail(cls, email):
		user = users.User(email)
		return cls.getAccount(user)
	
## Admin views ##
class AdminAccount(appengine_admin.ModelAdmin):
	model = Account
	listFields = ('user', 'description', 'averageCarbonFootprint', 'whencreated', 'whenupdated')
	editFields = ('user', 'description')
	readonlyFields = ('averageCarbonFootprint', 'whencreated', 'whenupdated')
	
		

class ProductCategory(db.Model):
	name = db.StringProperty(required=True)
	averageCarbonFootprint = db.FloatProperty()
	averageEnergyConsumption = db.FloatProperty()
	

class Producer(db.Model):
	name = db.StringProperty(required=True)
	link = db.LinkProperty()
	email = db.EmailProperty()
	phone = db.PhoneNumberProperty()
	address = db.PostalAddressProperty()
	rating = db.RatingProperty()
	description = db.TextProperty()
	logo = db.BlobProperty()
	

class Product(db.Model):
	code = db.StringProperty(required=True)
	name = db.StringProperty(required=True)
	category = db.ReferenceProperty(ProductCategory, required=True)
	producer = db.ReferenceProperty(Producer, required=True)
	description = db.TextProperty()
	rating = db.RatingProperty()
	carbonFootprint = db.FloatProperty()
	directEnergyConsumption = db.FloatProperty()
	indirectEnergyConsumption = db.FloatProperty()
	
	
class Order(db.Model):
	time = db.DateTimeProperty(required=True)
	customer = db.ReferenceProperty(Account, required=True)
	product = db.ReferenceProperty(Product, required=True)
	location = db.GeoPtProperty()



class FillTestData(webapp.RequestHandler):
	def get(self):
		Provider(name="UPC Database",
				 description="UPC barcodes database",
				 widget_url="http://carbon-14.appspot.com/services/upc/widget",
				 details_url="http://carbon-14.appspot.com/services/upc/details").put()
				 
		Provider(name="Rating",
				 description="Product rating system",
				 widget_url="http://carbon-14.appspot.com/services/rating/widget",
				 details_url="http://carbon-14.appspot.com/services/rating/details").put()
				 
		Provider(name="Environment",
				 description="Carbon footprint tracking",
				 widget_url="http://carbon-14.appspot.com/services/environment/widget",
				 details_url="http://carbon-14.appspot.com/services/environment/details").put()
				 
				 
		category = ProductCategory(name="Beverages").put()
		
		producer = Producer(name="The Coca-Cola Company").put()
		
		Product(code="0000040822938",
			name="Fanta Orange",
			producer=producer,
			category=category,
			description="Orange Soft Drink with Sugar and Sweeteners").put()
		
		Product(code="0000497000064",
			name="Sprite",
			producer=producer,
			category=category).put()


	
# Register to admin site
appengine_admin.register(AdminProvider, AdminAccount)
	
	
