from bottle import Bottle, run, request, response, abort, static_file,debug

import os
import json

app = Bottle()

prefix = "./albums"

@app.get('/')
def index():
    return static_file("index.html", root=".")

@app.get('/albums')
def albumlist():
   return album(None)

@app.get('/albums/<name>')
def album(name):
    if name is None:
       dir = prefix
    else:
       dir = "%s/%s" % (prefix, name)
    listing = os.listdir(dir)

    response.content_type = 'application/json'
    return json.dumps(sorted(listing), indent=3)

@app.get('/albums/<name>/<img>')
def image(name, img):
    filepath = "%s/%s" % (name, img)
    return static_file(filepath, root=prefix)

run(app, host='localhost', port=8080, debug=True)
