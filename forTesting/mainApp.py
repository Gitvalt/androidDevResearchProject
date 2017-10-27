# bluetooth is from PyBluez module
import bluetooth
import time

def getLocalTime():
	return time.asctime(time.localtime(time.time()))

def writeLog(msg):
    if msg is None:
        print 'Message to log cannot be empty'
    else:
        file = open("myfile.txt", 'a')
        file.write("Action: '" +  msg + "' on " + getLocalTime() + "\n")
    
def readLog():
	readfile = open("myfile.txt", "r")
	print readfile.read()
	readfile.close()
	
def clearLog():
	file = open("myfile.txt", 'w')
	file.write("New log created on: '" + getLocalTime() + "'\n")

def connectToDevice(targetMAC):
	print "Looking for the device"
	target_name = targetMAC
	target_address = None

	try:
		nearby_devices = bluetooth.discover_devices()
		
		for address in nearby_devices:
			print address
			writeLog("Address found: " + address)
			
			
	except IOError as error:
		print "Error in discovering devices"
		print error

	
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
waitClose = raw_input()
