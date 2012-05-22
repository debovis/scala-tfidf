from xlrd import cellname,open_workbook as ow
import simplejson,random,sys
from sets import Set
import urllib2,urllib

class testSimilarity():

	def __init__(self,path='server'):
		self.file_location = 'src/main/resources/questions.xls'
		if path == 'local':
			self.host = 'http://localhost'
		else:
			self.host = 'http://analytics.sparcloud.net'
		self.port = '8080'
		self.simAddress = 'similarity'
		self.uri = '{0}:{1}/{2}'.format(self.host,self.port,self.simAddress)
		self.headers = {'Content-Type': 'application/json'}
		self.questions = self.xlsToJson(self.file_location)

	def dataObj(self):
		return {
				'data_set':[],
				'comparison_document':{'title':'', 'value': ''}
				}

	def xlsToJson(self,file_location):
		wb = ow(file_location)
		questionsSheet = wb.sheet_by_index(0)
		questions = {}

		for i in range(1,questionsSheet.nrows):
			questions[i] = {
							'question'	: questionsSheet.cell(i,1).value,
							'answer'	: questionsSheet.cell(i,4).value,
							'category'	: str(questionsSheet.cell(i,5).value).split(',')
							}
		return questions
	
	def getCategories(self):
		d = [self.questions[q]['category'] for q in self.questions.keys()]
		s = Set()
		for listItem in d:
			for item in listItem:
				s.add(item)
		return s
	
	def getQuestionsInCategory(self,category):
		return [self.questions[q] for q in self.questions if category in self.questions[q]['category']]

	def querySimilarity(self,data):
		newData = simplejson.dumps({'data':data})
		prettyJson = simplejson.dumps({'data':data}, sort_keys=True, indent=4 * ' ')
		#print newData
		print '\n'.join([l.rstrip() for l in  prettyJson.splitlines()])
		request = urllib2.Request(self.uri,newData,self.headers)
		try:
			f = urllib2.urlopen(request)
			sim = simplejson.dumps(f.read(), sort_keys=True, indent=4 * ' ')
			f.close()
			return sim
		except Exception:
			return 'fail, make sure client is running currently accessing {0}'.format(self.uri)

	def randomQuestionSet(self):
		questions = self.questions
		data = self.dataObj()
		def getQs():
			categories = self.getCategories()
			category = random.choice(list(categories))
			return {'qs' : self.getQuestionsInCategory(category), 'category' :category}

		while 1:
			qs = getQs()
			questions = qs['qs']
			category = qs['category']
			if len(questions)<5:
				continue
			#elif len(questions):
			else: 
				break
		
		i=0
		for item in questions:
			data['data_set'].append({'title':str('doc{0}'.format(i)), 'value': item['question']})
			i = i+1
		
		# Lets take a random question from sample to compare
		randomQuestion = random.randint(0,len(questions)-1)
		data['comparison_document']['title'] = 'compDoc'
		data['comparison_document']['value'] = questions[randomQuestion]['question']

		del data['data_set'][randomQuestion]
		print 'category chosen is: {0}'.format(category)
		return self.querySimilarity(data)
		

if __name__ == '__main__':
	args = sys.argv
	if len(args)>1:
		ts = testSimilarity(args[1])
	else: 
		ts = testSimilarity()
	print '\n'.join([l.rstrip() for l in ts.randomQuestionSet().splitlines()])
	
	
