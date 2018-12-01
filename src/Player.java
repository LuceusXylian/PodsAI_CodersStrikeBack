import java.util.*;
import java.awt.Point;
import java.util.ArrayList;

class Player {
    private static final int zenCount = 2;
    private static final int boostDist = 2000;
    private static final int RADIUS = 350;
    private static Boolean canUseBoost = true;
    private static double zen;
    private static double zenList[] = new double[zenCount];
    private static ArrayList <Point> checkpoints = new ArrayList<>();

    private static void log(String name, double value) {
        System.err.println(name + ": " + value);
    }

    private static class Pod {
        int x, y, speedX, speedY, angle, nextCheckPointId;

        private Point getPoint() {
            Point point = new Point();
            point.x = x;
            point.y = y;
            return point;
        }
    }

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Point goal;
        for(int i =0; i<zenCount; i++) {
            zenList[i] = 0;
        }

        int laps = in.nextInt();
        int checkpointCount = in.nextInt();
        for (int i = 0; i < checkpointCount; i++) {
            int checkpointX = in.nextInt();
            int checkpointY = in.nextInt();

            Point nextCheckpoint  = new Point();
            nextCheckpoint.x = checkpointX;
            nextCheckpoint.y = checkpointY;
            checkpoints.add(nextCheckpoint);
        }

        // game loop
        while (true) {
            Pod[] racers = new Pod[2];
            for (int i = 0; i < 2; i++) {
                int x = in.nextInt(); // x position of your pod
                int y = in.nextInt(); // y position of your pod
                int vx = in.nextInt(); // x speed of your pod
                int vy = in.nextInt(); // y speed of your pod
                int angle = in.nextInt(); // angle of your pod
                int nextCheckPointId = in.nextInt(); // next check point id of your pod

                racers[i] = new Pod();
                racers[i].x = x;
                racers[i].y = y;
                racers[i].speedX = vx;
                racers[i].speedY = vy;
                racers[i].angle = angle;
                racers[i].nextCheckPointId = nextCheckPointId;
            }

            Pod[] opponents  = new Pod[2];
            for (int i = 0; i < 2; i++) {
                int x2 = in.nextInt(); // x position of the opponent's pod
                int y2 = in.nextInt(); // y position of the opponent's pod
                int vx2 = in.nextInt(); // x speed of the opponent's pod
                int vy2 = in.nextInt(); // y speed of the opponent's pod
                int angle2 = in.nextInt(); // angle of the opponent's pod
                int nextCheckPointId2 = in.nextInt(); // next check point id of the opponent's pod

                opponents[i] = new Pod();
                opponents[i].x = x2;
                opponents[i].y = y2;
                opponents[i].speedX = vx2;
                opponents[i].speedY = vy2;
                opponents[i].angle = angle2;
                opponents[i].nextCheckPointId = nextCheckPointId2;
            }
            log("nextCheckPointId", racers[0].nextCheckPointId);

            // Find the upcoming checkpoint  //nextCheckPointId starts with 1, but the goal is 0
            Point currentCheckpoint = (racers[0].nextCheckPointId -1 < 0)?
                    checkpoints.get(checkpoints.size()-1) : checkpoints.get(racers[0].nextCheckPointId -1);

            // Calculate the goal between the current checkpoint and the next one
            goal = calculateGoal(currentCheckpoint, checkpoints.get(racers[0].nextCheckPointId));

            // Calculate the speed
            // Send command to the racer
            movePod(Math.round(opponents[0].x), Math.round(opponents[0].y),
                    adjustSpeed(distanceBetween(racers[0].getPoint(), opponents[0].getPoint()), calculateAngle(racers[0].getPoint(), opponents[0].getPoint(), racers[0].angle)));

            movePod(Math.round(goal.x), Math.round(goal.y),
                    adjustSpeed(distanceBetween(racers[1].getPoint(), goal), calculateAngle(racers[1].getPoint(), goal, racers[1].angle)));

            if(canUseBoost) log("canUseBoost", 1);
            else log("canUseBoost", 0);
        }
    }

    private static double calculateAngle(Point currentPoint, Point targetPoint, int currentAngle) {
        return (Math.atan2(targetPoint.x - currentPoint.x, targetPoint.y - currentPoint.y));
    }

    private static int distanceBetween(Point point1, Point point2) {
        return (int) Math.sqrt(Math.pow(point2.x - point1.x, 2) + Math.pow(point2.y - point1.y, 2));
    }

    private static boolean pointsAreEqual(Point point1, Point point2) {
        return point1.x == point2.x && point1.y == point2.y;
    }

    private static int indexOfPoint(Point checkpoint){
        for (int i = 0, size = checkpoints.size(); i < size; i++) {
            if (pointsAreEqual(checkpoints.get(i), checkpoint)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean attackPod(Point opponent, int distance, double angle) {
        //calculate which opponent pod is first and attack it
        log("opponentDistance", distance);
        if(canUseBoost) {
            movePod(Math.round(opponent.x), Math.round(opponent.y), -1);
            canUseBoost=false;
        } else {
            movePod(Math.round(opponent.x), Math.round(opponent.y), -2);
        }
        return true;
    }

    private static boolean defendPod(int distance, Point goal){
        //returns true if shield was used
        if(distance <= 400) {
            movePod(Math.round(goal.x), Math.round(goal.y), -2);
            return true;
        }
        return false;
    }

    private static int calculateThrust(double angle) {
        int thrust;
        double zenSum = 0;
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
        return thrust;
    }

    private static int adjustSpeed(int distance, double angle) {
        log("Angle", angle);
        log("Dist", distance);

        // If angle is too wide
        if (angle >= 90 || angle <= -90) {
            return 10;
        } else if(angle >= 80 || angle <= -120 && distance >= 3000) {
            int thrust = calculateThrust(angle) -10;
            if(thrust < 0) return 0;
            else return thrust;

            // If the goal is far enough away
            // AND the boost is available
            // AND the racer is heading straight for the goal
        } else if (distance > boostDist && canUseBoost && angle == 0) {
            canUseBoost=false;
            return -1;
        } else {
            return calculateThrust(angle);
        }
    }

    private static Point calculateGoal(Point current, Point goal) {
        Point point1 = new Point(), point2 = new Point();

        int my = goal.y - current.y;
        int mx = goal.x - current.x;
        int m = mx == 0? my : my/mx;

        int b = goal.y - m * goal.x;
        int x1 = (int) (goal.x + RADIUS / Math.sqrt(1 + m * m));
        int x2 = (int) (goal.x - RADIUS / Math.sqrt(1 + m * m));

        point1.x = x1;
        point1.y = m * x1 + b;

        point2.x = x2;
        point2.y = m * x2 + b;

        // Return the point that is closer to the racer
        if (distanceBetween(current, point1) < distanceBetween(current, point2)) {
            return point1;
        }

        return point2;
    }

    private static void movePod(int X, int Y, int thrust) {
        log("thrust", thrust);
        switch(thrust){
            default:
                System.out.println(X + " " + Y + " " + thrust);
                return;
            case -1:
                System.out.println(X + " " + Y + " BOOST");
                return;
            case -2:
                System.out.println(X + " " + Y + " SHIELD");
                return;
        }
    }
}