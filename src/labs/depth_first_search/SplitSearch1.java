package labs.depth_first_search;

import org.jacop.constraints.Not;
import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.constraints.XlteqC;
import org.jacop.core.FailException;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;

public class SplitSearch1  {

    int wrongDecisions = 0;
    int n_nodes = 0;

    boolean trace = false;

    /**
     * Store used in search
     */
    Store store;

    /**
     * Defines varibales to be printed when solution is found
     */
    IntVar[] variablesToReport;

    /**
     * It represents current depth of store used in search.
     */
    int depth = 0;

    /**
     * It represents the cost value of currently best solution for FloatVar cost.
     */
    public int costValue = IntDomain.MaxInt;

    /**
     * It represents the cost variable.
     */
    public IntVar costVariable = null;

    public SplitSearch1(Store s) {
        store = s;
    }


    /**
     * This function is called recursively to assign variables one by one.
     */
    public boolean label(IntVar[] vars) {

        n_nodes++;

        if (trace) {
            for (int i = 0; i < vars.length; i++)
                System.out.print (vars[i] + " ");
            System.out.println ();
        }

        ChoicePoint choice = null;
        boolean consistent;

        // Instead of imposing constraint just restrict bounds
        // -1 since costValue is the cost of last solution
        if (costVariable != null) {
            try {
                if (costVariable.min() <= costValue - 1)
                    costVariable.domain.in(store.level, costVariable, costVariable.min(), costValue - 1);
                else
                    return false;
            } catch (FailException f) {
                return false;
            }
        }

        consistent = store.consistency();

        if (!consistent) {
            // Failed leaf of the search tree
            return false;
        } else { // consistent

            if (vars.length == 0) {
                // solution found; no more variables to label

                // update cost if minimization
                if (costVariable != null)
                    costValue = costVariable.min();

                reportSolution();

                return costVariable == null; // true is satisfiability search and false if minimization
            }

            choice = new ChoicePoint(vars);

            levelUp();

            store.impose(choice.getConstraint());

            // choice point imposed.

            consistent = label(choice.getSearchVariables());

            if (consistent) {
                levelDown();
                return true;
            } else {

                wrongDecisions++;

                restoreLevel();

                store.impose(new Not(choice.getConstraint()));

                // negated choice point imposed.

                consistent = label(vars);

                levelDown();

                if (consistent) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    void levelDown() {
        store.removeLevel(depth);
        store.setLevel(--depth);
    }

    void levelUp() {
        store.setLevel(++depth);
    }

    void restoreLevel() {
        store.removeLevel(depth);
        store.setLevel(store.level);
    }

    public void reportSolution() {
        if (costVariable != null)
            System.out.println ("Cost is " + costVariable);

        for (int i = 0; i < variablesToReport.length; i++)
            System.out.print (variablesToReport[i] + " ");

        System.out.println ();
        System.out.println("Search nodes: " + n_nodes);
        System.out.println("Wrong decisions: " + wrongDecisions);
        System.out.println ("\n---------------");
    }

    public void setVariablesToReport(IntVar[] v) {
        variablesToReport = v;
    }

    public void setCostVariable(IntVar v) {
        costVariable = v;
    }

    public class ChoicePoint {

        IntVar var;
        IntVar[] searchVariables;
        int value;

        public ChoicePoint (IntVar[] v) {
            var = selectVariable(v);
            value = selectValue(var);
        }

        public IntVar[] getSearchVariables() {
            return searchVariables;
        }

        /**
         * example variable selection; input order
         */
        IntVar selectVariable(IntVar[] v) {
            if (v.length != 0) {



                IntVar v_select;

                /*IntVar v_min = v[0];
                for (int i = 0; i < v.length; i++) {
                    if (v[i].min() < v_min.min()) {
                        v_min = v[i];
                    }
                }

                IntVar v_max = v[0];
                for (int i = 0; i < v.length; i++) {
                    if (v[i].max() > v_max.max()) {
                        v_max = v[i];
                    }
                }*/

                IntVar v_minDom = v[0];
                for (int i = 0; i < v.length; i++) {
                    if (v[i].getSize() < v_minDom.getSize()) {
                        v_minDom = v[i];
                    }
                }

                v_select = v_minDom;

                if (v_select.max() == v_select.min()) {
                    searchVariables = new IntVar[v.length - 1];
                    for (int i = 0; i < v.length - 1; i++) {
                        searchVariables[i] = v[i + 1];
                    }
                } else {
                    searchVariables = v;
                }

                return v_select;

            }
            else {
                System.err.println("Zero length list of variables for labeling");
                return new IntVar(store);
            }
        }

        int selectValue(IntVar v) {
            return (v.min() + v.max())/2;
        }

        public PrimitiveConstraint getConstraint() {
            return new XlteqC(var, value);
        }
    }
}

