
/*
 Class Name: P2 (Restaurant named P2)
 Description: P2 class reads the customer data from input file and arranges for Customer and Seat Semaphores to run
 as per the assignment rules.
 Precondition: Input file with customer data is provided at the run time.
 First we create the semaphores.As there are no customers and the restaurant is empty, so we call the constructor with parameter 0 thus creating semaphores with zero initial permits.
 Semaphore(1) constructs a binary semaphore. This semaphore is used to regulate the access of customer thread between
 Customer and Serve Class
 Postcondition: Restaurant serves customers as per the assignment specification
 */
import java.util.*;
import java.io.*;
import java.util.concurrent.*;

public class P2 extends Thread {
    public static Semaphore customers = new Semaphore(0);
    public static Semaphore seat = new Semaphore(0);
    public static Semaphore accessSeats = new Semaphore(1);

    public static final int CHAIRS = 5;     // we denote that the number of chairs in this restaurant is 5.
    public static final int cleaningTime=5; //cleaning time taken by the restaurant
    public static int lastLeavingTime=0;    //Time when the last customer from the batch leaves

    /* we create the integer iD which is a unique ID number for every customer
     and a boolean notBatch which is used in the Customer waiting loop */
    public static int iD=0;
    boolean nextBatch=false;

    public static int temp=0;   //Stores the value of leaving time to find the time when the last customer from the batch leaves
    int custBatch=0;            //Batch of customers entering restaurant
    /* we create the integer numberOfFreeSeats so that the customers can either sit on a free seat or queue up if there
     are no seats available */
    public static int numberOfFreeSeats = CHAIRS;

    /* THE CUSTOMER THREAD */
    class Customer extends Thread {

        boolean notEating=true;                    //Checks if the customer is already served the order
        int arrivalTime, eatingTime, leaves,seats; //Variables to store customer details
        String custNum;                            //Stores customer number

        /* Constructor for the Customer */
        public Customer(int at, int et, String cs) {
            arrivalTime = at;       				//Customer arrival time
            eatingTime = et;        				//Time taken to enjoy the meals
            custNum = cs;           				//Customer number
            seats=0;                				//Seating time, computed based on the assignment rules
            leaves=0;               				//Time when the customer leaves the restaurant,computed based on the assignment rules
        }

        public void run() {
            while (notEating) {                 	// as long as the customer is not eating
                try {
                    accessSeats.acquire();        	//tries to get access to the chairs
                    if (numberOfFreeSeats > 0) {  	//if there are any free seats
                        numberOfFreeSeats--;      	//sitting down on a chair
                        getLeavingTime();         	//calculates the seating time and leaving time of the customer
                        iD++;                     	//Customer count for the day
                        customers.release();      	//notify the restaurant that there is a customer
                        this.eating();            	//eating...
                        accessSeats.release();    	// don't need to lock the chair for customer anymore as the customer has left
                        try {
                            seat.acquire();       	// now it's this customers turn, but we have to wait if the restaurant is busy
                            notEating = false;    	// this customer will now leave after the procedure
                        } catch (InterruptedException ex) {}
                    }
                    else  {                     	// there are no free seats
                        //System.out.println("There are no free seats. Customer " + custNum + " has to wait.");//Debug code
                        accessSeats.release();  	//release the lock on the seats
                        notEating=false; 			// the customer will leave since there are no spots in the queue left.
                    }
                }
                catch (InterruptedException ex) {}
            }
        }
        /* this method calculates the customer seating time and leaving time from the restaurant */
        public void getLeavingTime(){
            seats=arrivalTime;
            leaves= eatingTime + seats;
            if(nextBatch){								//boolean value to check if the next batch has arrived
                lastLeavingTime=temp;					//transfer the time of customer who left last from the restaurant into a temp variable
                nextBatch=false;							//change the value to false until the following batch arrives
            }
            if (iD>=custBatch){							//checks if the customer is from same batch or next batch
                if(arrivalTime < lastLeavingTime){ 		//checks if the customer has been waiting in the queue
                    seats=lastLeavingTime+cleaningTime;	//seating time of the customer will be post the cleaning performed -
                    leaves= eatingTime + seats;			//after the last customer from previous batch has left
                }
            }
            if (temp < leaves)
                temp=leaves; 		//temp variable to store the value of leaving time of the current batch customer who left at the last
        }

