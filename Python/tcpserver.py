import socket, select


def sendall(sock, message):
	for s in clients:
		if s != server and s != sock:
			try:
				s.send(message)
			except:
				s.close()
				clients.remove(s)


host = ''
port = 5002

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind((host,port))
server.listen(5)

alias={}



clients=[server]

while 1:
	inRdy, outRdy, errRdy = select.select(clients, [], [])
	for s in inRdy:
		if s == server:
			client, addr = server.accept()
			clients.append(client)
			alias[client.getpeername()]=str(client.getpeername())
			print "Client (%s, %s) connected" % addr
			
			sendall(client, "[%s:%s] entered the room\n" % addr)
		else:
			data = s.recv(1024)
			if data:
				if data.startswith('/userchange'):
					bits = data.split(" ")
					if len(bits) == 2:
						old = alias[s.getpeername()]
						alias[s.getpeername()]=bits[1].rstrip("\n")
						data = 'You changed your username to:'+bits[1] 
						s.send(data)
						msg = old+' is now going by: '+bits[1]
						sendall(s,msg)
					else:
						data = 'Proper use is: /userchange desired_username.\n' 
						s.send(data)
				elif data.startswith('/pm'):
					bits = data.split(" ",2)
					if len(bits) == 3:
						for addr, aka in alias.items():
							if aka == bits[1]:
								for m in clients:
									if m.getpeername()==addr:
										m.send(bits[2])
					else:
						data = 'Proper use is: /userchange desired_username.\n' 
						s.send(data)		
				else:
					sendall(s,"\r"+'<'+alias[s.getpeername()]+'> '+data)
			else:
				s.close()
				clients.remove(s)
	
	
server.close()
