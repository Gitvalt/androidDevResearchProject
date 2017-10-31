# bluetooth is from opensource PyBluez module
import bluetooth
from bluetooth import *
import time
from uuid import getnode as get_mac
import sys
import Config
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
		#devices found (paired devices respond with true even if not available)
		nearby_devices = bluetooth.discover_devices()
		
		for address in nearby_devices:
			print "Comparing addresses"
			print address + "||" + targetMAC

			#if we found the address we are looking for, then respond with true
			if str(address) == str(targetMAC):
				printedMsg = "Target MAC {0} was found".format(targetMAC)
				writeLog(printedMsg)
				print printedMsg
				return True
			
		else:
			print "Reading found devices completed!"
			print "--MAC devices was not found--\n"
			writeLog("MAC devices was not found")
			return False

	except IOError as error:
		print "Error in finding the devices:" + error + "\n"
		return None

#Use bluetooth to find any available bluetooth devices. NOTE! paired devices are always returned as found
def findDevices():
	print "--Looking for devices--"

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
				print "--Devices were found! Count: " + str(i) + "--\n"
				return nearby_devices
			else:
				print "--No devices were found--\n"
				return None

	except IOError as error:
		print "Reading has been completed"
		print "--Devices could not be read: \n" + error.message + "--\n"
		return None

# Listen to incoming messages from bluetooth device on port x
def listenForCommunication():

	print "--Listening for connections--"
	#id used to detect the service
	UUID = Config.uuid

	#create service and start to listen for responses at channel port
	server_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
	server_socket.bind(("", bluetooth.PORT_ANY))
	server_socket.listen(1)

	#get name of the channel
	port = server_socket.getsockname()[1]

	advertise_service( server_socket, "SampleServer",
						service_id = UUID,
						service_classes = [ UUID, SERIAL_PORT_CLASS ],
						profiles = [ SERIAL_PORT_PROFILE ],
	#                   protocols = [ OBEX_UUID ]
						)

	#write current status
	msg = "Listening for communication from RFCOMM channel {0}".format(port)
	print msg
	writeLog(msg)
	sys.stdout.flush() #force to show printed data

	#server now starts listening for connections from RFCOMM. Continues when
	client, client_info = server_socket.accept()

	try:
		while True:
			data = client.recv(1024)
			if len(data) == 0: break
			print("received [%s]" % data)
			writeLog(data)
			sys.stdout.flush() #force terminal to print pending prints

	except IOError:
		pass

	print("disconnected")

	client.close()
	server_socket.close()
	print("--Receiving connetions has ended--\n")

# Send information to the found device
def sendToDevice(deviceMAC, message):

	UUID = Config.uuid
	if (deviceMAC is None or message is None):
		print "You need a device address and a message in order to send bluetooth message"
		return False
	else:
		#find bluetooth service with id {UUID} from address {deviceMac}
		service_matches = find_service( uuid = UUID, address = addr )
		if len(service_matches) == 0:
			print "Could not find service"
			writeLog("Could not find the service")
			return False

		foundService = service_matches[0]
		port = foundService["port"]
		name = foundService["name"]
		host = foundService["host"]

		print "Service was found"
		writeLog("Found service: '{0}' from port: '{1}' at host: '{2}'".format(name, port, host))

		# Create the client socket
		sock = BluetoothSocket( RFCOMM )
		sock.connect((host, port))

		print("connected.  type stuff")
		while True:
		    data = input()
		    if len(data) == 0: break
		    sock.send(data)

		sock.close()


		return True


def runMainApp():

	#the executable portions starts:
	print "Program is now starting\n"

	# clear log and set startup
	clearLog()
	writeLog("Looking for devices")
	foundDevice = findDevices()

	if foundDevice is None:
		print "No devices were found!"
		writeLog("No devices were found")
		return
	else:
		for address in foundDevice:
			response = isDeviceAvailable(address)
			if response is True:
				tmp = "Address '{0}' is available!".format(address)
				writeLog(tmp)
				print tmp
			else:
				tmp = "Address '{0}' is not available!".format(address)
				writeLog(tmp)
				print tmp

	print "\nCheck if available"

	#check if found device is available for connection
	arg = isDeviceAvailable('C0:EE:FB:26:EB:BC')
	sys.stdout.flush() #force terminal to print pending prints

	
	print "\nIf available start listening for communication"

	#if connection is available start listening for connections on port x
	if arg is True:
		print "--Device is available--\n"
		response = listenForCommunication()

	elif arg is None:
		print "Someting went wrong!"
	else:
		print "Device is not available"

	#end program and wait for user input
	writeLog("Program has ended")
	waitClose = raw_input("Write something!")

runMainApp()
