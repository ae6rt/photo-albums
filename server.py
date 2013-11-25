import os
import json

# thumbnails http://stackoverflow.com/questions/2612436/create-thumbnail-images-for-jpegs-with-python
# http://rsbweb.nih.gov/ij/

from bottle import Bottle, run, response, static_file

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


@app.get('/<filename:re:.*\.js>')
def javascripts(filename):
    print "here js %s" % filename
    return static_file(filename, root='static')


@app.get('/<filename:re:.*\.css>')
def stylesheets(filename):
    return static_file(filename, root='static')


run(app, host='localhost', port=8080, debug=True)
