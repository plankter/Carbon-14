'''
Created on Mar 11, 2009

@author: Anton Rau
'''

from google.appengine.ext import db

class Product(db.Model):
    name = db.StringProperty()
    description = db.TextProperty()
    category = db.ReferenceProperty(ProductCategory)
    producer = db.ReferenceProperty(Producer)
    rating = db.RatingProperty()
    carbonFootprint = db.FloatProperty()
    directEnergyConsumption = db.FloatProperty()
    indirectEnergyConsumption = db.FloatProperty()
    


    def __init__(selfparams):
        '''
        Constructor
        '''
        