package labs.photo;

import org.jacop.constraints.*;
import org.jacop.search.*;
import org.jacop.core.*;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Photo {

    public static void main(String[] args) {

        System.out.println("Choose example data (1, 2 or 3)");

        Scanner sc = new Scanner(System.in);
        int choice = 0;
        try {
            choice = sc.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("No valid input");
            System.exit(0);
        }

        int n = 0;
        int n_prefs = 0;
        int[][] prefs = {};

        switch (choice) {
            case 1:
                n = 9;
                n_prefs = 17;
                prefs = new int[][] {{1,3}, {1,5}, {1,8},
                        {2,5}, {2,9}, {3,4}, {3,5}, {4,1},
                        {4,5}, {5,6}, {5,1}, {6,1}, {6,9},
                        {7,3}, {7,8}, {8,9}, {8,7}};
                break;
            case 2:
                n = 11;
                n_prefs = 20;
                prefs = new int[][] {{1,3}, {1,5}, {2,5},
                    {2,8}, {2,9}, {3,4}, {3,5}, {4,1},
                    {4,5}, {4,6}, {5,1}, {6,1}, {6,9},
                    {7,3}, {7,5}, {8,9}, {8,7}, {8,10},
                    {9, 11}, {10, 11}};
                break;
            case 3:
                n = 15;
                n_prefs = 20;
                prefs = new int[][] {{1,3}, {1,5}, {2,5},
                    {2,8}, {2,9}, {3,4}, {3,5}, {4,1},
                    {4,15}, {4,13}, {5,1}, {6,10}, {6,9},
                    {7,3}, {7,5}, {8,9}, {8,7}, {8,14},
                    {9, 13}, {10, 11}};
                break;
            default:
                System.out.println("No valid input");
                System.exit(0);

        }

        Store store = new Store();

        IntVar[] persons = new IntVar[n];

        for (int i = 0; i < n; i++) {
            persons[i] = new IntVar(store, "Person " + (i + 1), 1, n);
        }
        store.impose(new Alldiff(persons));
        store.impose(new XltY(persons[0],persons[1]));

        IntVar[] wishesFulfilledVector = new IntVar[n_prefs];
        int[] neg = new int[n_prefs];

        for (int i = 0; i < n_prefs; i++) {
            wishesFulfilledVector[i] = new IntVar(store, 0, 1);
            neg[i] = -1;
        }

        for (int i = 0; i < n_prefs; i++) {
            PrimitiveConstraint dist = new Distance(persons[prefs[i][0] - 1], persons[prefs[i][1] - 1], new IntVar(store, 1, 1));
            store.impose(new Reified(dist, wishesFulfilledVector[i]));
        }

        IntVar wishesFulfilled = new IntVar(store, "Wishes Fulfilled", -n_prefs, 0);
        store.impose(new LinearInt(wishesFulfilledVector, neg, "==", wishesFulfilled));

        Search<IntVar> label = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(persons, new MostConstrainedDynamic<>(), new IndomainMax<IntVar>());

        label.setSolutionListener(new PrintOutListener<>());
        boolean result = label.labeling(store, select, wishesFulfilled);

        int negWishes = -wishesFulfilled.value();

        System.out.println("Regular problem");
        System.out.println("------------------------");

        System.out.println(negWishes + " wishes fulfilled");

        for (int i = 0; i < n; i++) {
            System.out.println(persons[i]);
        }
        System.out.println("------------------------");

        /*--------------------------------------------*/
        System.out.println();
        System.out.println();

        Store store_mod = new Store();

        IntVar[] persons_mod = new IntVar[n];

        for (int i = 0; i < n; i++) {
            persons_mod[i] = new IntVar(store_mod, "Person " + (i + 1), 1, n);
        }
        store_mod.impose(new Alldiff(persons_mod));

        //IntVar[] wishesFulfilledVector_mod = new IntVar[n_prefs];
        IntVar[] distances_mod = new IntVar[n_prefs];

        for (int i = 0; i < n_prefs; i++) {
            //wishesFulfilledVector_mod[i] = new IntVar(store_mod, 0, 1);
            distances_mod[i] = new IntVar(store_mod, 1, n - 1);
        }

        for (int i = 0; i < n_prefs; i++) {
            PrimitiveConstraint dist_mod = new Distance(persons_mod[prefs[i][0] - 1], persons_mod[prefs[i][1] - 1], distances_mod[i]);
            //PrimitiveConstraint dist_mod2 = new Distance(persons_mod[prefs[i][0] - 1], persons_mod[prefs[i][1] - 1], new IntVar(store_mod, 1, 1));
            store_mod.impose(dist_mod);
           // store_mod.impose(new Reified(dist_mod2, wishesFulfilledVector_mod[i]));
        }

        IntVar maxDist = new IntVar(store_mod, 0, Integer.MAX_VALUE);
        //IntVar wishesFulfilled_mod = new IntVar(store_mod, 0, n_prefs);
        //store_mod.impose(new SumInt(wishesFulfilledVector_mod, "==", wishesFulfilled_mod));

        store_mod.impose(new Max(distances_mod, maxDist));

        Search<IntVar> label_mod = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select_mod = new SimpleSelect<IntVar>(persons_mod, null, new IndomainMin<IntVar>());

        boolean result_mod = label_mod.labeling(store_mod, select_mod, maxDist);

        System.out.println("Modified problem");
        System.out.println("------------------------");

        System.out.println("The maximal distance is " + maxDist.value());

        for (int i = 0; i < n; i++) {
            System.out.println(persons_mod[i]);
        }
        System.out.println("------------------------");

    }
}
