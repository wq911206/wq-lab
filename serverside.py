from __future__ import with_statement

import cgi
import urllib

from google.appengine.api import users
from google.appengine.ext import ndb
from google.appengine.ext import db
import webapp2
from stream import Stream
from stream import Picture

from google.appengine.ext import blobstore
from google.appengine.ext.webapp import blobstore_handlers

from google.appengine.api import images

import jinja2
import os

JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)


# -*- coding: utf-8 -*-
#
# jQuery File Upload Plugin GAE Python Example 2.2.0
# https://github.com/blueimp/jQuery-File-Upload
#
# Copyright 2011, Sebastian Tschan
# https://blueimp.net
#
# Licensed under the MIT license:
# http://www.opensource.org/licenses/MIT
#


from google.appengine.api import files, images
from google.appengine.ext import blobstore, deferred
from google.appengine.ext.webapp import blobstore_handlers
import json
import re
import urllib
import webapp2

WEBSITE = 'https://blueimp.github.io/jQuery-File-Upload/'
MIN_FILE_SIZE = 1  # bytes
MAX_FILE_SIZE = 5000000  # bytes
IMAGE_TYPES = re.compile('image/(gif|p?jpeg|(x-)?png)')
ACCEPT_FILE_TYPES = IMAGE_TYPES
THUMBNAIL_MODIFICATOR = '=s80'  # max width / height
EXPIRATION_TIME = 30000000000  # seconds


def cleanup(blob_keys):
    blobstore.delete(blob_keys)


class UploadHandler(webapp2.RequestHandler):

    def initialize(self, request, response):
        super(UploadHandler, self).initialize(request, response)
        self.response.headers['Access-Control-Allow-Origin'] = '*'
        self.response.headers[
            'Access-Control-Allow-Methods'
        ] = 'OPTIONS, HEAD, GET, POST, PUT, DELETE'
        self.response.headers[
            'Access-Control-Allow-Headers'
        ] = 'Content-Type, Content-Range, Content-Disposition'

    def validate(self, file):
        if file['size'] < MIN_FILE_SIZE:
            file['error'] = 'File is too small'
        elif file['size'] > MAX_FILE_SIZE:
            file['error'] = 'File is too big'
        elif not ACCEPT_FILE_TYPES.match(file['type']):
            file['error'] = 'Filetype not allowed'
        else:
            return True
        return False

    def get_file_size(self, file):
        file.seek(0, 2)  # Seek to the end of the file
        size = file.tell()  # Get the position of EOF
        file.seek(0)  # Reset the file position to the beginning
        return size

    def write_blob(self, data, info):
        blob = files.blobstore.create(
            mime_type=info['type'],
            _blobinfo_uploaded_filename=info['name']
        )
        with files.open(blob, 'a') as f:
            f.write(data)
        files.finalize(blob)
        return files.blobstore.get_blob_key(blob)

    def handle_upload(self,stream_name):
        results = []
        blob_keys = []
        ss=1
        for name, fieldStorage in self.request.POST.items():
            #print fieldStorage.filename
            if type(fieldStorage) is unicode:
                continue
            result = {}
            result['name'] = re.sub(
                r'^.*\\',
                '',
                fieldStorage.filename
            )
            result['type'] = fieldStorage.type
            result['size'] = self.get_file_size(fieldStorage.file)
            if self.validate(result):                
                #img=images.resize(fieldStorage.value,300,300)
                img=fieldStorage.value
                blob_key = str(
                    self.write_blob(img, result)
                )
                blob_keys.append(blob_key)
                picture=Picture(parent=db.Key.from_path('User',users.get_current_user().nickname(),'Stream',stream_name))
                ss=picture.uploaddate
                picture.imgkey=blob_key
                picture.put()                              
            results.append(result)
        #print results
        return (results,ss)

    def post(self):
        stream_name=re.findall('=(.*)',self.request.headers['Referer'])[0]
        stream=Stream.query(Stream.name==stream_name, Stream.author==users.get_current_user()).fetch()[0]
        #stream.numberofpictures=stream.numberofpictures+1
        #stream.total=stream.total+1
        (results,ss)=self.handle_upload(stream_name)
        stream.lastnewdate=ss
        #print stream.total,stream.numberofpictures,stream.lastnewdate
        result = {'files': results}  
        stream.put()      
        s = json.dumps(result, separators=(',', ':'))
        if 'application/json' in self.request.headers.get('Accept'):
            self.response.headers['Content-Type'] = 'application/json'
        self.response.write(s)




application = webapp2.WSGIApplication(
    [
        ('/upload', UploadHandler)
    ],
    debug=True
)