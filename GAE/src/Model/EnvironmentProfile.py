'''
Created on Mar 11, 2009

@author: Anton Rau
'''

from google.appengine.ext import db

class EnvironmentProfile(db.Model):
    energyConsumption = db.FloatProperty()
    carbonFootprint = db.FloatProperty()


    def __init__(selfparams):
        '''
        Constructor
        '''
        