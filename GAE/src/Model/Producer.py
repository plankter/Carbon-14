'''
Created on Mar 11, 2009

@author: Anton Rau
'''

from google.appengine.ext import db

class Producer(db.Model):
    name = db.StringProperty(required=True)
    category = db.CategoryProperty()
    link = db.LinkProperty()
    email = db.EmailProperty()
    phone = db.PhoneNumberProperty()
    address = db.PostalAddressProperty()
    rating = db.RatingProperty()
    description = db.TextProperty()
    logo = db.BlobProperty()



    def __init__(selfparams):
        '''
        Constructor
        '''
        