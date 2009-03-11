'''
Created on Mar 11, 2009

@author: Anton Rau
'''

from google.appengine.ext import db

class ProductCategory(db.Model):
    name = db.StringProperty(required=True)
    averageCarbonFootprint = db.FloatProperty()
    averageEnergyConsumption = db.FloatProperty()
    


    def __init__(selfparams):
        '''
        Constructor
        '''
        