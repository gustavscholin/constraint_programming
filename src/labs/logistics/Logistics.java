package labs.logistics;

import org.jacop.core.*;
import org.jacop.constraints.*;
import org.jacop.search.*;

import java.util.ArrayList;
import java.util.List;

public class Logistics {

    public static void main(String[] args) {

        int graph_size = 6;
        int start = 1;
        int n_dests = 2;
        int[] dest = {5,6};
        int n_edges = 9;
        int[] from = {1,1,1,2,2,3,3,3,4};
        int[] to = {2,3,4,3,5,4,5,6,6};
        int[] cost = {6,1,5,5,3,5,6,4,2};

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
                solution[i][j] = new IntVar(store, "Node " + (j + 1) + " in dest " + dest[i]);
                if (j + 1 != start) {
                    solution[i][j].addDom(j+1,j+1);
                }
            }
            solution[i][dest[i] - 1].addDom(start,start);
        }

        for (int i = 0; i < n_edges; i++) {
            for (int j = 0; j < n_dests; i++) {
                IntervalDomain dom1 = new IntervalDomain(from[i],from[i]);
                IntervalDomain dom2 = new IntervalDomain(to[i], to[i]);
                if (from[i] - 1 == start) {
                    solution[j][from[i] - 1].addDom(dom2);
                } else if (to[i] - 1 == start) {
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
