#! /usr/bin/python
import getopt
import httplib
import simplejson as json
import sys
import urllib

DEFAULT_SERVER = "localhost:8080"

class RemoteDictionary:
  """
  A client facade for the Dictionary object exposed by the server.
  Contains methods with identical signatures as the remote object,
  so client application can treat it as a local object in application
  code.
  """
  def __init__(self, server=DEFAULT_SERVER):
    self.server = server

  def labels(self):
    data = self.__transport__("/labels")
    return json.loads(data)

  def synonyms(self, label):
    params = urllib.urlencode({"label" : label})
    data = self.__transport__("/synonyms?%s" % (params))
    return json.loads(data)

  def __transport__(self, url):
    try:
      conn = httplib.HTTPConnection(self.server)
      conn.request("GET", url)
      response = conn.getresponse()
      if (response.status == 200):
        data = response.read()
        return data
    finally:
      conn.close()

def usage(message=""):
  if (len(message) > 0):
    print "Error: %s" % (message)
  print "Usage: %s [--label=${label}] labels|synonyms" % (sys.argv[0])
  print "--label|-l: the label for which synonyms are needed."
  print "labels    : show all labels in the dictionary."
  print "synonyms  : show all synonyms for a given label. The --label"
  print "parameter is required."
  print "One of labels or synonyms is required."
  sys.exit(-1)

def main():
  # extract and validate command parameters
  (opts, args) = getopt.getopt(sys.argv[1:], "l:h", ["label=", "help"])
  operation = ""
  if (len(args) > 0):
    operation = args[0]
    if (operation != "labels" and operation != "synonyms"):
      usage("Invalid operation [%s], should be either 'labels' or 'synonyms'")
  else:
    usage("One of 'labels' or 'synonyms' must be specified")
  label = ""
  for option, argval in opts:
    if option in ("-h", "--help"):
      usage()
    if option in ("-l", "--label"):
      label = argval
  if (operation == "synonyms" and label == ""):
    usage("No label provided for 'synonyms' request")
  # pretend that this is a real application and delegate to the Remote
  # Dictionary Facade
  dictionary = RemoteDictionary()
  if (operation == "labels"):
    print dictionary.labels()
  else:
    print dictionary.synonyms(label)

if __name__ == "__main__":
  main()
