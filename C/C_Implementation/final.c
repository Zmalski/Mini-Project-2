#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <sys/wait.h>
#include <time.h>
#include <pthread.h>
#include <semaphore.h>

#define TRUE 1
#define FALSE 0
sem_t mutex; // Semaphore to be used for mutual exclusion

struct request { // Structure containing all info on a request
    int id; // id = -1 if request does not exist
    int length;
};
struct request *queue; // Initialize global list of requests

struct args { // Structure to hold arguments for thread function
    int size;
};

struct request pop(int size){
    struct request returnItem = queue[0]; // Save the first item
    for(int i = 0; i < size;  i++){ // Move every item in queue forward one place
        queue[i] = queue[i+1];
        if(queue[i].id == -1) // If next item is "empty"
            return returnItem; // Stop loop and return the first item
    }
    return returnItem; //return the first item
}

int push(struct request item, int size){
    for (int i = 0; i < size; i++){ // Iterate through request queue from beginning->end
        if (queue[i].id == -1){
            queue[i] = item; // Set first item that is "empty" to the item to be inserted
            return TRUE; // Return true if item was succesfully inserted
        }
    }
    return FALSE; // Return false if item was not inserted because queue was full
}

void init (int size){ //Initialize a queue of requests to default values
    for(int i = 0; i < size; i++){
        queue[i].id = -1;
        queue[i].length = 0;
    }
}

void *consumer(void * tArgs) { // Function to be run by each thread
    struct request item;
    int size = ((struct args*)tArgs)->size; // Grab size parameter from the argument structure
    while(1){ // Loop for handling and processing requests
        int wait = 1;
        sem_wait(&mutex); // Lock semaphore
        while(wait == 1){ // Keep trying to process requests if queue is empty
            item = pop(size); // pop request off queue
            if (item.id != -1) // If request is empty, loop again
                wait = 0;
            else {
                sleep(0.1); // sleep for a short amount of time if queue is empty
            }
        }
        sem_post(&mutex); // Release semaphore
        time_t rawTime = time(NULL); // Create time_t variable to extract current time from
        struct tm  *localTime = localtime(&rawTime); // Convert time_t to a time struct
        printf("Consumer: Assigned request ID %d, processing request for the next %d seconds, current time is %02d:%02d:%02d\n", item.id, item.length, localTime->tm_hour, 
           localTime->tm_min, localTime->tm_sec);
        fflush(stdout);
        sleep(item.length); // "Process" the request
        rawTime = time(NULL); // Create time_t variable to extract current time from
        localTime = localtime(&rawTime); // Convert time_t to a time struct
        printf("Consumer: Completed request id %d at time %02d:%02d:%02d\n", item.id, localTime->tm_hour, 
           localTime->tm_min, localTime->tm_sec);
    }
    pthread_exit(NULL);
}

int main() {
    int N; // Number of consumers/queue length
    int M; // Request length/producer wait length
    printf("Enter N: ");
    scanf("%d", &N);
    printf("Enter M: ");
    scanf("%d", &M);
    queue = malloc(N * sizeof(struct request)); // Allocate memory for global queue variable based on user input
    printf("Initializing...\n");
    fflush(stdout);
    sem_init(&mutex, 0, 1); // Initialize semaphore for threads
    time_t t; // Time to be used for random number seed
    int requestId = 0;
    int requestLength = 0;
    pthread_t list[N]; // A list of identifiers for each unique thread
    init(N); // Initialize queue to a list of requests
    struct args *tArgs = malloc(sizeof(struct args)); // Create argument structure to send through to threads
    tArgs->size = N; // Set size in arguments structure for threads to reference

    for(int i = 0; i < N; i++){ // Loop to create all threads
        pthread_create(&list[i], NULL, consumer, (void *) tArgs);
    }
    
    srand((unsigned) time(&t)); // Seed random number generator
    while (1) { // Generate requests
        int randLength = (rand() % M) + 1; // Generate random length for request
        requestLength = randLength;
        struct request newRequest; // Create request to add to queue
        newRequest.id = requestId;
        newRequest.length = requestLength;
        int pushSuccess = 0;
        while(pushSuccess == 0){ // Keep trying to add request if queue is full
            pushSuccess = push(newRequest, N); // Add request to queue
            if (pushSuccess == 0)
                printf("Producer: Queue is full.  Holding to to request ID %d", requestId);
        }

        time_t rawTime = time(NULL); // Create time_t variable to extract current time from
        struct tm  *localTime = localtime(&rawTime); // Convert time_t to a time struct
        printf("Producer: Produced request ID %d, length %d seconds at %02d:%02d:%02d\n", requestId, requestLength, localTime->tm_hour, 
           localTime->tm_min, localTime->tm_sec); // Print update message

        randLength = (rand() % M) + 1; // Generate random length to sleep for
        printf("Producer: Sleeping for %d seconds. \n", randLength);
        sleep(randLength);



        requestId++;
    }
    printf("Done.");
    free(queue);
    free(tArgs);
    exit(0);
}
