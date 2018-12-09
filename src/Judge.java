import java.util.Vector;

public class Judge implements Runnable{
	private Racer RacersBatch[];
	private static Vector<Racer> LeaderBoard;
	private static long time = 0;
	private Thread JT;	//Judge Thread
	
	public Judge(Racer[] RacerBatch, long time){
		LeaderBoard = new Vector<Racer>();
		this.RacersBatch = RacerBatch;
		Judge.time = time;
		JT = new Thread(this, "Judge");
	}
	

/**
 * Used to know what place each racer came in
 * @param R
 */
	public synchronized static void AddToLeaderBoard(Racer R){
		LeaderBoard.add(R);
	}
/**
 * Used when all the racers have finished the race and will be released from waiting
 * @return
 */
	private void RaceHasEnded(){
		while(Main.FinsihLineWait.hasQueuedThreads())
			Main.FinsihLineWait.release();
	}
/**
 * Checks what racers are waiting to be awoken at the river
 */
	public void CheckRiver(){
		for(int i = 0; i < 1; i++)
			Main.Group.release();
	}

/**
 * Shows the result of the race
 */
	private void ShowResults(){
		for(int i = 0; i < LeaderBoard.size(); i++){
			System.out.println(LeaderBoard.get(i).getName() + " came in " + (i + 1 ) + " place!");
			System.out.println("Report");
			System.out.println("Total Time to complete Race: " + LeaderBoard.get(i).getTotalTime());
			System.out.println("Obstacle Time");
			LeaderBoard.get(i).getObstacleTime();
			System.out.println("");
		}
	}
	
/**
 * Message Output	
 * @param m
 */
	private void msg(String m) {
		System.out.println("[" + (System.currentTimeMillis() - time) + "] " + "Judge" + ": " + m);
	}

/**
 * Starts the thread
 */
	public void start(){
		msg("has arrived at the race");
		JT.start();
	}
/**
 * Runs the judge thread
 */
	@Override
	public void run() {
	
		//Waiting for the racers to all finish the race
		try {
			Main.JudgeWait.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Racers at the finish line show them their results
		msg("The race has ended and the judge is going to show the results!");
		ShowResults();
		RaceHasEnded();
		//Waiting for everyone to leave home
		try {
			Main.JudgeWait.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Race has Finished
		msg("The judge has left home");
		
	}

}
