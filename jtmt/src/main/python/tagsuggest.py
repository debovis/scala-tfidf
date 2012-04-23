#!/usr/bin/python
import sys
import getopt
import os.path
import re

### Configuration ###
DEFAULT_DICT_PATH = "/home/sujit/bin/blog_dict.txt" # path to dict file
TITLE_BOOST = 5      # score boost for title occurrences
COUNT_CUTOFF = 4     # min count to report a term
### Configuration ###

class InvertedIndex:
  """
  Class to convert the input file into an inverted index of words and
  their corresponding word positions. The class uses this data structure
  to return a count of occurrences of single or multi-word phrases in
  the document.
  """
  def __init__(self, inpath):
    self.index = {}
    self.wordSplitter = re.compile(r'[-_]')
    infile = open(inpath, 'r')
    regex = re.compile(r'[;,\.-_\" ]|<[^>].*>')
    pos = 0
    ignorable = False    # words are not counted while this is True
    while (True):
      line = infile.readline()
      if (not line):
        break
      # code blocks start with <pre> and end with </pre>, which we ignore.
      if (line.startswith("<pre>")):
        ignorable = True
      if (ignorable and line.startswith("</pre>")):
        ignorable = False
      if (ignorable):
        continue
      line = line[:-1].lower()
      words = regex.split(line)
      # make sure phrase matches spanning paragraphs are not considered
      pos = pos + 1
      boost = 1
      if line.startswith("<title>") and line.endswith("</title>"):
        # word matches in title are scored TITLE_BOOST times normal
        # this is done by considering the title line TITLE_BOOST times,
        # that way, neighboring words are still neighboring words, but
        # they just "occur" TITLE_BOOST times in the title.
        boost = TITLE_BOOST
      for i in range(0, boost):
        for word in words:
          try:
            positions = self.index[word]
          except KeyError:
            positions = set()
          pos = pos + 1
          positions.add(pos)
          self.index[word] = positions
    infile.close()

  def getCount(self, term):
    words = self.wordSplitter.split(term.lower())
    prevPositions = set()
    newPositions = set()
    for word in words:
      try:
        positions = self.index[word]
        if (len(prevPositions) == 0):
          # the previous positions set is empty, which could only
          # happen if this is the first word in our term. So there
          # is nothing to filter against, so the current positions
          # becomes the basis for the filtering. If it exits at this
          # point, because this is a single word phrase, then this
          # will report success or failure based on the length of
          # positions collection.
          prevPositions.update(positions)
        else:
          # we have a previous position set, so we compare each
          # entry in our current positions to see if it is 1 ahead
          # of an entry in our previous positions, The intersection
          # will form the basis of the previous positions for the
          # next word in the phrase.
          newPositions = positions.intersection(map(lambda x: x+1, prevPositions))
          prevPositions.clear()
          prevPositions.update(newPositions)
          newPositions.clear()
      except KeyError:
        return 0
    return len(prevPositions)

class Dictionary():
  """
  Class to encapsulate the creation of a data structure from the dictionary
  file. Format of the dictionary file is as follows:
  label:label_synonyms:label_categories
  where:
  label = the word or phrase that represents a label. If the label is
          a multi-word label, it should be hyphenated.
  label_synonyms = a comma-separated list of synonyms for the label. For
          example, webservice may be used instead of web-service. As in
          the label field, multi-word synonyms should be hyphenated.
  label_categories = a comma-separated list of categories the label should
          roll up to. For example, cx_oracle could roll up to databases,
          oracle, python and scripting.
  Internally, the data structure created is a map of label and label_synonyms
  pointing to a list of label_categories.
  """
  def __init__(self, dictpath):
    self.cats = {}
    self.syns = {}
    dictfile = open(dictpath, 'r')
    while (True):
      line = dictfile.readline()
      if (not line):
        break
      if (line.startswith("#")):
        # comment line, skip
        continue
      # strip out the line terminator, lowercase and replace all
      # whitespace with hyphen.
      line = line[:-1].lower().replace(" ", "-")
      (label, synonyms, categories) = line.split(":")
      # empty comma-separated synonyms or categories are stored in the
      # cats and syns dictionaries as a empty element, the filter
      # removes it so an empty string maps to an empty list
      self.cats[label] = filter(
        lambda x: len(x.strip()) > 0, categories.split(","))
      self.syns[label] = filter(
        lambda x: len(x.strip()) > 0, synonyms.split(","))
    dictfile.close

  def labels(self):
    """
    Return the list of all labels in the dictionary.
    @return the list of all labels.
    """
    return self.cats.keys()

  def synonyms(self, label):
    """
    Return the synonyms for the specified label. If no synonyms
    exist, returns an empty List.
    @param label the label to look up in the dictionary.
    @return a List of synonyms mapped to the label
    """
    try:
      return self.syns[label]
    except KeyError:
      return []

  def categories(self, label):
    """
    Return the categories for the specified label, If no categories
    exist, returns an empty List.
    @param label the label to look up in the dictionary.
    @return a List of categories mapped to the label.
    """
    try:
      return self.cats[label]
    except KeyError:
      return []
    
