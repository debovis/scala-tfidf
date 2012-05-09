from xlrd import cellname,open_workbook as ow
import simplejson
from sets import Set
import urllib2,urllib

class testSimilarity():

	def __init__(self):
		self.file_location = 'src/main/resources/questions.xls'
		self.host = 'http://localhost'
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
							'question'	: questionsSheet.cell(i,0).value,
							'answer'	: questionsSheet.cell(i,3).value,
							'category'	: str(questionsSheet.cell(i,4).value).split(',')
							}
		return questions
	
	def getCategories(self):
		d = [questions[q]['category'] for q in self.questions]
		s = Set()
		for listItem in d:
			for item in listItem:
				s.add(item)
		return s
	
	def getQuestionsInCategory(self,category):
		return [questions[q] for q in questions if category in questions[q]['category']]

	def querySimilarity(self,data):
		newData = simplejson.dumps({'data':data})
		request = urllib2.Request(self.uri,newData,self.headers)
		try:
			f = urllib2.urlopen(request)
			sim = simplejson.dumps(f.read())
			f.close()
			return sim
		except Exception:
			return 'fail'


if __name__ == '__main__':
	ts = testSimilarity()
	questions = ts.questions
	data = ts.dataObj()

	qs = ts.getQuestionsInCategory('Linux')
	#cd = ts.getQuestionsInCategory('jquery')
	
	i=0
	for item in qs:
		data['data_set'].append({'title':str('doc{0}'.format(i)), 'value': item['question']})
		i = i+1
	
	data['comparison_document']['title'] = 'compDoc'
	data['comparison_document']['value'] = qs[-1]['question']

	del data['data_set'][-1]

	sim = ts.querySimilarity(data)

	print sim


#curl -d '{"data": {"data_set": [{"value": "What file should you edit to change  the  runlevel for your system?", "title": "doc0"}, {"value": "How do you check the current runlevel on a linux machine?", "title": "doc1"}, {"value": " What command is used for querying settings of an ethernet device and changing them.", "title": "doc2"}, {"value": "What  command displays the status of the cluster ", "title": "doc3"}, {"value": "How do you create a new ext4 filesystem?", "title": "doc4"}, {"value": "What Linux component provides firewalling?", "title": "doc5"}, {"value": "What is a typical Web server used on a Linux server?", "title": "doc6"}, {"value": "How do you display a volume group?", "title": "doc7"}, {"value": "If you were to create a new file system and wanted it to mount after a reboot, what file would need to be edited?", "title": "doc8"}, {"value": "What does the ethtool <interface name> command do?", "title": "doc9"}, {"value": "How would you check to see if a network interface had a link active?", "title": "doc10"}, {"value": "What command would you use to change run levels?", "title": "doc11"}, {"value": "How would you check to see if iptables would start at boot up?", "title": "doc12"}, {"value": "How would you install a kernel in Redhat without using yum?", "title": "doc13"}, {"value": "What command would you run to find out what package provides a certain file or binary? ", "title": "doc14"}, {"value": "How can you configure your server to boot into run level 3 upon every boot?", "title": "doc15"}, {"value": "How can you tell what services are configured to start at boot?", "title": "doc16"}], "comparison_document": {"value": "Whats the simplest way to list the processes on a system?", "title": "doc17"}}}' http://localhost:8080/similarity


























