package hack.carpark;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Collections2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunCarpark
{
    private static int smartCount = 0;
    private static int naiveCount = 0;
    private static int tieCount = 0;

    public static void main(String ... args)
    {
        final String finalPosition = "ABCDE_";

        List<char []> all = allPermutations(finalPosition);

        for (char [] cars : all) {

            Carpark smart = new SmartCarpark(cars, finalPosition);
            Carpark naive = new NaiveCarpark(cars, finalPosition);

            smart.park();
            naive.park();

            smart.check();
            naive.check();

            evaluate(smart, naive);
        }

        System.out.printf("Smart: %d, Naive: %d, Tie: %d%n", smartCount, naiveCount, tieCount);
    }

    private static void evaluate(Carpark smart, Carpark naive)
    {
        if (smart.moves() < naive.moves()) {
            System.out.println("Smart parking wins!");
            smartCount++;
        }
        else if (naive.moves() < smart.moves()) {
            System.out.println("Naive parking wins!");
            naiveCount++;
        }
        else {
            System.out.println("Tie!");
            tieCount++;
        }
    }

    private static List<char []> allPermutations(String str)
    {
        List<Character> l = new ArrayList<>();
        for (char c : str.toCharArray()) {
            l.add(c);
        }

        List<char []> all = new ArrayList<>();

        for (List<Character> lc : Collections2.orderedPermutations(l)) {
            char [] cars = new char [str.length()];
            for (int i = 0; i < lc.size(); i++) {
                cars[i] = lc.get(i);
            }
            all.add(cars);
        }
        return all;
    }

    public static char[] shuffle(final String str)
    {
        char [] cars = new char [str.length()];
        List<Character> l = new ArrayList<>();
        for (char c : str.toCharArray()) {
            l.add(c);
        }
        Collections.shuffle(l);
        for (int i = 0; i < l.size(); i++) {
            cars[i] = l.get(i);
        }

        return cars;
    }

    public abstract static class Carpark
    {
        protected final String finalPosition;
        protected final char[] cars;

        protected Map<Character, Integer> carPos = new HashMap<>();

        private int moves = 0;

        Carpark(final char [] shuffledCars, final String finalPosition)
        {
            this.cars = new char[shuffledCars.length];
            System.arraycopy(shuffledCars, 0, cars, 0, shuffledCars.length);

            this.finalPosition = finalPosition;
            for (int i = 0; i < cars.length; i++) {
                carPos.put(cars[i], i);
            }
        }

        public abstract void park();

        public int findPosition(char c)
        {
            return carPos.get(c);
        }

        public int moves()
        {
            return moves;
        }

        public void move(int newPos, int oldPos)
        {
            if (cars[newPos] != '_') {
                throw new IllegalStateException("crashed!");
            }
            cars[newPos] = cars[oldPos];
            carPos.put(cars[newPos], newPos);
            cars[oldPos] = '_';
            carPos.put('_', oldPos);
            moves++;
            report();
        }

        public void report()
        {
            System.out.printf("%d. %s%n", moves, Arrays.toString(cars));
        }

        public void check()
        {
            checkState(finalPosition.equals(new String(cars)), "%s vs %s", finalPosition, new String(cars));
        }
    }

    public static class NaiveCarpark extends Carpark
    {
        NaiveCarpark(final char [] shuffledCars, final String finalPosition)
        {
            super(shuffledCars, finalPosition);
        }

        @Override
        public void park()
        {
            int freePosition = findPosition('_');
            System.out.println("=============================");
            System.out.println("= Naive Parking...");

            report();
            for (int i = 0; i < cars.length; i++) {
                if (finalPosition.charAt(i) != cars[i]) {
                    int oldPosition = findPosition(finalPosition.charAt(i));
                    if (freePosition != i) {
                        move(freePosition, i);
                    }
                    move(i, oldPosition);
                    freePosition = oldPosition;
                }
            }
            System.out.println("=============================");
        }
    }

    public static class SmartCarpark extends Carpark
    {
        private int ratchet = 0;

        SmartCarpark(final char [] shuffledCars, final String finalPosition)
        {
            super(shuffledCars, finalPosition);
        }

        private int moveRatchet()
        {
            for (int i = ratchet; i < cars.length; i++) {
                if (cars[i] != finalPosition.charAt(i)) {
                    ratchet = i;
                    return ratchet;
                }
            }
            ratchet = cars.length;
            return ratchet;
        }

        public void park()
        {
            moveRatchet();
            int freePosition = findPosition('_');

            System.out.println("=============================");
            System.out.println("= Smart Parking...");
            report();
            while (ratchet != cars.length) {
                // what car should be there?
                char wanted = finalPosition.charAt(freePosition);
                int wantedPosition = findPosition(wanted);
                if (freePosition != wantedPosition) {
                    move(freePosition, wantedPosition);
                    freePosition = wantedPosition;
                }
                // the free slot is already at its final place...
                else {
                    moveRatchet();
                    if (ratchet == cars.length) {
                        break;
                    }
                    move(freePosition, ratchet);
                    freePosition = ratchet;
                }
            }
            System.out.println("=============================");
        }
    }
}
