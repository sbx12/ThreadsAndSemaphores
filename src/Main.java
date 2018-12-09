import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class Main extends Thread{
	public static final Vector<Thread> WaitLine = new Vector<Thread>();
	public static final Vector<Racer> Racers = new Vector<Racer>();
	private static long StartTime = System.currentTimeMillis();
	private static Racer RacersBatch[];
	private static Random random = new Random();
	public static String Forest = "";   		//Contains the forest
	public static String RacersCount;			//Gets the number of racers
	public static Semaphore mutex = new Semaphore(1, true);		//Semaphore used in the mountain
	public static Semaphore mutex2 = new Semaphore(1, true);	//Semaphore 2 used in the river
	public static Semaphore mutex3 = new Semaphore(1, true);	//Semaphore 3 used in the goHome
	public static Semaphore Group = new Semaphore(0, true);	    //Semaphore for river group which is 2
	public static Semaphore JudgeWait = new Semaphore(0, true);	    //Semaphore for river group which is 2
	public static Semaphore FinsihLineWait = new Semaphore(0, true);	    //Semaphore for river group which is 2
	public static int numlines = 0; 							//Used for the river group
	public static int numatFinishline = 0;
	public static int numofRacersLeft = 0;
	public static Judge J;										//The Judge

	
	public static void main(String[] args) throws IOException {
		//Get number of Racers
		Scanner scanner = new Scanner(System.in);
		System.out.print("Please input the number of Racers: ");
		RacersCount = scanner.next();
		while(Integer.parseInt(RacersCount) < 1 ||  Integer.parseInt(RacersCount) > 10){
			System.out.print("Not Valid. Please input the number of Racers[1-10]: ");
			RacersCount = scanner.next();
		}
		
		//Prepare Program
		CreateForest();
		int NumofRacers = Integer.parseInt(RacersCount);
		numofRacersLeft = NumofRacers;
		RacersBatch = new Racer[NumofRacers];
		for(int i = 0; i < RacersBatch.length; i++){
			RacersBatch[i] = new Racer(i + 1, CreateMagicWord(), WaitLine, StartTime);
			Racers.add(RacersBatch[i]);
		}
		J = new Judge(RacersBatch, StartTime);
		
		//Start the program
		J.start();
		for(int i = 0; i < RacersBatch.length; i++)
			RacersBatch[i].start();
	}

/**
 * Create the Race Time
 * @return
 */
	protected static final long RaceTime(){
		return System.currentTimeMillis() - StartTime;
	}
	
/**
 * Creates the forest filled with random letters
 */
	private static void CreateForest(){
	    Random rand = new Random();
	    String alphabet = "abcd";
	    for (int i = 1; i < 401; i++){
	    	if(i % 4 == 1 && i != 1)
	    		Forest += "\n";
	    	Forest += alphabet.charAt(rand.nextInt(alphabet.length()));
	    }
	}

/**
 * Reads off the Forest and brings back the magic word
 * @return
 * @throws IOException
 */
	private static String CreateMagicWord() throws IOException{
		Random rand = new Random();
		String alphabet = "abcd";
		String MagicWord = "";
	    for (int i = 0; i < 4; i++)
	    	MagicWord += alphabet.charAt(rand.nextInt(alphabet.length()));
	    return MagicWord;
	}

}
