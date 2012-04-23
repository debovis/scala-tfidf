#!/usr/bin/python
import cherrypy
import os.path
import simplejson as json

DICTFILE = "/home/sujit/tmp/dictionary.txt"

class Dictionary:

  def __init__(self, dictpath=DICTFILE):
    """
    Loads up the file into an internal data structure {String:Set(String)}
    @param dictpath the path to the dictionary file, defaults to DICTPATH
    """
    if (os.path.exists(dictpath)):
      self.dictionary = {}
      dictfile = open(dictpath, 'rb')
      while (True):
        line = dictfile.readline()[:-1]
        if (not line):
          break
        line = line.lower()             # lowercase the line
        line = line.replace(" ", "-")   # replace all whitespace by "-"
        vals = []
        if (line.find(":") > -1):
          (lhs, rhs) = line.split(":")
          if (rhs.find(",") > -1):
            vals.extend(rhs.split(","))
          else:
            vals.append(rhs)
          self.dictionary[lhs] = vals
        else:
          self.dictionary[line] = vals
      dictfile.close()

  def index(self):
    """
    This method is called when the / request is made. This is just an
    informational page, mostly for human consumption, it will not be
    called from the client.
    """
    return """<br/><b>To get back all labels in dictionary, enter:</b>
           <br/>http://localhost:8080/labels
           <br/><b>To get back synonyms for a given label, enter:</b>
           <br/>http://localhost:8080/synonyms?label=${label}"""

  def labels(self):
    """
    Returns a JSON list of dictionary keys. Each element in the list
    corresponds to a "label" in one of my blog posts.
    @return a JSON list of dictionary keys.
    """
    cherrypy.response.headers["Content-Type"] = "application/json"
    return json.dumps(self.dictionary.keys())

  def synonyms(self, label):
    """
    Given a dictionary key, returns its human-generated "synonyms". This
    corresponds to the RHS of the dictionary.txt file. The synonyms are
    returned as a JSON list.
    @param label the dictionary key, must be provided
    @return a JSON list of synonyms for the dictionary keys.
    """
    try:
      cherrypy.response.headers["Content-Type"] = "application/json"
      return json.dumps(self.dictionary[label])
    except KeyError:
      return "[]"

  index.exposed = True
  labels.exposed = True
  synonyms.exposed = True

def main():
  cherrypy.quickstart(Dictionary())
  
if __name__ == "__main__":
  main()
