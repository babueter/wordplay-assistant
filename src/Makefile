#### Global Variables ####
CC	= gcc
#CFLAGS	= -W -Wall -O2 -pipe
CFLAGS  = -g -W -Wall -O0 -pipe -fPIC
LIBS	= -lc -lgcc
LINK	= gcc
LFLAGS	= 
INCLUDE	= -I./include
LIBS_DIR = ./libs

#### Targets ####
SHARED_OBJECTS	= dawg.o tiles.o board.o wordplay.o
SHARED_TARGET	= libs/libassistant.so

ASS_OBJECTS	= ${SHARED_OBJECTS} jduplicate.o assistant.o
ASS_TARGET	= assistant

GADDAG_OBJECTS	= dawg.o makegaddag.o
GADDAG_TARGET	= makegaddag

DAWG_OBJECTS    = dawg.o makedawg.o
DAWG_TARGET     = makedawg

TARGETS		= ${DAWG_TARGET} $(GADDAG_TARGET) ${SHARED_TARGET}

#### Implicit rules ####
.SUFFIXES: .o .c
.c.o:
	$(CC) -c $(CFLAGS) $(INCLUDE) -o $@ $<

#### Explicit rules ####
all: $(TARGETS)

$(ASS_TARGET): $(ASS_OBJECTS)
	$(LINK) -o $(ASS_TARGET) $(LFLAGS) $(ASS_OBJECTS) $(LIBS)

$(GADDAG_TARGET): $(GADDAG_OBJECTS)
	$(LINK) -o $(GADDAG_TARGET) $(LFLAGS) $(GADDAG_OBJECTS) $(LIBS)

$(DAWG_TARGET): $(DAWG_OBJECTS)
	$(LINK) -o $(DAWG_TARGET) $(LFLAGS) $(DAWG_OBJECTS) $(LIBS)

$(LIBS_DIR):
	mkdir $@

$(SHARED_TARGET): $(SHARED_OBJECTS) $(LIBS_DIR)
	$(LINK) -o $(SHARED_TARGET) -shared $(LFLAGS) $(SHARED_OBJECTS) $(LIBS)

clean:
	rm -f *.o libs/*.so $(GADDAG_TARGET) $(DAWG_TARGET) $(ASS_TARGET)

