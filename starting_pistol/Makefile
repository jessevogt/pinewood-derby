LDFLAGS=-L/usr/local/opt/ncurses/lib
CFLAGS=-I/usr/local/opt/ncurses/include
LIBS=-lncurses

all: starting_pistol

starting_pistol: main.c
	gcc -pedantic-errors -Wall -Wextra -O3 $(LDFLAGS) $(CFLAGS) $(LIBS) main.c -o starting_pistol
