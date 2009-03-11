'''
Created on Mar 11, 2009

@author: Anton Rau
'''

from google.appengine.ext import db

class Customer(db.Model):
    user = db.UserProperty()
    orders = db.ListProperty(Order)
    environmentProfile = db.ReferenceProperty(EnvironmentProfile)
    

    def __init__(selfparams):
        '''
        Constructor
        '''
        