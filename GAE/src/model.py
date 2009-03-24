'''
Created on Mar 23, 2009

@author: Anton Rau
'''

from google.appengine.ext import db


class Account(db.Model):
    user = db.UserProperty(required=True)
    description = db.TextProperty()
    averageCarbonFootprint = db.FloatProperty()
    
    @staticmethod
    def getAccount(user):
        return Account.gql("WHERE user = :1", user).get()
        
    @classmethod
    def getAccountForEmail(cls, email):
        user = users.User(email)
        return cls.getAccount(user)
        

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
    
    