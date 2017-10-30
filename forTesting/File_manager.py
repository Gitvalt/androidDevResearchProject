import time
from Config import *
import os.path

#Return local time
def getLocalTime():
	return time.asctime(time.localtime(time.time()))

def doesFileExist(filename):
	if os.path.isfile(filename) is True:
		print "File exists"
		return True
	else:
		print "File does not exist"
		return False

#Write information to a log
def writeLog(msg):
    if msg is None:
        print 'Cannot add a empty string to log file'
    else:
		file = open(logFile, 'a+')
		file.write("Action: '{0}' executed at '{1}'\n".format(msg, getLocalTime()))

#Read content of the log
def readLog():
	readfile = open(logFile, "r")
	print readfile.read()
	readfile.close()

#Empty the log
def clearLog():
	file = open(logFile, 'w')
	file.write("New log created on: '%s' \n" % getLocalTime())
