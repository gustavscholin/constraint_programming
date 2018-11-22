package introductory_exercises;

import org.jacop.constraints.*;
import org.jacop.floats.constraints.LinearFloat;
import org.jacop.floats.core.FloatVar;
import org.jacop.search.*;
import org.jacop.core.*;

public class GoodBurger {

    public static void main(String[] args) {
        Store store = new Store();

        int nbrOfIngredients  = 8;

        String[] ingredients = {"Beef Patty", "Bun", "Cheese", "Onions", "Pickles", "Lettuce", "Ketchup", "Tomato"};
        int[] sodium = {50, 330, 310, 1, 260, 3, 160, 3};
        int[] fat = {17, 9, 6, 2, 0, 0, 0, 0};
        int[] cal = {220, 260, 70, 10, 5, 4, 20, 9};
        int[] costs = {-25, -15, -10, -9, -3, -4, -2, -4};

        IntVar[] v = new IntVar[nbrOfIngredients];

        for (int i = 0; i < nbrOfIngredients; i++) {
            v[i] = new IntVar(store, ingredients[i], 1, 5);
        }

        store.impose(new LinearInt(v, sodium, "<", 3000));
        store.impose(new LinearInt(v, fat, "<", 150));
        store.impose(new LinearInt(v, cal, "<", 3000));

        store.impose(new XeqY(v[5], v[6]));
        store.impose(new XeqY(v[7], v[4]));

        IntVar maxBurgerCost = new IntVar(store, "cost", Integer.MIN_VALUE, 0);

        store.impose(new LinearInt(v, costs, "==", maxBurgerCost));

        //boolean result = store.consistency();

        //System.out.println(result);

        Search<IntVar> label = new DepthFirstSearch<IntVar>();

        label.setPrintInfo(true);

        SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(v, null, new IndomainMin<IntVar>());

        boolean result = label.labeling(store, select, maxBurgerCost);

        double burgerCost = maxBurgerCost.value()/(-100.0);

        System.out.println("The maximum cost of the burger is " + burgerCost + " dollars.");
    }
}
