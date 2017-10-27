# bluetooth is from opensource PyBluez module
import bluetooth
import time

#this is a python application for testing bluetooth based applications

def getLocalTime():
	return time.asctime(time.localtime(time.time()))

#Write information to a log
def writeLog(msg):
    if msg is None:
        print 'Message to log cannot be empty'
    else:
        file = open("myfile.txt", 'a')
        file.write("Action: '" +  msg + "' on " + getLocalTime() + "\n")

#Read content of the log
def readLog():
	readfile = open("myfile.txt", "r")
	print readfile.read()
	readfile.close()
	
#Empty the log
def clearLog():
	file = open("myfile.txt", 'w')
	file.write("New log created on: '" + getLocalTime() + "'\n")

#is selected device available for connection?
def isDeviceAvailable(targetMAC):
	print "Looking for the device: " + targetMAC
	
	try:
		nearby_devices = bluetooth.discover_devices()
		
		for address in nearby_devices:
			
			if address is targetMAC:
				writeLog("Address found: " + address)
				print "target found"
				return True
			
		else:
			print "No devices found!"
			writeLog("No devices were found")
			return False
			
	except IOError as error:
		print "Error in finding the device:"
		print error
		return False

#Use bluetooth to find any available bluetooth devices
def findDevices():
	print "Looking for devices"
	
	try:
		nearby_devices = bluetooth.discover_devices()
		
		for address in nearby_devices:
			print address
			writeLog("Address found: " + address)
		else:
			print "No devices were found"
			return None
		
		return nearby_devices
			
	except IOError as error:
		print "Error in discovering devices"
		print error

# Listen to incoming messages from bluetooth device on port x
def listenToPort(x):
    server_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    port = x

    server_socket.bind(("", port))
    server_socket.listen(1)
    
    client_sock,address = server_sock.accept()
    print "Accepted connection from ",address

    data = client_sock.recv(1024)
    print "received [%s]" % data

    client_sock.close()
    server_sock.close()
   
# Send information to the found device
def sendToDevice(deviceMAC):
    
    if deviceMAC is None:
        return
    
    else:
        bd_addr = deviceMAC

        port = 1

        sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
        sock.connect((bd_addr, port))

        sock.send("hello mobile device!!")

        sock.close()
        return

print "Program is now starting"

writeLog("Looking for devices")
findDevices()
arg = isDeviceAvailable('C0:EE:FB:26:EB:BC')

if arg is True:
	print "Device is available"
else:
	print "Device is not available"


waitClose = raw_input()
