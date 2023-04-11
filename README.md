# Covid-safe-restaurant-semaphore
Covid safe restaurant using semaphore
Class Name: P2 (Restaurent named P2)
 Description: P2 class reads the customer data from input file and arranges for Customer and Seat Semaphores to run  as per the assignment rules.
 Precondition: Input file with customer data is provided at the run time.  First we create the semaphores.As there are no customers and the restaurent is empty so we call the constructor with parameter 0 thus creating semaphores with zero initial permits.
 Semaphore(1) constructs a binary semaphore. This semaphore is used to regulate the access of customer thread between Customer and Serve 
 Postcondition: Restaurent serves customers as per the assignment specification
