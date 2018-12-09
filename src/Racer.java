import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

public class Racer implements Runnable{
	private int RacerNum;
	private String MagicWord;
	private static long time = 0;
	private Random random;
	private Thread RT;	//Racer Thread
	private boolean MoveToNextStage;
	private boolean InRiver;
	private boolean Finished;
	private long TotalTime = 0;
	private long[] ObstacleTime = new long[3];
	
	
	
	public Racer(int RacerNum, String MagicWord, Vector<Thread> WaitLine, long time){
		this.RacerNum = RacerNum;
		this.MagicWord = MagicWord;
		Finished = false;
		random = new Random();
		Racer.time = time;
		RT = new Thread(this, getName());
	}
	
	public String getName(){
		return "Racer " + RacerNum;
	}
	
	public Thread getThread(){
		return RT;
	}
	
	public Boolean getInRiver(){
		return InRiver;
	}
	
	public Boolean getFinished(){
		return Finished;
	}
	public long getTotalTime(){
		return TotalTime;
	}
	
	public void getObstacleTime(){
		System.out.println("Forest Time: " + ObstacleTime[0]);
		System.out.println("Mountain Time: " + ObstacleTime[1]);
		System.out.println("River Time: " + ObstacleTime[2]);
	}
	
	public void setInRiver(Boolean value){
		InRiver = value;
	}
	
	
	
	private void msg(String m) {
		System.out.println("[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m);
	}
/**
 * Used when the racer is resting	
 * @param resttime
 */
	private void Rest(int resttime) {
		msg("is resting and eating.");
		long temp = random.nextInt(1000) + resttime;
		TotalTime += temp;
		try {
			Thread.sleep(temp);
		} catch (InterruptedException e) {
			System.out.println("Problem rest");
		}
	}
	
/**
 * Used to simulate the amount of time an action takes	
 * @param actiontime
 */
	private void action(int actiontime) {
		long temp = random.nextInt(actiontime) + 1;
		TotalTime += temp;
		try {
			Thread.sleep(temp);
		} catch (InterruptedException e) {
			System.out.println("Problem action");
		}
	}
/**
 * Used to simulate the amount of time an action takes but not in a random amount of time; 	
 * @param actiontime
 */
	private void actionNoRandom(int actiontime){
		TotalTime += actiontime;
		try {
			Thread.sleep(actiontime);
		} catch (InterruptedException e) {
			System.out.println("Problem actionrandom");
		}
	}
	
/**
 * Used to find the map that contains the magic word in the forest
 * @return
 * @throws IOException
 */
	private Boolean Compass() throws IOException{
	    String line;
	    Scanner scanner = new Scanner(Main.Forest);
	    while(scanner.hasNextLine()){
	    	line = scanner.nextLine();
	    	//Trying to find a map
	    	action(200);
	    	//Checks if the map contains the magic word
    		if(line.equals(MagicWord)){
    			RT.setPriority(Thread.NORM_PRIORITY);
    			return true;
    		}
	    }
	    scanner.close();
	    //Did not find the map with the magic word
	    RT.setPriority(Thread.NORM_PRIORITY);
	    return false;
	}

/**
 * This simulates the reacer entering the forest and trying to find the map
 * @throws IOException
 */
	private void Forest() throws IOException{
		long obstime = System.currentTimeMillis();
		msg("Has entered the forest and searching for the map");
		
		//The racer is searching through the forest
		RT.setPriority(RT.getPriority() + random.nextInt(4));
		//If the racer finds the correct map they can leave the forest
		if(Compass()){
			msg("Has found the map with the magic word and is now leaving the Forest!");
		}
		//The racer failed to find the map with the magic word
		else{
			Thread.yield();
			Thread.yield();
			msg("Failed to find the map and searched through the entire forest and is leaving");
			
		}
		ObstacleTime[0] += (System.currentTimeMillis() - obstime);
	}

/**
 * Simulates the racer trying to pass through the Mountain
 * @throws InterruptedException 
 */
	private void Mountain(){
		long obstime = System.currentTimeMillis();
		msg("Has entered the Mountain and is waiting to cross the passage");
		//All racers are now waiting for their turn to cross the passage
		//The semaphores makes sure that only one racer crosses the passage at a time
		try {
			Main.mutex.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			msg("Is now crossing the Passage");
			action(1999);
			msg("Has now passed the Mountain!");
		Main.mutex.release();
		
		//Crossed the passage
		ObstacleTime[1] += (System.currentTimeMillis() - obstime);
		
	}

/**
 * Simulate the race crossing the River
 */
	private void River(){
		long obstime = System.currentTimeMillis();
		//Has arrived and will now wait to join group
		msg("Has arrived at the river and is waiting to form a group to cross the river");
		actionNoRandom(100);
		
		//Access the group
		try {
			Main.mutex2.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Main.numlines++;

		//Form Group using semaphotres
		if(Main.numlines % 2 != 0 && Integer.parseInt(Main.RacersCount) != Main.numlines){
			Main.mutex2.release();
			msg("Has joined the group and waiting for another racer to join");
			try {
				Main.Group.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//Let group cross the river using the release called from the judge thread
		else{
			Main.mutex2.release();
			Main.J.CheckRiver();
		}
		msg("Has departed with the group across the river!");
		actionNoRandom(1999);
		//Group has crossed the river
		ObstacleTime[2] += (System.currentTimeMillis() - obstime);
		msg("Has crossed the river");
		//Running out of the boat
		action(1000);
	}

/**
 * Simulates what the racer ones they reach the finish line
 * @throws InterruptedException 
 */
	private void GoHome() throws InterruptedException{
		Main.mutex3.acquire();
		Main.numatFinishline++;
		msg("Is at the finsih line!");
		//Group up the racers in the finish line
		if(Main.numatFinishline == Integer.parseInt(Main.RacersCount)){
			Main.mutex3.release();
			msg("Is the last racer to arrive at the Finish Line");
			Main.JudgeWait.release();
			Main.FinsihLineWait.acquire();
		}
		//Wait for the other racers to arrive to get the results
		else{
			Main.mutex3.release();
			msg("is waiting to get their results!");
			Main.FinsihLineWait.acquire();
		}
			
		msg("Seeing if their friend has gone home yet");

		//Makes sure the Racer's friend was in the race
		if(RacerNum < Main.Racers.size())
			//Checks if their friend are still in the race
			while(Main.Racers.get(RacerNum).getThread().isAlive()){
				//Now waiting until friend has finished the race
				msg("Waiting for friend");
				Main.Racers.get(RacerNum).getThread().join();;
				msg("Friend is leaving and they both now are going to go home");
			}

		//Racers have gone home
		msg("Has now gone home");
		Main.numofRacersLeft--;
		//Releases the judge so they can go home too
		if(Main.numofRacersLeft == 0)
			Main.JudgeWait.release();

	}
/**
 * Begins the thread once called
 */
	public void start(){
		msg("has started the race");
		RT.start();
	}

/**
 * Once the thread starts it starts the Race simulation
 */
	public void run() {
		Rest(1000);
		try {
			Forest();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Rest(1000);
		Mountain();
		Rest(1000);
		River();
		Judge.AddToLeaderBoard(Main.Racers.get(RacerNum - 1));
		try {
			GoHome();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
