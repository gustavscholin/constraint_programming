package labs.logistics;

import org.jacop.core.*;
import org.jacop.constraints.*;
import org.jacop.search.*;

public class Logistics {

    public static void main(String[] args) {

        int graph_size = 6;
        int start = 1;
        int n_dests = 1;
        int[] dest = {6};
        int n_edges = 7;
        int[] from = {1,1,2,2,3,4,4};
        int[] to = {2,3,3,4,5,5,6};
        int[] cost = {4,2,5,10,3,4,11};

        Store store = new Store();

        IntVar[][] solution = new IntVar[n_dests][graph_size];

        for (int i = 0; i < n_dests; i++) {
            for (int j = 0; j < graph_size; j++) {
                solution[i][j] = new IntVar(store, "Node " + (j + 1) + " in dest " + dest[i]);
                if (j + 1 != start) {
                    solution[i][j].addDom(j+1,j+1);
                }
            }
        }

        for (int i = 0; i < n_edges; i++) {
            for (int j = 0; j < n_dests; i++) {
                IntervalDomain dom1 = new IntervalDomain(from[i],from[i]);
                IntervalDomain dom2 = new IntervalDomain(to[i], to[i]);
                solution[j][from[i] - 1].addDom(dom2);
                solution[j][to[i] - 1].addDom(dom1);
            }
        }

        for (int i = 0; i < n_dests; i++) {
            IntVar[] nodes = new IntVar[graph_size];
            for (int j = 0; j < graph_size; j++) {
                nodes[j] = solution[i][j];
            }
            store.impose(new Subcircuit(nodes));
        }


    }
}
