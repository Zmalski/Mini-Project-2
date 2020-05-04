package pkg;

import java.util.Scanner;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.lang.*;


public class Main {
	static int N;
	static int M;
	/*first creates a new linked list with <Request> data type
	 * and then the list called queue is a synchronized list that holds the
	 * linked list within that in order to prevent concurrency errors */
	static List<Request> ll = new LinkedList<Request>();
	static List<Request> queue = Collections.synchronizedList(ll);
	 /*1. Type: SlaveThread[]
	 * - To hold all the slaves in one object to instantiate them
	 * 2. Type: Thread[]
	 * - Creates the runnable slave instances 
	 * Global objects so they can be accessed from multiple methods (createSlaves & startSlaves) 
	 * */
	static SlaveThread[] slaves;
	static Thread[] threads;
	 public static Object lock1 = new Object();
	public static void main(String[] args) throws InterruptedException {
		getInput();//prompts for input
		createSlaves();//starts all slaves and returns back 
		generateRequests(); //Master
		
	}
	/*
	 * This function creates two object arrays
	 * 1. Type: SlaveThread[]
	 * - To hold all the slaves in one object to instantiate them
	 * 2. Type: Thread[]
	 * - Creates the runnable slave instances 
	 * returns program flow back to main 
	 * */
	public static void createSlaves() throws InterruptedException {
			slaves = new SlaveThread[N];
		for(int i=0; i< N; i++) {
			slaves[i] = new SlaveThread();
		}
		threads = new Thread[N];
		for(int i=0; i <N; i++) {
			threads[i] = new Thread(slaves[i]);
		}
		return;
	}

	/*
	 * This is the "Master Thread" generating Requests 
	 * called from main, it runs  do..while(true) structure
	 * it creates a request, checks the queue, then decides what function to call
	 *  
	 * */
	private static void generateRequests() throws InterruptedException  {
	
		int jobLength;
		int jobID = 0;
		startSlaves();
		do {
			jobID++;
			jobLength = new Random().nextInt((M+1) - 1) + 1; //random number with 1 < value < M 
			Request request = new Request(jobID, jobLength);
			System.out.println("New Request arrived, ID: " + request.getId() + " Job length: " + request.getLengthReq());
			
			if (checkQueue()) 
				add(request);
			else
			wait(request);
		
			
			Thread.sleep((new Random().nextInt((4+1) - 1) + 1)*1000); //puts the master to sleep after checking queue
		}
		while(true);
		
	}
	private static void startSlaves() throws InterruptedException {
		for (int i=0; i< N; i++) {
			threads[i].start();
		}
		
	}
	//this function will cause main to sleep, check queue again, then add the request and then sleep again
	private static void wait(Request r) throws InterruptedException {
//it will keep on sleeping for 0.5 seconds until the queue is finally free
		do {
			System.out.println("Queue is full, "+Thread.currentThread().getName()+"(Master) is holding request ID: "+ r.getId());
			Thread.sleep(500);
		}
		while (checkQueue()==false);//do this while queue is still full
		System.out.println("Queue has available space, " + Thread.currentThread().getName() + "(Master) is adding request ID: " + r.getId());
		add(r);//if condition is false then go to add the request to queue
		
	}

	synchronized private static void add(Request r) {
			queue.add(r);
		System.out.println(Thread.currentThread().getName()+"(Master) has added ID: " + r.getId());
		return;
	}

	synchronized public static boolean checkQueue () {
		return queue.size() < N? true : false;
	}

//This function is called first from main to retrieve M and N, and performs some basic data validation 
//Will prompt repeatedly if M or N inputed is less then 0 
	public static void getInput() {
		Scanner in = new Scanner(System.in);
		System.out.println("Info: \nN = The number of slave threads and size of input buffer (POSITIVE INT). M = Max seconds of any one request can take (POSITIVE INT)");
		
		do {
			System.out.println("Please enter N");
			N = in.nextInt();
			
		}
		while (N < 0);
		
		do {
		System.out.println("Please enter M");
		M = in.nextInt();
			}
		while(M < 0);
		
		}
	public int getN() {
		return this.N;
	}

}
