/*
*/
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>

#include <dawg.h>
#include <assistant.h>

/* Used in the compression phase */
typedef struct node_list {
        Node_Construct *node;
        struct node_list *next;
} Node_List;

/* Construction functions */
extern void dawg_add_child(Node_Construct*, Node_Construct*);
extern Node_Construct* dawg_has_child(Node_Construct*, char);
extern int dawg_node_count(Node_Construct*);
extern void dawg_print_node(Node_Construct*);

/* Compression functions */
extern boolean dawg_identical_children(Node_Construct*, Node_Construct*);
extern int dawg_compress_phase(Node_Construct*, Node_List*, int);

/* Storage and retrieval functions */
extern unsigned int dawg_set_uid(Node_Construct*, unsigned int);
extern void dawg_store_nodes(Node_Construct**, Node_Construct*);


void dawg_add(Node_Construct *parent, char *word) {
/* Add the string to the DAWG structure starting with parent */
  Node_Construct *next_child;
  char first_letter = toupper(*word); word++;
  int depth = strlen(word);

 /* Add the node if it doesnt exist, update the depth if greater */
  next_child = dawg_has_child(parent, first_letter);
  if ( next_child == NULL ) {
    next_child = dawg_new_child(first_letter);
    dawg_add_child(parent, next_child);
    next_child->depth = depth;
  } else {
    if ( depth > next_child->depth ) {
      next_child->depth = depth;
    }
  }

 /* Recurse if there are still letters in the word to add, uptade the terminal value otherwise */
  if ( strlen(word) != 0 ) {
    dawg_add(next_child, word);
  } else {
    next_child->terminal = true;
  }

  if ( parent->depth < depth ) {
    parent->depth = depth+1;
  }
}

void dawg_add_child(Node_Construct *parent, Node_Construct *new_child) {
/* Add the child to the linked list */
  Node_Construct *child = parent->child;

 /* This is the first child, add him and move on */
  if ( child == NULL ) {
    parent->child = new_child;
    return;
  }

 /* The new child is alphabetically before the first node */
  if ( child->value > new_child->value ) {
    parent->child = new_child;
    new_child->next_sibling = child;

    return;
  }

 /* Walk through the linked list and find the alphabetical place in the chain*/
  Node_Construct *sibling = child->next_sibling;

  while ( sibling != NULL ) {
    if ( sibling->value == new_child->value ) {
      return;
    }
    if ( sibling->value > new_child->value ) {
      break;
    }
    child = sibling;
    sibling = child->next_sibling;
  }

 /* The new child already exists on this parent */
  if ( child->value == new_child->value ) {
    return;
  }

 /* insert the child into the list */
  new_child->next_sibling = sibling;
  child->next_sibling = new_child;

}

Node_Construct* dawg_new_child(char value) {
/* return a pointer to an initialized child */
  Node_Construct *new_child = malloc(sizeof(Node_Construct));

  new_child->next_sibling = NULL;
  new_child->child = NULL;
  new_child->value = toupper(value);
  new_child->uid = 0;
  new_child->depth = 0;
  new_child->terminal = false;

  return new_child;
}

Node_Construct* dawg_has_child(Node_Construct *parent, char value) {
/* Return child if the parent has one with the value specified */
  Node_Construct *child = parent->child;

 /* walk through the list of children */
  while ( child != NULL ) {
   /* Found a child matching the description, return the value */
    if ( child->value == value ) {
      return child;
    }
    child = child->next_sibling;
  }

 /* None of the children matched value */
  return NULL;
}

int dawg_node_count(Node_Construct *node) {
/* Recursively count all nodes in this structure */
  int count = 1;

  Node_Construct *child = node->child;
  while ( child != NULL ) {
    count += dawg_node_count(child);
    child = child->next_sibling;
  }

  return count;
}

unsigned int dawg_set_uid(Node_Construct *node, unsigned int uid) {
/* Set the uid of each node in the DAWG so we can reference it later */
  Node_Construct *child = node->child;

 /* This node will take the current uid */
  if ( ! node->uid ) {
    node->uid = uid;
  }

 /* Child nodes will take the next highest uid */
  while ( child != NULL ) {
    if ( ! child->uid ) {
      uid = dawg_set_uid(child, uid+1);
    }
    child = child->next_sibling;
  }

 /* Return the highest uid used */
  return uid;
}

