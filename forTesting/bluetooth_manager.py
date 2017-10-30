from Config import *
from File_manager import *
from bluetooth import *

#is selected device available for connection?
#(True | False | {error_message})
def isDeviceAvailable(targetMAC):
    writeLog("Testing if MAC: '%s' is available" % targetMAC)
	try:
		#devices found (paired devices will respond with true even if not currently available)
		nearby_devices = discover_devices()

        #go through every found device
		for address in nearby_devices:

			#if we found the address we are looking for, then respond with true
			if str(address) == str(targetMAC):
				printedMsg = "Target MAC '{0}' was found".format(targetMAC)
				writeLog(printedMsg)
				return True

        #if for loop has been completed
		else:
			writeLog("Bluetooth device with address '{0}' was not found".format(targetMAC))
			return False

	except IOError as error:
		print "Error in finding the devices:" + error + "\n"
		return error

#Use bluetooth to find any available bluetooth devices. NOTE! paired devices are always returned as found
def findDevices():
	writeLog("Looking for bluetooth devices")
	try:
		nearby_devices = discover_devices()

		i = 0
		for address in nearby_devices:
			writeLog("A device is found. Address: " + address)
			i = i + 1

        #all found devices has been handled
		else:

            writeLog("Finding devices completed:")

            if i > 0:
                writeLog("Devices were found! Count: " + str(i))
				return nearby_devices

			else:
				writeLog("No devices were found")
				return None

	except IOError as error:
        writeLog("Finding devices completed:")
		writeLog("Devices could not be read. ERROR! " + error.message)
		return None

# Listen to incoming messages from bluetooth device on port x
def listenForCommunication():

    writeLog("Listening for incoming bluetooth communication")

	#id used to detect the service
	UUID = Config.uuid

	#create service and start to listen for responses at channel port
	server_socket = BluetoothSocket(RFCOMM)
	server_socket.bind(("", PORT_ANY))
	server_socket.listen(1)

	#get name of the channel
	port = server_socket.getsockname()[1]

    #Advertises the service to devices scanning for it
	advertise_service( server_socket, "Picture Frame Application",
						service_id = UUID,
						service_classes = [ UUID, SERIAL_PORT_CLASS ],
						profiles = [ SERIAL_PORT_PROFILE ],
	#                   protocols = [ OBEX_UUID ]
						)

	#write current status
	msg = "Listening for communication from RFCOMM channel {0}".format(port)
	writeLog(msg)

	#server now starts listening for connections from RFCOMM. Continues when
	client, client_info = server_socket.accept()

	try:
		while True:
			data = client.recv(1024)
			if len(data) == 0: break
			writeLog("Data received: {0}".format(data))

	except IOError:
		pass

    writeLog("Disconnected")
	client.close()
	server_socket.close()
    writeLog("Listening for communication ended")
    return data

# Send information to the found device
def sendToDevice(deviceMAC, message):

    writeLog("Sending message '{0}' to device with address '{1}'".format(message, deviceMAC))

	UUID = Config.uuid

	if (deviceMAC is None or message is None):
		writeLog("You need a device address and a message in order to send bluetooth message")
		return False

	else:
		#find bluetooth service with id {UUID} from address {deviceMac}
		service_matches = find_service( uuid = UUID, address = deviceMAC )
		if len(service_matches) == 0:
			print "Could not find service"
			writeLog("Could not find the service")
			return False

		foundService = service_matches[0]
		port = foundService["port"]
		name = foundService["name"]
		host = foundService["host"]

		print "Service was found"

        print_msg = "Found service: '{0}' from port: '{1}' at host: '{2}'".format(name, port, host)
		writeLog(print_msg)
		print print_msg
		sys.stdout.flush() #force terminal to print pending prints

		# Create the client socket
		sock = BluetoothSocket( RFCOMM )
		sock.connect((host, port))

		print("connected.  type stuff")
		sys.stdout.flush() #force terminal to print pending prints

		while True:
		    data = input()
		    if len(data) == 0: break
		    sock.send(data)
		sock.close()
		print "\n"
		return True
