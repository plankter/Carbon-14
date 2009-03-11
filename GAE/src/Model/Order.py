'''
Created on Mar 11, 2009

@author: Anton Rau
'''

from google.appengine.ext import db

class Order(db.Model):
    time = db.DateTimeProperty(required=True)
    owner = db.ReferenceProperty(Customer, required=True)
    product = db.ReferenceProperty(Product, required=True)
    location = db.GeoPtProperty()


    def __init__(selfparams):
        '''
        Constructor
        '''
        