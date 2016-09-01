This is my solution to the RGM Advisors Order Book Programming Problem. 

 + To compile and run the program run the following on a unix command line: **Note that a JDK installation is required.
    cd bin/
    /bin/bash compile_source
    /bin/bash run

    or 

    cd bin/
    /bin/bash compile_source
    cd ../src/
    cat $TEST_INPUT_FILE | java Pricer

 + javadoc can be found in doc/package-summary.html

 + Unit testing is in test/



About the design:
1. How did you choose your implementation language? 
I chose Java due to my proficiency with the language and the convenience of Java Collections for implementing hash tables and Red-Black trees. 
A C++ implementation likely would've been faster, but would've required more care with garbage collection and the implementation time
would've been extended.

2. What is the time complexity for processing an Add Order message?
Processing an Add Order requires worse case O(nlog(n)). This is the case when the entire tree must be traversed before the target
number of shares can be satisfied. On average the runtime is much closer to O(log(n)) where target number of shares is less than the
number of shares in the log book.

I considered two possible implementations for the order log books in the Buyer and Seller classes. 
    - java.util.PriorityQueue (heap): 
            * Pros:
                first element can be accessed in O(1)
            * Cons:
                Removing elements requires O(n)
    - java.util.TreeMap (Red-Black Tree)
            * Pros:
                Elements can be added and removed in O(log(n))
            * Cons:
                cannot access the first element in constant time
                
In the special case where the target number of shares is 1, then PriorityQueue would likely outperform a TreeMap. However, in the 
general case I assumed the target number of shares will be much smaller than the size of the log, but larger than 1. In this case,
the TreeMap yields better performance. 

I also maintain a max/min price_paid_per_share, which allows me to achieve O(log(n)) runtime when the new Order will not affect
my max/min price to sell/buy. 

3. What is the the time complexity for processing a Reduce Order message?
Reduce Orders are processed in worse case O(n*log(n)). Again, the average case is closer to O(log(n)) when the target number of
shares is much less that the number of shares in the log book. Processing a Reduce Order also requires a O(1) lookup in a hashMap to
associate the order_id with the price. 

4. If your implementation were put into production and found to be too slow, what ideas would you try out to improve its performance? (Other than reimplementing it in a different language such as C or C++.)

In general, I would start with algorithmic enhancements to achieve better big O runtime, then I would consider parallelization, next
the IO, then the data type representations, and finally the memory footprint. Below are some ideas:
 - Modify the Java TreeMap implementation to maintain a cost for the target shares upon element
   insertion/removal. This would allow consistent O(log(n)) runtime for addition and removal of Orders.
 - Thread the application. Two different levels are possible:
    * Create a Pricer thread for each instrument at the top level. 
    * Thread the Buyer and Seller instances so that buy and sell orders can be processed in parallel.
 - Consider the IO. Could the order messages could be accessed in a more efficient format apart from text on the command
   line? Say through an API where they were available as a Java object. This would save the overhead of string creation and
   processing using regular expressions.
 - Consider the variables and their datatypes. Start with those accessed and created most frequently. Am I using the most efficient
   representation for each one? Is there an Integer that could be replaced with a primitive int? A String that could be replaced
   by a char or char[] or byte?
 - Consider the system that is running the application. Does it have a cache size large enough for the log? Is it
   frequently accessing RAM or the disk during Add/Remove Order processing? If so, then increase the size of the cache or the RAM.