        /* this method will simulate customer is eating in the restaurant and prints the customer details */
        public void eating(){
            System.out.println(custNum + "\t\t"+"  " + arrivalTime +"\t\t"+"  "  + seats+"       " + leaves  );
            try {
                sleep(100);                   //adds time delay else the output scrolls too fast
            } catch (InterruptedException ex) {}
        }

    }

    /* THE Seat THREAD */
    class Seats extends Thread {

        public Seats() {}

        public void run() {
            while(true) {                       // runs in an infinite loop
                try {
                    customers.acquire();              // tries to acquire a customer - if none is available he goes to sleep
                    accessSeats.release();            // at this time he has been awakened -> want to modify the number of available seats
                    if(numberOfFreeSeats==0){
                        seat.release();             // the restaurant is ready to serve
                        accessSeats.release();      // we don't need the lock on the chairs anymore
                        this.serveNextBatch();      // preparing to serve the next batch...
                    }
                } catch (InterruptedException ex) {}
            }
        }

        /* this method will simulate serving to next batch of customer */
        public void serveNextBatch(){
            //System.out.println("The restaurant is ready to serve next batch of customer");//Debug code
            numberOfFreeSeats=CHAIRS;               // All chairs are free
            custBatch=custBatch+numberOfFreeSeats;  //Customer batch number
            nextBatch=true;                         //Indicates that the next batch of customers arriving/waiting in the queue can enter
            try {
                sleep(100);
            } catch (InterruptedException ex){ }
        }
    }

    /* main method */
    public static void main(String args[]) {
        Queue<String> queue = new LinkedList<>();   //Queue object to store the data from the file in FIFO order
        String myStr;                               //String to hold each line of the file until the last line is read
        int i=0;                                    //counter to check the number of lines in the file
        String inFile=" " ;
        P2 p2 = new P2();                            //Creates a new restaurant
        System.out.print('\u000C');                  //Clears screen
        if (args.length==0) {                        //Checks if a file name is given by the user at the run time
            System.out.println("no arguments were given");
        }
        else {
            inFile=args[0];
        }

        try{FileReader file= new FileReader(inFile);
            BufferedReader br = new BufferedReader(file);//Read the file

            while((myStr = br.readLine()) != null){    //Read the file line by line
                queue.add(myStr);                       //Transfers the data into the queue
                i++;                                    //Moves to the next customer details,until the last customer details are read
            }
        } catch (IOException e){
            System.out.println(" Error opening the file " + inFile);//file exception handling
            System.exit(0); }
        System.out.println("Customer" + "\t" + "Arrival Time" + "\t" + "Seats" + "\t" + " Leaves" ); //Prints the header line
        p2.begin(i,queue);                         // Transfers the queue data to customer class and begins the simulation
    }

    /* Transfers the queue data to customer class and begins the simulation */
    public void begin(int i,Queue<String> q){
        //Creates an object of seat
        Seats s1 = new Seats();
        s1.start();                			 //Ready for another day of work
        int at[]=new int[i];       			 //creates the arrival time array of size = number of lines in the queue or input file
        int et[]=new int[i];       			 //creates the eating time array of size = number of lines in the queue or input file
        String cs[]=new String[i]; 			 //creates the customer array of size = number of lines in the queue or input file
        for(int n=0;n<(i-1);n++)
        {
            String head = q.peek();              //Transfer the queue head to string variable
            String[] myData= head.split("\\s+"); //split the string to extract the value of arrival time, cust num, eating time
            at[n]= (Integer.valueOf(myData[0]));
            cs[n]= myData[1];
            et[n]= (Integer.valueOf(myData[2]));
            q.remove();                          // Removes the data line already transferred to the array from the queue
        }
        createCustomer(at,et,cs,i);             //method to create customer with details and start the customer thread
    }

    /* This method will pass the customer details to customer class and starts the customer thread */
    public void createCustomer(int at[],int et[],String cs[],int i){
        for (int j=0; j<(i-1); j++) {
            Customer aCustomer = new Customer(at[j],et[j],cs[j]);   //Creates the customer as it arrives
            aCustomer.start();                                      //Starts the customer thread
            try {
                sleep(1000);                                         //adds time delay else the output scrolls too fast
            } catch(InterruptedException ex) {};
        }
    }
}