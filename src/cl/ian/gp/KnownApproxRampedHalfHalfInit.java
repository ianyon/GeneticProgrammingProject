package cl.ian.gp;

import cl.ian.gp.nodes.MeanReynoldsTerminal;
import cl.ian.gp.nodes.MyERC;
import cl.ian.gp.nodes.Power;
import ec.EvolutionState;
import ec.app.regression.func.Mul;
import ec.gp.*;
import ec.gp.koza.GPKozaDefaults;
import ec.gp.koza.HalfBuilder;
import ec.util.Parameter;

/**
 * Created by Ian on 15/01/2016.
 * <p>
 * Method used to initialize the individuals of a population. Half the individuals are initialized using some
 * known structure and the other half are initialized using Ramped-Half-Half
 */
public class KnownApproxRampedHalfHalfInit extends HalfBuilder {

  public static final String P_KNOWNHALFBUILDER = "known-half-builder";
  public static final String P_USEKNOWNAPPROX = "use-known-approx";

  public enum Case {
    FRICTION_FACTOR("FrictionFactor"),
    DRAG_COEFFICIENT("DragCoefficient"),
    NUSSELT_NUMBER("NusseltNumber");

    public final String text;

    Case(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }
  }

  // For statistics only
  public int growCount, fullCount, knownApproxCount;

  public static Case selectedCase;

  public Parameter defaultBase() {
    return GPKozaDefaults.base().push(P_KNOWNHALFBUILDER);
  }

  public void setup(final EvolutionState state, final Parameter base) {
    super.setup(state, base);

    Parameter def = defaultBase();

    String knownApprox = state.parameters.getString(base.push(P_USEKNOWNAPPROX), def.push(P_USEKNOWNAPPROX));
    if (knownApprox.equalsIgnoreCase(Case.FRICTION_FACTOR.text))
      selectedCase = Case.FRICTION_FACTOR;
    else if (knownApprox.equalsIgnoreCase(Case.DRAG_COEFFICIENT.text))
      selectedCase = Case.DRAG_COEFFICIENT;
    else if (knownApprox.equalsIgnoreCase(Case.NUSSELT_NUMBER.text))
      selectedCase = Case.NUSSELT_NUMBER;
    else
      state.output.fatal("Improperly selected the case for known approximation. Programming error");
  }

  public GPNode newRootedTree(final EvolutionState state,
                              final GPType type,
                              final int thread,
                              final GPNodeParent parent,
                              final GPFunctionSet set,
                              final int argposition,
                              final int requestedSize) {

    // First half use known approximation
    if (state.random[thread].nextDouble() < 0.5) {
      knownApproxCount++;
      switch (selectedCase) {
        case FRICTION_FACTOR:
          return knownApproximationFrictionFactor(state, type, thread, parent, argposition, set);
        case DRAG_COEFFICIENT:
          return knownApproximationDragCoefficient(state, type, thread, parent, argposition, set);
        case NUSSELT_NUMBER:
          return knownApproximationNusseltNumber(state, type, thread, parent, argposition, set);
        default:
          state.output.fatal("Inexistent known approximation case. Programming error");
      }
    }

    final int max = state.random[thread].nextInt(maxDepth - minDepth + 1) + minDepth;
    // The other half use Ramped Half Half
    if (state.random[thread].nextDouble() < pickGrowProbability) {
      growCount++;
      return growNode(state, 0, max, type, thread, parent, argposition, set);
    } else {
      fullCount++;
      return fullNode(state, 0, max, type, thread, parent, argposition, set);
    }
  }

  private GPNode baseKnownApproximation(EvolutionState state, GPType type, int thread,
                                        GPNodeParent parent, int argposition, GPFunctionSet set, double C1, double C2) {
    int t = type.type;
    GPNode[] terminals = set.terminals[t];
    GPNode[] nodes = set.nodes[t];

    if (nodes.length == 0)
      errorAboutNoNodeWithType(type, state);   // total failure

    // This tree is  C1 * (Rem(i)^C2)
    GPNode root = returnNode(nodes, Mul.class, state);
    root.resetNode(state, thread);  // give ERCs a chance to randomize
    root.argposition = (byte) argposition;
    root.parent = parent;

    GPNode firstChild = returnNode(terminals, MyERC.class, state);
    ((MyERC) firstChild).value = C1;

    GPNode secondChild = returnNode(nodes, Power.class, state);
    secondChild.resetNode(state, thread);
    {
      GPNode secondFirstChild = returnNode(terminals, MeanReynoldsTerminal.class, state);
      secondFirstChild.resetNode(state, thread);

      GPNode secondSecondChild = returnNode(terminals, MyERC.class, state);
      ((MyERC) secondSecondChild).value = C2;

      assignChilds(secondChild, secondFirstChild, secondSecondChild);
    }
    assignChilds(root, firstChild, secondChild);

    return root;
  }

  private GPNode knownApproximationFrictionFactor(EvolutionState state, GPType type, int thread,
                                                  GPNodeParent parent, int argposition, GPFunctionSet set) {
    final int C1 = state.random[thread].nextInt(40 + 1) + 1;
    final double C2 = -state.random[thread].nextDouble(false, false);
    return baseKnownApproximation(state, type, thread, parent, argposition, set, C1, C2);
  }

