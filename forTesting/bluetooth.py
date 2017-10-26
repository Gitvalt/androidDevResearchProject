# bluetooth is from PyBluez module
#import bluetooth
#import time

def writeLog(msg):
    file = open("myfile.txt", 'a')
	file.write("test" + "\n")

    
def readLog():
    readfile = open("myfile.txt", "r")
    print readfile.read()
    readfile.close()
    return

print("hello!")
input = raw_input("Enter something: ")
print "got: " + input    

writeLog()

    
#readLog()
#writeLog("haammsterriiii")
#writeLog("haammsterriiii")
#writeLog("haammsterriiii")




waitClose = raw_input()

"""
print "start find devices function"
try:
    #found_addresses = findDevice("Phone")
except IOError:
    print "We got a error " + error 
"""

"""
def findDevice(x):
    target_name = x
    target_address = None

    nearby_devices = bluetooth.discover_devices()
    
    for address in nearby_devices:
        if target_name == bluetooth.lookup_name(address):
            target_address = address
            break

    if target_address is not None:
        print "Got something: " + target_address
    else:
        print "Could not find device"
    
    return target_address

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
"""

