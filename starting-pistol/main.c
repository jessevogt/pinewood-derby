#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/time.h>

#include <stdio.h>
#include <stdlib.h>

#include <string.h>
#include <errno.h>
#include <unistd.h>

/*
http://www.paulgriffiths.net/program/c/srcs/echoservsrc.html
http://beej.us/net2/html/syscalls.html
*/

#define MESSAGE "BANG!"
#define MESSAGE_LEN sizeof(MESSAGE)

#define DIE_WITH_ERROR(msg) { printf(msg": (%d) %s\n", errno, strerror(errno)); exit(EXIT_FAILURE); }

int start_server(const char *server_ip, unsigned short server_port);
int start_client(const char *server_ip, unsigned short server_port);

void setup(const char *ip, unsigned short port, int *socket_descriptor, struct sockaddr_in *socket_address);

int main(int argc, char **argv) {
	if (argc >= 3) {
		if (strcmp("client", argv[1]) == 0) {
			return start_client(argv[2], 9999);
		} else if (strcmp("server", argv[1]) == 0) {
			return start_server(argv[2], 9999);
		}
	}

	return EXIT_FAILURE;
}

void setup(const char *ip, unsigned short port, int *socket_descriptor, struct sockaddr_in *socket_address) {
	printf("setting up for socket and bind\n");

	if ((*socket_descriptor = socket(AF_INET, SOCK_STREAM, 0)) < 0)
		DIE_WITH_ERROR("error calling socket");

	printf("socket success. socket descriptor: %d\n", *socket_descriptor);

	memset(socket_address, 0, sizeof(*socket_address));
	socket_address->sin_family = AF_INET;
	socket_address->sin_addr.s_addr = ip == NULL ? htonl(INADDR_ANY) : inet_addr(ip);
	socket_address->sin_port = htons(port);

	if (bind(*socket_descriptor, (struct sockaddr *)socket_address, sizeof(*socket_address)) < 0)
		DIE_WITH_ERROR("error calling bind");

	printf("bind success\n");
}

int start_server(const char *listen_address, unsigned short listen_port) {
	int server_socket_descriptor;
	int client_socket_descriptor;
	struct sockaddr_in server_address;
	const char buffer[MESSAGE_LEN];
	struct timeval message_received_ts;

	printf("attempting server start on %s:%d\n", listen_address, listen_port);

	setup(listen_address, listen_port, &server_socket_descriptor, &server_address);

	if (listen(server_socket_descriptor, 1024) < 0)
		DIE_WITH_ERROR("error calling listen");

	printf("listen success\n");

	while (1) {
		memset((void*)buffer, 0, MESSAGE_LEN);
		memset(&message_received_ts, 0, sizeof(message_received_ts));

		if ((client_socket_descriptor = accept(server_socket_descriptor, NULL, NULL)) < 0)
			DIE_WITH_ERROR("error calling accept");

		gettimeofday(&message_received_ts, NULL);
		recv(client_socket_descriptor, (void *)buffer, MESSAGE_LEN, 0);

		if (strncmp(buffer, MESSAGE, MESSAGE_LEN) == 0) {
			printf("%ld\n", message_received_ts.tv_sec * 1000 + message_received_ts.tv_usec / 1000);
		}

		if (close(client_socket_descriptor) < 0)
			DIE_WITH_ERROR("error calling close");
	}

	return EXIT_SUCCESS;
}

int start_client(const char *server_ip, unsigned short server_port) {
	int client_socket_descriptor;
	struct sockaddr_in client_address;
	struct sockaddr_in server_address;

	setup(NULL, 0, &client_socket_descriptor, &client_address);

	memset(&server_address, 0, sizeof(server_address));
	server_address.sin_family = AF_INET;
	server_address.sin_addr.s_addr = inet_addr(server_ip);
	server_address.sin_port = htons(server_port);

	printf("attempting connect to %s:%d\n", server_ip, server_port);

	if (connect(client_socket_descriptor, (struct sockaddr *)&server_address, sizeof(server_address)) < 0)
		DIE_WITH_ERROR("error when attempting to connect to server");

	send(client_socket_descriptor, MESSAGE, MESSAGE_LEN, 0);

	return EXIT_SUCCESS;
}
