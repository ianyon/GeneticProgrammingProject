package cl.ian.gp;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPTree;

import java.util.Arrays;

/**
 * Created by Ian on 26/01/2016.
 */
public class MyGPIndividual extends GPIndividual {

  public double[] evaluationError;

  public String fitnessAndTree() {
    return String.format("%s\n%s", fitness.fitnessToStringForHumans(), stringTreeForHumans());
  }

  public int depth() {
    return trees[0].child.depth();
  }

  public String depthAndSize() {
    return "Depth: " + depth() + " Size: " + size();
  }

  public String stringTreeForHumans() {
    if (trees[0].printStyle == GPTree.PRINT_STYLE_C) return trees[0].child.makeCTree(true,
        trees[0].printTerminalsAsVariablesInC, trees[0].printTwoArgumentNonterminalsAsOperatorsInC);
    else if (trees[0].printStyle == GPTree.PRINT_STYLE_LATEX) return trees[0].child.makeLatexTree();
    else if (trees[0].printStyle == GPTree.PRINT_STYLE_DOT) return trees[0].child.makeGraphvizTree();
    else return stringRootedTreeForHumans();
  }

  public String stringRootedTreeForHumans() {
    StringBuilder str = new StringBuilder();
    printRootedTreeForHumans(trees[0].child, str, 0, 0);
    return str.toString();
  }

  private static int printRootedTreeForHumans(GPNode child, StringBuilder str, int tablevel, int printbytes) {
    if (printbytes > GPNode.MAXPRINTBYTES) {
      str.append("\n");
      tablevel++;
      printbytes = 0;
      for (int x = 0; x < tablevel; x++)
        str.append(GPNode.GPNODEPRINTTAB);
    }

    if (child.children.length > 0) {
      str.append(" (");
      printbytes += 2;
    } else {
      str.append(" ");
      printbytes += 1;
    }

    String n = child.toStringForHumans();
    printbytes += n.length();
    str.append(n);

    for (int x = 0; x < child.children.length; x++)
      printbytes = printRootedTreeForHumans(child.children[x], str, tablevel, printbytes);
    if (child.children.length > 0) {
      str.append(")");
      printbytes += 1;
    }
    return printbytes;
  }


  @Override
  public void printTrees(EvolutionState state, int log) {
    trees[0].printTreeForHumans(state, log);
  }

  @Override
  public void printIndividualForHumans(final EvolutionState state, final int log) {
    if (!evaluated)
      state.output.println("Not evaluated!", log);
    fitness.printFitnessForHumans(state, log);
    printTrees(state, log);
  }

  public static MyGPIndividual getBest(Individual bestInd, Individual newInd) {
    if (bestInd == null || newInd.fitness.betterThan(bestInd.fitness))
      return (MyGPIndividual) newInd.clone();
    return (MyGPIndividual) bestInd;
  }

  public static MyGPIndividual getErrorBest(Individual best, Individual ind) {
    if (best == null || ((HitLevelKozaFitness) ind.fitness).errorBetterThan(best.fitness))
      return (MyGPIndividual) ind.clone();
    return (MyGPIndividual) best;
  }

  public void setEvaluationErrorSize(int errorArraySize) {
    this.evaluationError = new double[errorArraySize];
  }

  public void setEvaluationError(int i, double error) {
    evaluationError[i] = error;
  }

  public String printEvaluationError() {
    return Arrays.toString(evaluationError);
  }
}
