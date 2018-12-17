package labs.logistics;

import org.jacop.core.*;
import org.jacop.constraints.*;
import org.jacop.search.*;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Logistics {

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

        int graph_size = 0;
        int start = 0;
        int n_dests = 0;
        int[] dest = {};
        int n_edges = 0;
        int[] from = {};
        int[] to = {};
        int[] cost = {};

        switch (choice) {
            case 1:
                graph_size = 6;
                start = 1;
                n_dests = 1;
                dest = new int[] {6};
                n_edges = 7;
                from = new int[] {1,1,2,2,3,4,4};
                to = new int[] {2,3,3,4,5,5,6};
                cost = new int[] {4,2,5,10,3,4,11};
                break;
            case 2:
                graph_size = 6;
                start = 1;
                n_dests = 2;
                dest = new int[] {5,6};
                n_edges = 7;
                from = new int[] {1,1,2, 2,3,4, 4};
                to = new int[] {2,3,3, 4,5,5, 6};
                cost = new int[] {4,2,5,10,3,4,11};
                break;
            case 3:
                graph_size = 6;
                start = 1;
                n_dests = 2;
                dest = new int[] {5,6};
                n_edges = 9;
                from = new int[] {1,1,1,2,2,3,3,3,4};
                to = new int[] {2,3,4,3,5,4,5,6,6};
                cost = new int[] {6,1,5,5,3,5,6,4,2};
                break;
            default:
                System.out.println("No valid input");
                System.exit(0);

        }

        Store store = new Store();

        IntVar[][] solution = new IntVar[n_dests][graph_size];
        ArrayList<ArrayList<Integer>> neighbours = new ArrayList<>();
        ArrayList<ArrayList<Integer>> costs = new ArrayList<>();

        for (int i = 0; i < graph_size; i++) {
            neighbours.add(new ArrayList<Integer>());
            costs.add(new ArrayList<Integer>());
            if (i + 1 != start) {
                neighbours.get(i).add(i + 1);
                costs.get(i).add(0);
            }
        }

        for (int i = 0; i < n_edges; i++) {
            neighbours.get(from[i] - 1).add(to[i]);
            costs.get(from[i] - 1).add(cost[i]);
            neighbours.get(to[i] - 1).add(from[i]);
            costs.get(to[i] - 1).add(cost[i]);
        }

        for (int i = 0; i < n_dests; i++) {
            neighbours.get(dest[i] - 1).add(start);
            costs.get(dest[i] - 1).add(0);
        }



        for (int i = 0; i < n_dests; i++) {
            for (int j = 0; j < graph_size; j++) {
                solution[i][j] = new IntVar(store, "Dest " + dest[i] + ", node " + (j + 1));
                if (j + 1 != start) {
                    solution[i][j].addDom(j+1,j+1);
                }
            }
            solution[i][dest[i] - 1].addDom(start,start);
        }

        for (int i = 0; i < n_edges; i++) {
            for (int j = 0; j < n_dests; j++) {
                IntervalDomain dom1 = new IntervalDomain(from[i],from[i]);
                IntervalDomain dom2 = new IntervalDomain(to[i], to[i]);
                if (from[i] == start) {
                    solution[j][from[i] - 1].addDom(dom2);
                } else if (to[i] == start) {
                    solution[j][to[i] - 1].addDom(dom1);
                } else {
                    solution[j][from[i] - 1].addDom(dom2);
                    solution[j][to[i] - 1].addDom(dom1);
                }
            }
        }

        for (int i = 0; i < n_dests; i++) {
            IntVar[] nodes = new IntVar[graph_size];
            for (int j = 0; j < graph_size; j++) {
                nodes[j] = solution[i][j];
            }
            store.impose(new Subcircuit(nodes));
        }

        IntVar[][] indexes = new IntVar[n_dests][graph_size];
        IntVar[][] min_costs = new IntVar[n_dests][graph_size];

        for (int i = 0; i < n_dests; i++) {
            for (int j = 0; j < graph_size; j++) {
                indexes[i][j] = new IntVar(store, 0, neighbours.get(j).size());
                min_costs[i][j] = new IntVar(store, 0, Integer.MAX_VALUE);
                store.impose(new Element(indexes[i][j], convertIntegers(neighbours.get(j)), solution[i][j]));
                store.impose(new Element(indexes[i][j], convertIntegers(costs.get(j)), min_costs[i][j]));
            }
        }

        IntVar[] dest_dists = new IntVar[n_dests];

        for (int i = 0; i < n_dests; i++) {
            IntVar[] dists = new IntVar[graph_size];
            dest_dists[i] = new IntVar(store, 0, Integer.MAX_VALUE);
            for (int j = 0; j < graph_size; j++) {
                dists[j] = min_costs[i][j];
            }
            store.impose(new SumInt(dists, "==", dest_dists[i]));
        }

        IntVar sum_dists = new IntVar(store, "Minimal cost", 0, 1000);

        IntVar[] solution_singel = new IntVar[graph_size * n_dests];

        for (int j = 0; j < n_dests; j++) {
            for (int i = 0; i < graph_size; i++) {
                solution_singel[i + j * graph_size] = solution[j][i];
            }
        }

        IntVar common_sum = new IntVar(store, 0, 1000);
        IntVar[] common = new IntVar[n_edges];

        if (dest.length == 1) {

            store.impose(new SumInt(dest_dists, "==", sum_dists));

        } else {



            for (int i = 0; i < n_edges; i++) {
                common[i] = new IntVar(store, 0, 1);
                PrimitiveConstraint c1 = new XeqC(solution[0][from[i] - 1], to[i]);
                PrimitiveConstraint c2 = new XeqC(solution[1][from[i] - 1], to[i]);
                And a = new And(c1, c2);
                store.impose(new Reified(a, common[i]));
            }

            IntVar part_sum = new IntVar(store, 0, 1000);


            store.impose(new SumInt(dest_dists, "==", part_sum));
            store.impose(new LinearInt(common, cost, "==", common_sum));

            store.impose(new XplusYeqZ(sum_dists, common_sum, part_sum));

        }

        Search<IntVar> label = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(solution_singel, new MostConstrainedDynamic<>(), new IndomainMax<IntVar>());

        boolean result = label.labeling(store, select, sum_dists);

        System.out.println("--------------------------------");

        System.out.println("Minimal cost is " + sum_dists.value());

        for (int i = 0; i < graph_size*n_dests; i++) {
            if (i % graph_size == 0) {
                System.out.println();
            }
            System.out.println(solution_singel[i]);
        }


    }

    public static int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }
}
