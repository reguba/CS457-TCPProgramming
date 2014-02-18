import socket, select, string, sys
 
def prompt():
	sys.stdout.write('<You> ')
	sys.stdout.flush()
	
	
	
host = ''
port = 5002
 
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.settimeout(2)

try:
	s.connect((host,port))
except:
	print 'Unable to connect'
	sys.exit()
 
print 'Connect to Server. Start sending messages.'

while 1:
	s_list = [sys.stdin, s]
	inRdy, outRdy, errRdy = select.select(s_list, [], [])
	
	for sock in inRdy:
		if sock == s:
			data = s.recv(1024)
			if not data:
				print '\nDisconnected from server'
				sys.exit()
			else:
				sys.stdout.write(data)
		else:
			msg = sys.stdin.readline()
			s.send(msg)
	
