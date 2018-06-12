import java.util.*;
import java.awt.Point;
import java.util.ArrayList;

class Player {
	static final int zenCount = 4;
	static final int boostDist = 2000;
	static final int radius = 350;
	static Boolean canUseBoost = true,
        listComplete = false;
    static int thrust;
	static int xOpppnentDiff;
	static int yOpppnentDiff;
	static double zen, zenSum;
    static double zenList[] = new double[zenCount];
    static ArrayList <Point> checkpoints = new ArrayList<Point>();
        
    public static void log(String name, int value) {
        System.err.println(name + ": " + value);
    }
    
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Point goal = new Point();
        
        for(int i =0; i<zenCount; i++) {
            zenList[i] = 0;
        }
        
        // game loop
        while (true) {
            int x = in.nextInt();
            int y = in.nextInt();
            int nextCheckpointX = in.nextInt(); // x position of the next check point
            int nextCheckpointY = in.nextInt(); // y position of the next check point
            int nextCheckpointDist = in.nextInt(); // distance to the next checkpoint
            int nextCheckpointAngle = in.nextInt(); // angle between your pod orientation and the direction of the next checkpoint
            int opponentX = in.nextInt();
            int opponentY = in.nextInt();
            
            Point racer = new Point();
            racer.x = x;
            racer.y = y;
            
            Point nextCheckpoint  = new Point();
            nextCheckpoint.x = nextCheckpointX;
            nextCheckpoint.y = nextCheckpointY;
            
	         // Get index of the current checkpoint
	            int checkIndex = indexOfPoint(nextCheckpoint);
	
	            // If the index of the checkpoint is 0 and the list of checkpoints contains more than 2 checkpoints
	            if (checkIndex == 0 && checkpoints.size() > 1) {
	                // All checkpoints have been found
	                listComplete = true;
	            }
	
	            // If the checkpoint is not contained in the list yet
	            if (checkIndex < 0) {
	                // Add checkpoint to the list
	                checkpoints.add(nextCheckpoint);
	
	                // Calculate goal between racer and checkpoint
	                goal = calculateGoal(racer, nextCheckpoint);
	
	            }
	            // If the list is complete
	            else if (listComplete) {
	                // Find the upcoming checkpoint
	                Point nextCheck = (checkIndex + 1 == checkpoints.size()) ? checkpoints.get(0) : checkpoints.get(checkIndex + 1);
	
	                // Calculate the goal between the current checkpoint and the next one
	                goal = calculateGoal(nextCheck, nextCheckpoint);
	            }
	
	            // Calculate the speed
	            // Send command to the racer
	            movePod(Math.round(goal.x), Math.round(goal.y), adjustSpeed(distanceBetween(racer, goal), nextCheckpointAngle));
        }
    }
    
    public static int distanceBetween(Point point1, Point point2) {
        return (int) Math.sqrt(Math.pow(point2.x - point1.x,2) + Math.pow(point2.y - point1.y, 2));
    }
    
    public static boolean pointsAreEqual(Point point1, Point point2) {
        return point1.x == point2.x && point1.y == point2.y;
    }
    
    public static int indexOfPoint(Point checkpoint){
        for (int i = 0, size = checkpoints.size(); i < size; i++) {
            if (pointsAreEqual(checkpoints.get(i), checkpoint)) {
                return i;
            }
        }
        return -1;
    }
        
    public static int adjustSpeed(int distance, int angle) {
        log("Angle", angle);
        log("Dist", distance);
        
        //calculate the thrust
        zenSum = 0;
        for(int i =0; i<zenCount-1; i++) {
            zenList[i+1] = zenList[i];
            zenSum += zenList[i+1];
        }
        zenList[0] = (angle / 180.0);
        if(zenList[0] < 0) {
            zenList[0] = zenList[0]/-1.0;
        }
        zenSum += zenList[0];
        zen = zenSum/zenCount;
        thrust = ((int)((double) 100-100.0*zen));
        if(thrust < 0 || thrust > 100) {
            thrust=66;
        }
        
        // If angle is too wide
        if (angle >= 90 || angle <= -90) {
            return 0;
        }
        // If the goal is far enough away
        // AND the boost is available
        // AND the racer is heading straight for the goal
        // AND the first lap has passed
        else if (distance > boostDist && canUseBoost && angle == 0 && listComplete) {
            canUseBoost=false;
            return -1;
        } else {
            return thrust;
        }
    }
    
    public static Point calculateGoal(Point current, Point goal) {
        Point point1 = new Point(), point2 = new Point();
        int m, b, x1, x2,
        
        m1 = (goal.y - current.y) / (goal.x - current.x);
        b = goal.y - m1 * goal.x;
        x1 = (int) (goal.x + radius / Math.sqrt(1 + m1 * m1));
        x2 = (int) (goal.x - radius / Math.sqrt(1 + m1 * m1));
        
        point1.x = x1;
        point1.y = m1 * x1 + b;
        
        point2.x = x2;
        point2.y = m1 * x2 + b;
    
        // Return the point that is closer to the racer
        if (distanceBetween(current, point1) < distanceBetween(current, point2)) {
            return point1;
        }
    
        return point2;
    }
    
    public static void movePod(int X, int Y, int thrust) {
        if(thrust == -1) {
            System.out.println(X + " " + Y + " BOOST BOOST");
        } else {
            System.out.println(X + " " + Y + " " + thrust);
        }
    }
}