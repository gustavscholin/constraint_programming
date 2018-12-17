package labs.auto_regression_filter;

import org.jacop.constraints.*;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.*;

public class AutoRegressionFilter {

    public static void main(String[] args) {
        int del_add = 1;
        int del_mul = 2;

        int number_add = 1;
        int number_mul = 3;
        int n = 28;

        int[] last = {27,28};

        int[] add = {9,10,11,12,13,14,19,20,25,26,27,28};

        int[] mul = {1,2,3,4,5,6,7,8,15,16,17,18,21,22,23,24};

        int[][] dependencies = {
                {9},
                {9},
                {10},
                {10},
                {11},
                {11},
                {12},
                {12},
                {27},
                {28},
                {13},
                {14},
                {16, 17},
                {15, 18},
                {19},
                {19},
                {20},
                {20},
                {22, 23},
                {21, 24},
                {25},
                {25},
                {26},
                {26},
                {27},
                {28},
                {},
                {},
        };

        Store store = new Store();

        IntVar[][] rectangles = new IntVar[n][4];
        for (int i = 0; i < n; i++) {
            rectangles[i][0] = new IntVar(store, "Start time for operation " + (i + 1), 0, 10000);
            if (contains(add, i + 1)) {
                rectangles[i][1] = new IntVar(store, "Resource for operation " + (i + 1), 0, number_add - 1);
                rectangles[i][2] = new IntVar(store, del_add, del_add);
            } else {
                rectangles[i][1] = new IntVar(store, "Resource for operation " + (i + 1), 0, number_mul - 1);
                rectangles[i][2] = new IntVar(store, del_mul, del_mul);
            }
            rectangles[i][3] = new IntVar(store, 1, 1);
        }

        IntVar[] add_starts = new IntVar[add.length];
        IntVar[] add_durs = new IntVar[add.length];
        IntVar[] add_res = new IntVar[add.length];

        for (int i = 0; i < add.length; i++) {
            add_starts[i] = rectangles[add[i] - 1][0];
            add_durs[i] = rectangles[add[i] - 1][2];
            add_res[i] = new IntVar(store, 1, 1);
        }

        store.impose(new Cumulative(add_starts, add_durs, add_res, new IntVar(store, number_add, number_add)));


        IntVar[] mul_starts = new IntVar[mul.length];
        IntVar[] mul_durs = new IntVar[mul.length];
        IntVar[] mul_res = new IntVar[mul.length];

        for (int i = 0; i < mul.length; i++) {
            mul_starts[i] = rectangles[mul[i] - 1][0];
            mul_durs[i] = rectangles[mul[i] - 1][2];
            mul_res[i] = new IntVar(store, 1, 1);
        }

        store.impose(new Cumulative(mul_starts, mul_durs, mul_res, new IntVar(store, number_mul, number_mul)));

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < dependencies[i].length; j++) {
                store.impose(new XplusYlteqZ(rectangles[i][0], rectangles[i][2], rectangles[dependencies[i][j] - 1][0]));
            }
        }

        IntVar[][] add_recs = new IntVar[add.length][4];
        IntVar[][] mul_recs = new IntVar[mul.length][4];
        int add_index = 0;
        int mul_index = 0;
        for (int i = 0; i < n; i++) {
            if (contains(add, i + 1)) {
                add_recs[add_index][0] = rectangles[i][0];
                add_recs[add_index][1] = rectangles[i][1];
                add_recs[add_index][2] = rectangles[i][2];
                add_recs[add_index][3] = rectangles[i][3];
                add_index++;
            } else {
                mul_recs[mul_index][0] = rectangles[i][0];
                mul_recs[mul_index][1] = rectangles[i][1];
                mul_recs[mul_index][2] = rectangles[i][2];
                mul_recs[mul_index][3] = rectangles[i][3];
                mul_index++;
            }
        }

        store.impose(new Diff2(add_recs));
        store.impose(new Diff2(mul_recs));

        IntVar[][] start_resource_times = new IntVar[n][2];
        for (int i = 0; i < n; i++) {
            start_resource_times[i][0] = rectangles[i][0];
            start_resource_times[i][1] = rectangles[i][1];
        }

        IntVar[] maxCands = new IntVar[last.length];
        IntVar cost = new IntVar(store, 0, 10000);

        for (int i = 0; i < last.length; i++) {
            maxCands[i] = new IntVar(store, 0, 10000);
            store.impose(new XplusYeqZ(rectangles[last[i] - 1][0], rectangles[last[i] - 1][2], maxCands[i]));
        }

        store.impose(new Max(maxCands, cost));

        Search<IntVar> label = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new SimpleMatrixSelect<>(start_resource_times, new MostConstrainedDynamic<>(), new IndomainMin<IntVar>());

        boolean result = label.labeling(store, select, cost);

        System.out.println();
        System.out.println("---------------------------------------");
        System.out.println();

        for (int i = 0; i < n; i++) {
            if (contains(add, i + 1)) {
                System.out.println(start_resource_times[i][0] + "  \t" + start_resource_times[i][1] + "\t Adder" );
            } else {
                System.out.println(start_resource_times[i][0] + "  \t" + start_resource_times[i][1] + "\t Multiplier" );
            }

        }

    }

    private static boolean contains(int[] array, int key) {
        for (int i : array) {
            if (i == key) {
                return true;
            }
        }
        return false;
    }
}