def usage(message=""):
  if (len(message) > 0):
    print "Error: %s" % (message)
  print "Usage: %s --help|([--dict=dict_file] --file=input_file)" % (sys.argv[0])
  print "-d|--dict: full path to dictionary file"
  print "-f|--file: full path to input file to be analyzed"
  print "-i|--interactive: prompt for each label and create a label string"
  print "-h|--help: print this information"
  sys.exit(2)

def validate():
  """
  Parses the command line parameters and extracts the relevant info
  from it. Returns a triple of (dictpath, filepath, interactive).
  @return a triple extracted from the command line parameters.
  """
  (opts, args) = getopt.getopt(sys.argv[1:], "d:f:ih",
    ["dict=", "file=", "interactive", "help"])
  dictpath = None
  filepath = None
  interactive = False
  for option, argval in opts:
    if (option in ("-h", "--help")):
      usage()
    if (option in ("-d", "--dict")):
      dictpath = argval
      if (not os.path.exists(dictpath)):
        usage("Dictionary File [%s] does not exist" % (dictpath))
    if (option in ("-f", "--file")):
      filepath = argval
      if (not os.path.exists(filepath)):
        usage("Input File [%s] does not exist" % filepath)
    if (option in ("-i", "--interactive")):
      interactive = True
  if (dictpath == None):
    dictpath = DEFAULT_DICT_PATH
  if (filepath == None):
    usage("Input file must be specified")
  return (dictpath, filepath, interactive)

def addCount(countmap, term, count):
  """
  Adds the count to the term count mapping. If no term count mapping
  exists, then one is created and the count updated.
  @param countmap the map of term to term counts to update.
  @param term the term to update for.
  @param count the term count to update.
  """
  try:
    origCount = countmap[term]
  except KeyError:
    origCount = 0
  countmap[term] = origCount + count

def main():
  (dictpath, inpath, interactive) = validate()
  dictionary = Dictionary(dictpath)
  invertedIndex = InvertedIndex(inpath)
  occurrences = {}
  # grab the basic occurrence counts for the labels and its synonyms
  for term in dictionary.labels():
    termCount = invertedIndex.getCount(term)
    addCount(occurrences, term, termCount)
    for synonym in dictionary.synonyms(term):
      # if synonyms exist, also look for occurrences of the synonyms and
      # add it to the occurrence count for the label
      addCount(occurrences, term, invertedIndex.getCount(synonym))
    for category in dictionary.categories(term):
      # add the updated term count (for base label and all its synonyms)
      # to the category occurrences.
      addCount(occurrences, category, occurrences[term])
  terms = occurrences.keys()
  # filter out terms whose counts are below our cutoff
  terms = filter(lambda x: occurrences[x] > COUNT_CUTOFF, terms)
  # sort the remaining (filtered) terms by their count descending
  terms.sort(lambda x, y: occurrences[y] - occurrences[x])
  if (interactive):
    labels = []
    for term in terms:
      yorn = raw_input("%s (%d) - include[Y/n]? " % (term, occurrences[term]))
      if (yorn == 'n' or yorn == 'N'):
        continue
      labels.append(term)
    print "Labels: %s" % (",".join(labels))
  else:
    for term in terms:
      print "%s (%d)" % (term, occurrences[term])
    print "Labels: %s" % (",".join(terms))
    
if __name__ == "__main__":
  main()
