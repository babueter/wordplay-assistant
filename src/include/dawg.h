/*
*/
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>

#include <assistant.h>

/* Prevent double loading */
#ifndef FILE_DAWG
#define FILE_DAWG

/* Node_Construct is only used in the creation of the object */
typedef struct node_construct {
	struct node_construct *next_sibling;
	struct node_construct *child;
	char value;
	unsigned int uid;
	unsigned int next_sibling_uid;
	unsigned int child_uid;
	int depth;
	boolean terminal;
} Node_Construct;

/* This is the object used when loading from disk */
typedef struct node {
	struct node *next_sibling;
	struct node *child;
	char value;
	boolean terminal;
} Node;

/* Create a new empty node */
extern Node_Construct*   dawg_new_child(char);

/* Add an entire string to the structure */
extern void    dawg_add(Node_Construct*, char*);

/* For debugging purposes only */
extern void    dawg_print(Node_Construct*, int);

/* Compress the object, save to disk */
extern void    dawg_compress(Node_Construct*);
extern void    dawg_save(Node_Construct*, char*);

/* Node functions */
extern Node*   dawg_load(char*);
extern Node*   node_new(void);
extern Node*   node_has_child(Node*, char);
extern void    node_print(Node*, int);

#endif	/* FILE_DAWG */
