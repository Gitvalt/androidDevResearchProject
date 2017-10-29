# bluetooth is from opensource PyBluez module
import bluetooth
import time
from uuid import getnode as get_mac
import sys
#this is a python application for testing bluetooth based applications

def getLocalTime():
	return time.asctime(time.localtime(time.time()))

#Write information to a log
def writeLog(msg):
    if msg is None:
        print 'Cannot add a empty string to log file'
    else:
		file = open("myfile.txt", 'a')
		file.write("Action: '{0}' executed at '{1}'\n".format(msg, getLocalTime()))

#Read content of the log
def readLog():
	readfile = open("myfile.txt", "r")
	print readfile.read()
	readfile.close()

#Empty the log
def clearLog():
	file = open("myfile.txt", 'w')
	file.write("New log created on: '%s' \n" % getLocalTime())

#is selected device available for connection?
def isDeviceAvailable(targetMAC):
	print "Looking for the device: " + targetMAC
	writeLog("Testing if MAC: '%s' is available" % targetMAC)
	try:
		nearby_devices = bluetooth.discover_devices()

		for address in nearby_devices:
			print address + "||" + targetMAC
			if str(address) == str(targetMAC):
				print "target found"
				return True
			else:
				print "this is not a match! " + address

		else:
			print "Reading found devices completed!"
			writeLog("MAC devices was not found")
			return False

	except IOError as error:
		print "Error in finding the device:"
		print error
		return None

#Use bluetooth to find any available bluetooth devices
def findDevices():
	print "Looking for devices"

	try:
		nearby_devices = bluetooth.discover_devices()
		i = 0
		for address in nearby_devices:
			print "Device found: " + address
			writeLog("Address found: " + address)
			i = i + 1
		else:
			print "Reading has been completed"

			if i > 0:
				print "Devices were found! Count: " + str(i)
				return nearby_devices
			else:
				print "No devices were found"
				return None

		return nearby_devices

	except IOError as error:
		print "Error in discovering devices"
		print error

# Listen to incoming messages from bluetooth device on port x
def listenToPort(port_x, backlog):

	port = port_x
	server_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
	server_socket.bind(("", bluetooth.PORT_ANY))
	server_socket.listen(1)

	port_socket = server_socket.getsockname()[1]
	print "Found socket: '{0}'".format(port_socket)

	#print current situation
	printmessage = "Beginning to listen port '{0}' and setting backlog to '{1}'".format(port_socket, backlog)
	writeLog(printmessage)
	print printmessage + "\n"
	sys.stdout.flush() #force to show printed data

	client, client_info = server_socket.accept()

	while True:
		data = client.recv(1024)
		print "data received %s" & data
		sys.stdout.flush() #force to show printed data
		if raw_input() is not None:
			False


	client.close()
	server_socket.close()

# Send information to the found device
def sendToDevice(deviceMAC):

    if deviceMAC is None:
        return

    else:
        bd_addr = deviceMAC

        port = 1

        sock	=	bluetooth.BluetoothSocket( bluetooth.RFCOMM )
        sock.connect((bd_addr, port))

        sock.send("hello mobile device!!")

        sock.close()
        return


def runMainApp():

	#the executable portions starts:
	print "Program is now starting"

	# clear log and set startup
	clearLog()
	writeLog("Looking for devices")
	findDevices()

	#check if found device is available for connection
	arg = isDeviceAvailable('C0:EE:FB:26:EB:BC')

	sys.stdout.flush() #force to show printed data

	#if connection is available start listening for connections on port x
	if arg is True:
		print "Device is available"
		listenToPort(3, 1)
	elif arg is None:
		print "Someting went wrong!"
	else:
		print "Device is not available"

	#end program and wait for user input
	writeLog("Program has ended")
	waitClose = raw_input("Write something!")
