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

from google.appengine.api import urlfetch
from xml.dom.minidom import parse, parseString

import appengine_admin



class Account(db.Model):
	user = db.UserProperty("User", required=True)
	description = db.TextProperty("Description")
	averageCarbonFootprint = db.FloatProperty("Average Footprint")
	created = db.DateTimeProperty("Created", auto_now_add=True)
	updated = db.DateTimeProperty("Updated", auto_now=True)
	
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
	listFields = ('user', 'description', 'averageCarbonFootprint', 'created', 'updated')
	editFields = ('user', 'description')
	readonlyFields = ('averageCarbonFootprint', 'created', 'updated')
	
		

class ProductCategory(db.Model):
	name = db.StringProperty(required=True)
	unit = db.StringProperty()
	shortTips = db.TextProperty()
	tips = db.TextProperty()
	minCarbonFootprint = db.FloatProperty()
	maxCarbonFootprint = db.FloatProperty()
	minDirectEnergyConsumption = db.FloatProperty()
	maxDirectEnergyConsumption = db.FloatProperty()
	minIndirectEnergyConsumption = db.FloatProperty()
	maxIndirectEnergyConsumption = db.FloatProperty()
	created = db.DateTimeProperty("Created", auto_now_add=True)
	updated = db.DateTimeProperty("Updated", auto_now=True)
	
	def __unicode__(self):
		return self.name
	
## Admin views ##
class AdminProductCategory(appengine_admin.ModelAdmin):
	model = ProductCategory
	listFields = ('name', 'created', 'updated')
	editFields = ('name', 'unit', 'shortTips', 'tips', 'minCarbonFootprint', 'maxCarbonFootprint', 'minDirectEnergyConsumption', 'maxDirectEnergyConsumption', 'minIndirectEnergyConsumption', 'maxIndirectEnergyConsumption')
	readonlyFields = ('created', 'updated')
	
	

class Producer(db.Model):
	name = db.StringProperty(required=True)
	link = db.LinkProperty()
	email = db.EmailProperty()
	phone = db.PhoneNumberProperty()
	address = db.PostalAddressProperty()
	description = db.TextProperty()
	logo = db.BlobProperty()
	created = db.DateTimeProperty("Created", auto_now_add=True)
	updated = db.DateTimeProperty("Updated", auto_now=True)
	
	def __unicode__(self):
		return self.name
	
## Admin views ##
class AdminProducer(appengine_admin.ModelAdmin):
	model = Producer
	listFields = ('name', 'description', 'created', 'updated')
	editFields = ('name', 'link', 'email', 'phone', 'address', 'description', 'logo')
	readonlyFields = ('created', 'updated')
	
	

class Product(db.Model):
	code = db.StringProperty(required=True)
	name = db.StringProperty(required=True)
	category = db.ReferenceProperty(ProductCategory, required=True)
	producer = db.ReferenceProperty(Producer, required=True)
	unitSize = db.FloatProperty()
	description = db.TextProperty()
	tips = db.TextProperty()
	methodology = db.StringProperty()
	totalCarbonFootprint = db.FloatProperty()
	materialCarbonFootprint = db.FloatProperty()
	manufacturingCarbonFootprint = db.FloatProperty()
	distributionCarbonFootprint = db.FloatProperty()
	usageCarbonFootprint = db.FloatProperty()
	disposalCarbonFootprint = db.FloatProperty()
	directEnergyConsumption = db.FloatProperty()
	indirectEnergyConsumption = db.FloatProperty()
	created = db.DateTimeProperty("Created", auto_now_add=True)
	updated = db.DateTimeProperty("Updated", auto_now=True)
	
	def __unicode__(self):
		return self.name
	
## Admin views ##
class AdminProduct(appengine_admin.ModelAdmin):
	model = Product
	listFields = ('code', 'name', 'category', 'producer', 'created', 'updated')
	editFields = ('code', 'name', 'category', 'producer', 'unitSize', 'tips', 'methodology', 'description', 'totalCarbonFootprint', 'materialCarbonFootprint', 'manufacturingCarbonFootprint', 'distributionCarbonFootprint', 'usageCarbonFootprint', 'disposalCarbonFootprint', 'directEnergyConsumption', 'indirectEnergyConsumption')
	readonlyFields = ('created', 'updated')
	
	
class Order(db.Model):
	customer = db.ReferenceProperty(Account, required=True)
	product = db.ReferenceProperty(Product, required=True)
	location = db.GeoPtProperty()
	created = db.DateTimeProperty("Created", auto_now_add=True)
	updated = db.DateTimeProperty("Updated", auto_now=True)
	
## Admin views ##
class AdminOrder(appengine_admin.ModelAdmin):
	model = Order
	listFields = ('customer', 'product', 'location', 'created', 'updated')
	editFields = ('customer', 'product', 'location')
	readonlyFields = ('created', 'updated')
			
	
# Register to admin site
appengine_admin.register(AdminAccount, AdminProductCategory, AdminProducer, AdminProduct, AdminOrder)


def handle404(self):
	path = os.path.join(os.path.dirname(__file__), '404.html')
	self.response.out.write(template.render(path, None))


def requestData(barcode):
	product = memcache.get(barcode)
	if product is not None:
		return product
	else:
		product = Product.gql("WHERE code = :1", barcode).get()
		#if not memcache.add(barcode, product):
		#	logging.error("Memcache set failed.")
		return product