  private GPNode knownApproximationDragCoefficient(EvolutionState state, GPType type, int thread,
                                                   GPNodeParent parent, int argposition, GPFunctionSet set) {
    final double C1 = 2 + 3 * state.random[thread].nextDouble(false, false);
    final double C2 = state.random[thread].nextDouble(false, false);
    return baseKnownApproximation(state, type, thread, parent, argposition, set, C1, C2);
  }

  private GPNode knownApproximationNusseltNumber(EvolutionState state, GPType type, int thread,
                                                 GPNodeParent parent, int argposition, GPFunctionSet set) {
    int t = type.type;
    GPNode[] terminals = set.terminals[t];
    GPNode[] nodes = set.nodes[t];

    if (nodes.length == 0)
      errorAboutNoNodeWithType(type, state);   // total failure

    final double C1 = state.random[thread].nextDouble(false, false);
    final double C2 = state.random[thread].nextDouble(false, false);

    // This tree is times( times( C1, mypower( Rem(i), C2 ) ), times( mypower( 0.713, 0.37 ), mypower( 1.0348, 0.25 ) ) )
    // This tree is  ( C1 * Rem(i)^C2 ) * 0.713^0.37 * 1.0348^0.25
    GPNode root = returnNode(nodes, Mul.class, state);
    root.resetNode(state, thread);  // give ERCs a chance to randomize
    root.argposition = (byte) argposition;
    root.parent = parent;

    /***************** The first child: times( C1, mypower( Rem(i), C2 ) )****************/
    GPNode firstChild = returnNode(nodes, Mul.class, state);
    firstChild.resetNode(state, thread);
    assignChildInPosition(root, firstChild, 0);
    {
      GPNode firstFirstChild = returnNode(terminals, MyERC.class, state);
      ((MyERC) firstFirstChild).value = C1;

      GPNode firstSecondChild = returnNode(nodes, Power.class, state);
      firstSecondChild.resetNode(state, thread);
      {
        GPNode firstSecondFirstChild = returnNode(terminals, MeanReynoldsTerminal.class, state);
        firstSecondFirstChild.resetNode(state, thread);

        GPNode firstSecondSecondChild = returnNode(terminals, MyERC.class, state);
        ((MyERC) firstSecondSecondChild).value = C2;

        assignChilds(firstSecondChild, firstSecondFirstChild, firstSecondSecondChild);
      }
      // Assign the C1 and power of the multiplication
      assignChilds(firstChild, firstFirstChild, firstSecondChild);
    }
    /***** The second child: times( mypower( 0.713, 0.37 ), mypower( 1.0348, 0.25 ) )*******/
    // These powers can be simplified and the simplified expression is
    // 0,88235454289901685036206112230882 * 1,0085887160197468530750807258722
    // = 0,88993283549671004828779226160909
    // We wont simplify the expression to allow the GP algorithm to mutate this expression
    GPNode secondChild = returnNode(nodes, Mul.class, state);
    secondChild.resetNode(state, thread);
    { // First child of the multiplication
      GPNode secondFirstChild = returnNode(nodes, Power.class, state);
      secondFirstChild.resetNode(state, thread);
      {
        // First child of the power
        GPNode secondFirstFirstChild = returnNode(terminals, MyERC.class, state);
        ((MyERC) secondFirstFirstChild).value = 0.713;
        // Second child of the power
        GPNode secondFirstSecondChild = returnNode(nodes, MyERC.class, state);
        ((MyERC) secondFirstFirstChild).value = 0.37;

        assignChilds(secondFirstChild, secondFirstFirstChild, secondFirstSecondChild);
      }
      // Second child of the multiplication
      GPNode secondSecondChild = returnNode(nodes, Power.class, state);
      secondSecondChild.resetNode(state, thread);
      {
        // First child of the power
        GPNode secondSecondFirstChild = returnNode(terminals, MyERC.class, state);
        ((MyERC) secondSecondFirstChild).value = 1.0348;
        // Second child of the power
        GPNode secondSecondSecondChild = returnNode(nodes, MyERC.class, state);
        ((MyERC) secondSecondSecondChild).value = 0.25;

        assignChilds(secondSecondChild, secondSecondFirstChild, secondSecondSecondChild);
      }
      // Assign the multiplication second term with two powers
      assignChilds(secondChild, secondFirstChild, secondSecondChild);
    }
    /*************************************************************************************/

    assignChilds(root, firstChild, secondChild);
    return root;
  }

  private void assignChilds(GPNode parent, GPNode child1, GPNode child2) {
    assignChildInPosition(parent, child1, 0);
    assignChildInPosition(parent, child2, 1);
  }

  private void assignChildInPosition(GPNode parent, GPNode currentNode, int position) {
    currentNode.argposition = (byte) position;
    currentNode.parent = parent;
    parent.children[position] = currentNode;
  }

  private static GPNode returnNode(GPNode[] nodes, Class id, EvolutionState state) {
    for (GPNode node : nodes) {
      if (node.getClass() == id)
        return node.lightClone();
    }
    state.output.fatal("Custom initialization doesn't found required node");

    // Unreachable. It's used to calm the static checker only
    return new MyERC();
  }

}