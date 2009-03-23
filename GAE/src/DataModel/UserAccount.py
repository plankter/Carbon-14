'''
Created on Mar 11, 2009

@author: Anton Rau
'''

from google.appengine.ext import db

class UserAccount(db.Model):
    user = db.UserProperty(required=True)
    description = db.TextProperty()
    averageCarbonFootprint = db.FloatProperty()
    

    def __init__(selfparams):
        '''
        Constructor
        '''
        