class WidgetCarbonPage(webapp.RequestHandler):
	def get(self):
		barcode = self.request.get('barcode')
		product = requestData(barcode)
		
		if product is not None:
			template_values = {
						'product': product,
						 }
		
			path = os.path.join(os.path.dirname(__file__), 'widget_carbon.html')
			self.response.out.write(template.render(path, template_values))
		else:
			handle404(self)
				
				
class DetailsCarbonPage(webapp.RequestHandler):
	def get(self):
		barcode = self.request.get('barcode')
		product = requestData(barcode)
		
		if product is not None:
			bestProduct = Product.gql("WHERE category = :1 ORDER BY totalCarbonFootprint", product.category).get()
			# totalFootprint = product.materialCarbonFootprint + product.manufacturingCarbonFootprint + product.distributionCarbonFootprint + product.usageCarbonFootprint + product.disposalCarbonFootprint
			productFootprint = product.totalCarbonFootprint * product.unitSize
			template_values = {
						'product': product,
						'bestProduct': bestProduct,
						# 'totalFootprint': totalFootprint,
						'productFootprint': productFootprint,
						'url': '/services/carbon/submit?barcode=' + barcode,
						 }
		
			path = os.path.join(os.path.dirname(__file__), 'details_carbon.html')
			self.response.out.write(template.render(path, template_values))
		else:
			handle404(self)



class WidgetEnergyPage(webapp.RequestHandler):
	def get(self):
		barcode = self.request.get('barcode')
		product = requestData(barcode)
		
		if product is not None:
			if product.directEnergyConsumption is not None:
				template_values = {
							'product': product,
							 }
			
				path = os.path.join(os.path.dirname(__file__), 'widget_energy.html')
				self.response.out.write(template.render(path, template_values))
			else:
				handle404(self)
		else:
			handle404(self)
				
				
class DetailsEnergyPage(webapp.RequestHandler):
	def get(self):
		barcode = self.request.get('barcode')
		product = requestData(barcode)
		
		if product is not None:
			if product.directEnergyConsumption is not None:
				template_values = {
							'product': product,
							 }
			
				path = os.path.join(os.path.dirname(__file__), 'details_energy.html')
				self.response.out.write(template.render(path, template_values))
			else:
				handle404(self)
		else:
			handle404(self)


		

class SubmitPage(webapp.RequestHandler):
	def get(self):
		barcode = self.request.get('barcode')
		url = '/services/carbon/details?barcode=' + barcode
		# get the current user
		user = users.get_current_user()
		if not user:
			greeting = ("<a href=\"%s\">Sign in or register</a>." % users.create_login_url(url))
			self.response.out.write("<html><body>%s</body></html>" % greeting)		
		else:
			# create user account if haven't already
			account = Account.getAccount(user)
			if account is None:
				account = Account(user=user)
				account.put()
			
			product = requestData(barcode)
			order = Order(customer=account, product=product)
			order.put()
			self.redirect(url, True)




def updateProduct(barcode):
	url = "http://carbon-14.appspot.com/services/carbon/test?barcode=" + barcode
	result = urlfetch.fetch(url)
	
	if result.status_code == 200:
		dom = parseString(result.content)
		
		id = int(getText(dom.getElementsByTagName("id")[0].childNodes))
		if (id == barcode):
			product = requestData(barcode)
			
			distributionCarbonFootprint = float(getText(dom.getElementsByTagName("distributionCarbonFootprint")[0].childNodes))
			product.distributionCarbonFootprint = distributionCarbonFootprint
			totalFootprint = product.materialCarbonFootprint + product.manufacturingCarbonFootprint + product.distributionCarbonFootprint + product.usageCarbonFootprint + product.disposalCarbonFootprint
			product.totalCarbonFootprint = totalFootprint
			
			product.put()
		else:
			handle404(self)
	else:
		handle404(self)
	
	

def getText(nodelist):
	rc = ""
	for node in nodelist:
		if node.nodeType == node.TEXT_NODE:
			rc = rc + node.data
	return rc
		
class UpdatePage(webapp.RequestHandler):
	def get(self):
		barcode = self.request.get('barcode')
		updateProduct(barcode)
		self.redirect("http://carbon-14.appspot.com/services/carbon/admin/Product/", True)


class TestPage(webapp.RequestHandler):
	def get(self):
		barcode = self.request.get('barcode')
		distributionCarbonFootprint = 12
		
		template_values = {
						   'id': barcode,
						   'distributionCarbonFootprint': distributionCarbonFootprint
						   }
			
		path = os.path.join(os.path.dirname(__file__), 'test.xml')
		self.response.out.write(template.render(path, template_values))

		
#def UpdateProductCategory(product):
#	category = product.category
#	if (product.totalCarbonFootprint < category.minCarbonFootprint):
#		category.minCarbonFootprint = product.totalCarbonFootprint
#		category.bestProductCode = product.code
#		
#	if (product.totalCarbonFootprint > category.maxCarbonFootprint):
#		category.maxCarbonFootprint = product.totalCarbonFootprint
#		
#	category.put()
		
		
def main():
	application = webapp.WSGIApplication([
		('/services/carbon/widget', WidgetCarbonPage),
		('/services/carbon/details', DetailsCarbonPage),
		('/services/carbon/submit', SubmitPage),
		('/services/carbon/update', UpdatePage),
		('/services/carbon/test', TestPage),
		('/services/energy/widget', WidgetEnergyPage),
		('/services/energy/details', DetailsEnergyPage),
		(r'^(/services/carbon/admin)(.*)$', appengine_admin.Admin),
		], debug=True)
	util.run_wsgi_app(application)

if __name__ == '__main__':
	main()