void dawg_update_uids(Node_Construct *node) {
/* Update sibling and child uid pointers */
  Node_Construct *child = node->child;

 /* Update this nodes child uid pointer */
  if ( child != NULL ) {
    node->child_uid = node->child->uid;
  }

 /* Update each child uid pointers with its sibling and call it recursively */
  while ( child != NULL ) {
    if ( child->next_sibling != NULL ) {
      child->next_sibling_uid = child->next_sibling->uid;
    }
    dawg_update_uids(child);
    child = child->next_sibling;
  }
}

void dawg_store_nodes(Node_Construct **list, Node_Construct *node) {
/* Store each node in the list by uid */
  Node_Construct *child = node->child;

 /* index is the node uid */
  list[node->uid] = node;

 /* Do the same for all the children */
  while ( child != NULL ) {
    dawg_store_nodes(list, child);
    child = child->next_sibling;
  }
}

void dawg_print_node(Node_Construct *node) {
/* In case we want to print out a single node_construct */
  printf("%c (%d/%d) : (%u,%u,%u) %p\n",
         node->value, node->depth, node->terminal, node->uid, node->next_sibling_uid, node->child_uid, node);
}

void dawg_print(Node_Construct *node, int level) {
/* Recursively print the dawg structure */
  int i;
  for (i=0; i<level; i++) {
    printf("|");
  }

  dawg_print_node(node);

  Node_Construct *child = node->child;
  while (child != NULL) {
    dawg_print(child, level+1);
    child = child->next_sibling;
  }
}

boolean dawg_identical_children(Node_Construct *node, Node_Construct *candidate) {
/* Identical children point to the same node */
  Node_Construct *childA = node->child;
  Node_Construct *childB = candidate->child;

 /* Verify each child is exactly the same */
  boolean matching = true;
  while ( childA != NULL && childB != NULL && matching == true ) {
    if ( childA->value != childB->value ) {
      matching = false;
      break;
    }
    if ( childA->terminal != childB->terminal ) {
      matching = false;
      break;
    }
    if ( childA->child != childB->child ) {
      matching = false;
      break;
    }
    childA = childA->next_sibling;
    childB = childB->next_sibling;
  }

  return matching;
}

void dawg_compress(Node_Construct *node) {
/* Compress the noode by de-duplicating the suffixes.  */
/* Since this could take a long time we print status along the way */
  Node_List *list = malloc(sizeof(Node_List));
  int max_depth = node->depth;
  int i, count = 0;
  int total = dawg_node_count(node);

 /* Compression has to happen one phase at a time */
  for (i=0; i<max_depth; i++) {
    printf("Compressing phase %d\n", i);
    list->node = NULL;
    list->next = NULL;
    count += dawg_compress_phase(node, list, i);
  }
  printf("\nDone, deduplicated %d of %d nodes\n", count, total);

  free(list);
}

int dawg_compress_phase(Node_Construct *node, Node_List *list, int depth) {
/* Search for and compaire nodes at the specified depth and deduplicate if possible */
  int count = 0;

 /* If depth matches, compare children of nodes in list */
  if ( node->depth == depth ) {
    Node_List *last_node = list;
    Node_List *curr_node = list;
    boolean replaced = false;

   /* This is the first node in the list, update the list and return */
    if ( last_node->node == NULL ) {
      list->node = node;
      return count;
    }

   /* Walk through the list and compare each node */
    while ( curr_node != NULL ) {
      Node_Construct *candidate = curr_node->node;

     /* de-duplicate children if they are identical */
      if ( dawg_identical_children(node, candidate) ) {
        node->child = candidate->child;
        replaced = true;
        count++;
        break;
      }

      last_node = curr_node;
      curr_node = curr_node->next;
    }

   /* Append node to the end of the list if we didnt find a replacement */
    if ( ! replaced ) {
      Node_List *next = malloc(sizeof(Node_List));
      next->node = node;
      next->next = NULL;
      last_node->next = next;
    }

  } else {
   /* Recursively evaluate all cildren */
    Node_Construct *child = node->child;

    while ( child != NULL ) {
      count += dawg_compress_phase(child, list, depth);
      child = child->next_sibling;
    }
  }

  return count;
}

