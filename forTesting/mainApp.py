from bluetooth import *
from Config import *
from File_manager import *
from bluetooth_manager import *
import time
import sys

#This is a python application for testing bluetooth based applications
#"bluetooth" is from opensource PyBluez -module

# ---

def runMainApp():

	# the executable portions starts:
	# clear log and set startup
	print "Program is now starting...\n"
	sys.stdout.flush() #force terminal to print pending prints
	
	clearLog()
	writeLog("Program has started")

	print "Find nearby bluetooth devices"
	getDevices = findDevices()

	if getDevices is None:
		print "No devices available"
	else:
		print "Devices were found!"


	writeLog("Program has ended")

runMainApp()
print readLog()
