package package0;
import java.util.*;
import java.io.*;

//import java.awt.Point;
import static java.lang.System.out;

/**
 * 10/18/2014
 * Dalton Altstaetter DEA528
 * EE360C Algorithms Program 2
 * Dr. Pedro Santacruz
 */

// Begin class TestMain
public class TestMain 
{
	public static void main(String[] args)
	{
		final int ELEMENTS_PER_TRACE = 3;
		final int TX_DEV = 0;
		final int RX_DEV = 1;
		final int TIME = 2;
		File file = new File(args[0]);
		
		Scanner scanner = null;
		try {scanner = new Scanner(file);} 
		catch (FileNotFoundException e) {e.printStackTrace();}
		
		int numDev = scanner.nextInt();
		int numTraces = scanner.nextInt();
		
		int[][] traces = new int[numTraces][ELEMENTS_PER_TRACE];
		for(int i = 0; i < numTraces; i++)
		{
			int devTx = scanner.nextInt();
			int devRx = scanner.nextInt();
			int time = scanner.nextInt();
			
			traces[i][TX_DEV] = devTx;
			traces[i][RX_DEV] = devRx;
			traces[i][TIME] = time;
		}
		
		// Given a set of traces, we denote a query in the form of four non-negative integers 
		// separated by a single white-space: `i j ts tu'. This query encodes the question: 
		// can a message sent by Ui at time ts be delivered to device Uj by time tu.
		int targetTx = scanner.nextInt(); // device to start from
		int targetRx = scanner.nextInt(); // device to end on
		int targetTxTime = scanner.nextInt(); // lower range of time
		int targetRxTime = scanner.nextInt(); // upper range of time
		
		/*
		 * WHAT IF I ONLY ADDED THE TRACES TO THE ADJACENCY LIST IN WHICH
		 * HAD VALID TIMES FROM WHICH YOU COULD FOLLOW A PATH THROUGH THE
		 * ADJACENY LIST. THIS IS ACTUALLY WHAT I ENDED UP DOING BTW!!!!!
		 */
		
		Map<Integer, LinkedList<Trace>> adj = new HashMap<Integer, LinkedList<Trace>>();
		
		// add all devices to the hashMap as keys
		// keys will hold values that are adjacent to the key value in the Graph
		for(int i = 0; i < numDev; i++)
		{	adj.put(i, new LinkedList<Trace>());	}
		
		for(int j = 0; j < numTraces; j++)
		{
			Trace devNumAndTime;
			// Adds the TX device to the RX device's list at time TIME
			if( adj.containsKey(traces[j][RX_DEV]) )
			{
				// this only adds it to the list if it relevant to the comm. time range targetTxTime to targetRxTime inclusive
				if((traces[j][TIME] >= targetTxTime) && (traces[j][TIME] <= targetRxTime))
				{
					devNumAndTime = new Trace(traces[j][TX_DEV], traces[j][TIME]);
					adj.get(traces[j][RX_DEV]).add(devNumAndTime);
				}	
			}
			
			// Adds the RX device to the TX device's list at time TIME			
			if( adj.containsKey(traces[j][TX_DEV]) )
			{
				// this only adds it to the list if it relevant to the comm. time range targetTxTime to targetRxTime inclusive
				if((traces[j][TIME] >= targetTxTime) && (traces[j][TIME] <= targetRxTime))
				{
					devNumAndTime = new Trace(traces[j][RX_DEV], traces[j][TIME]);
					adj.get(traces[j][TX_DEV]).add(devNumAndTime);
				}
			}
		}
			
		// Need to start at the minimum time that is <= targetRxTime and >= targetTxTime 
		int currTime = 0;
		for(int k = 0; k < adj.get(targetTx).size(); k++)
		{	// This loop just gets the first valid time value
			if((adj.get(targetTx).get(k).time <= targetRxTime) && (adj.get(targetTx).get(k).time >= targetTxTime))
			{
				currTime = adj.get(targetTx).get(k).time;
				break;
			}
			else if( k == adj.get(targetTx).size()-1)
			{ // didn't find a match output a zero and terminate the program
				out.println("0");
				return; // THE PROGRAM ENDS HERE IF THERE IS AN INVALID INITIAL INPUT TIME
			}
		}
		
		// This loop grabs the smallest valid time data from where to start in my adj. list
		for(int i = 0; i < adj.get(targetTx).size(); i++)
		{	
			if((adj.get(targetTx).get(i).time <= targetRxTime) && (adj.get(targetTx).get(i).time >= targetTxTime))
			{
				int tempMin = adj.get(targetTx).get(i).time; 
				currTime = (tempMin < currTime) ? tempMin : currTime;
			}	
		}
		
		// At this point I should have the min valid start time and   
		// the index of where that is stored in my Hashmap/adjList
		int currentKey = targetTx;
		
		LinkedList<Trace> outputList = new LinkedList<>();
		while(currTime <= targetRxTime)
		{
			if(currentKey == targetRx)
			{   // There is a path to targetRx print to stdout and terminate program
				out.println(outputList.size());
				for(int q = 0; q < outputList.size(); q++)
				{	out.println(outputList.get(q).toString().replaceAll("[^0-9\\s^0-9\\^0-9]", ""));	}
				break;
			}
			else // I need to keep searching through my adjacency list
			{
				try // the exceptions remove paths and leaves only the path
				{   // that leads to the correct targetRx or else the expections
				    // lead to a dead end in which the exceptions see that no path
				    // can reach the targetRx device given our input parameters
					int nextDev; // If I went backwards in time then...
					if (currTime > adj.get(currentKey).get(0).time)
					{ // ...remove it from the node anyway and recover the previous state
						nextDev = adj.get(currentKey).get(0).device;
						currTime = adj.get(currentKey).get(0).time;
						
						int indexToRemoveFromCurrentNode = getIndex(adj, nextDev, currTime, currentKey);
						int indexToRemoveFromNextNode = getIndex(adj, currentKey,currTime, nextDev);
						
						adj.get(currentKey).remove(indexToRemoveFromCurrentNode);
						adj.get(nextDev).remove(indexToRemoveFromNextNode);
						// I should also check that there is another element to pull time & dev data from
						throw new BackInTimeException();
					}
					
					// The OutOfBoundsException will occur here if there are no traces left on this path
					// This also means that I didn't go down the right path since currentKey != targetRx
					nextDev = adj.get(currentKey).get(0).device; 
					currTime = adj.get(currentKey).get(0).time; // update currTime from new device
					
					int indexToRemoveFromCurrentNode = getIndex(adj, nextDev, currTime, currentKey); // finds index to remove from the current node's adjacency list
					int indexToRemoveFromNextNode = getIndex(adj, currentKey,currTime, nextDev); // finds index to remove from the next node's adjacency list
					
					outputList.add(new Trace(currentKey, adj.get(currentKey).remove(indexToRemoveFromCurrentNode))); // removes first element which is how we go through them & adds it to the output List
					adj.get(nextDev).remove(indexToRemoveFromNextNode); // removes the currentNode from the node we are going to. this avoids a loop.
					
					currentKey = nextDev;
				}
				catch(BackInTimeException e0) 
				{   // need to recover previous valid state on main path
					// need to go back one by one checking if the node is empty
					// if not take the path, if it is continue moving backwards

					int[] prevTrace = outputList.getLast().getPrivateVars(); // recover last good trace
					adj.get(currentKey).addFirst(new Trace(prevTrace[TX_DEV],prevTrace[TIME])); // add that trace back to the adj list for the RX_DEV
					currentKey = prevTrace[TX_DEV];
					currTime = prevTrace[TIME];
					adj.get(currentKey).addFirst(new Trace(prevTrace[RX_DEV],prevTrace[TIME]));// add that trace back to the adj list for the TX_DEV
					outputList.removeLast(); // remove the output we appended since it led to a dead end
				}
				catch(IndexOutOfBoundsException e1)
				{	// this occurs if there was no path to the targetRX from the path we took, allows us
					// to restore the list to the previous known point with this branch removed from it.
					if(outputList.size() == 0)
					{ // It can't be reached, terminate program
						out.println("0"); 
						break;
					}
					// This continues to remove values from our output list
					// when outputlist.size() == 0 then that means there is no
					// path to targetRx given our targetTx and (targetRxTime, targetRxTime) 
					int[] prevTrace = outputList.getLast().getPrivateVars();
					currentKey = prevTrace[TX_DEV]; // this continues to move backwards in our adj list till we get back to a node that has a path to travel
					currTime = prevTrace[TIME]; // this reverts our time back to our previous state where we have paths left to travel
					outputList.removeLast(); // removes the incorrect trace from the outputList and restores us one step higher to check for valid state
				}
			}
		}
	}	
//------------------------------------------------------------------------------------------------------//	
	/**
	 * This gets the linked list index (of a particular key value we choose)
	 * that matches the devNumber and time we want. This is essentially a 
	 * a compareTo that grabs the index of when compareTo is true
	 * This returns -1 if it fails and my program will crash if that happens
	 * 
	 * @param hm
	 * @param dev
	 * @param time
	 * @param key
	 * @return
	 */
	public static int getIndex(Map<Integer,LinkedList<Trace>> hm, int dev, int time, int key)
	{
		for(int index = 0; index < hm.get(key).size(); index++)
		{
			if( (hm.get(key).get(index).device == dev) && (hm.get(key).get(index).time == time) ) 
			{	return index;	}	
		}
		
		return -1;	
	}
}
//End class TestMain
//------------------------------------------------------------------------------------------------------//	
class BackInTimeException extends Exception
{ // this exception occurs we access an element in the 
  // linked list that is at a previous time than currTime
	BackInTimeException()
	{}
}