void dawg_save(Node_Construct *node, char *filename) {
/* Recursively write the dawg structure to a file*/
  FILE *fout = fopen(filename, "w+");

 /* Prepare the structure to be written to disk */
  unsigned int node_count = dawg_set_uid(node, 1);
  dawg_update_uids(node);

  Node_Construct **all_nodes = malloc(sizeof(Node_Construct*)*(node_count+1));
  dawg_store_nodes(all_nodes, node);

 /* Open file for writing */
  if ( fout == NULL ) {
    printf ("Error: unable to open file: %s\n", filename);
    exit(1);
  }

 /* Write header, which is the total count of nodes */
  if ( !fwrite(&node_count, sizeof(unsigned int), 1, fout) ) {
      printf ("Error: IO error\n");
      exit(ferror(fout));
  }

 /* Write each node in the list to disk */
  unsigned int i;
  for (i=1; i<=node_count; i++) {
    Node_Construct *tmp_node = all_nodes[i];

   /* Write the data to disk */
    if ( !fwrite(&tmp_node->value, sizeof(char), 1, fout) ) {
      printf ("Error: IO error\n");
      exit(ferror(fout));
    }
    if ( !fwrite(&tmp_node->next_sibling_uid, sizeof(unsigned int), 1, fout) ) {
      printf ("Error: IO error\n");
      exit(ferror(fout));
    }
    if ( !fwrite(&tmp_node->child_uid, sizeof(unsigned int), 1, fout) ) {
      printf ("Error: IO error\n");
      exit(ferror(fout));
    }
    if ( !fwrite(&tmp_node->terminal, sizeof(boolean), 1, fout) ) {
      printf ("Error: IO error\n");
      exit(ferror(fout));
    }
  }
  fclose(fout);

  free(all_nodes);
  return;
}

Node* dawg_load(char *filename) {
/* Read the dawg structure from a file*/
  FILE *fin = fopen(filename, "r");
  unsigned int count;

  Node *node;

 /* Open file for reading */
  if ( fin == NULL ) {
    printf ("Error: unable to open file: %s\n", filename);
    exit(1);
  }

 /* Read number of nodes */
  if ( !fread(&count, sizeof(unsigned int), 1, fin) ) {
    printf ("Error: File corrupt\n");
    exit(ferror(fin));
  }

 /* Create node list for storage of node and logical node pointers */
  Node **list = malloc(sizeof(Node*)*(count+1));
  unsigned int *next_sibling_uids = malloc(sizeof(unsigned int)*(count+1));
  unsigned int *child_uids = malloc(sizeof(unsigned int)*(count+1));

 /* Read in each node, error if count doesnt match up */
  unsigned int i;
  for (i=1; i<=count; i++) {
    Node *new = node_new();
    list[i] = new;

    if ( !fread(&new->value, sizeof(char), 1, fin) ) {
      printf ("Error: File corrupt\n");
      exit(ferror(fin));
    }
    if ( !fread(next_sibling_uids+i, sizeof(unsigned int), 1, fin) ) {
      printf ("Error: File corrupt\n");
      exit(ferror(fin));
    }
    if ( !fread(child_uids+i, sizeof(unsigned int), 1, fin) ) {
      printf ("Error: File corrupt\n");
      exit(ferror(fin));
    }
    if ( !fread(&new->terminal, sizeof(boolean), 1, fin) ) {
      printf ("Error: File corrupt\n");
      exit(ferror(fin));
    }
  }

 /* Set the child and sibling pointers */
  list[0] = NULL;
  node = list[1];
  for (i=1; i<=count; i++) {
    list[i]->next_sibling = list[ next_sibling_uids[i] ];
    list[i]->child = list[ child_uids[i] ];
  }

 /* Clean up after ourselves */
  free(list);
  free(next_sibling_uids);
  free(child_uids);

  return node;
}

Node* node_new() {
/* Create an empty node object and return it */
  Node *new = malloc(sizeof(Node));

  new->next_sibling = NULL;
  new->child = NULL;
  new->value = '@';

  return new;
}

Node* node_has_child(Node *node, char value) {
/* Return child if the parent has one with the value specified */
  Node *child = node->child;

 /* walk through the list of children */
  while ( child != NULL ) {
   /* Found a child matching the description, return the value */
    if ( child->value == value ) {
      return child;
    }
    child = child->next_sibling;
  }

 /* None of the children matched value */
  return NULL;
}

void node_print(Node *node, int level) {
/* Recursively print the dawg structure */
  int i;
  for (i=0; i<level; i++) {
    printf("|");
  }

  printf("%c (%d) : %p\n", node->value, node->terminal, node);

  Node *child = node->child;
  while (child != NULL) {
    node_print(child, level+1);
    child = child->next_sibling;
  }
}


