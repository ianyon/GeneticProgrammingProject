package cl.ian.gp;

import cl.ian.Case;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPTree;

/**
 * Created by Ian on 26/01/2016.
 */
public class MyGPIndividual extends GPIndividual {

  public String stringRootedTreeForHumans() {
    StringBuilder str = new StringBuilder();
    printRootedTreeForHumans(trees[0].child, str, 0, 0);
    return str.toString();
  }

  public String fitnessAndTree() {
    return String.format("%s\n%s", fitness.fitnessToStringForHumans(), stringRootedTreeForHumans());
  }

  public int depth() {
    return trees[0].child.depth();
  }

  public String depthAndSize() {
    return "Depth: " + depth() + " Size: " + size();
  }

  public static MyGPIndividual getBest(Individual bestInd, Individual newInd) {
    if (bestInd == null || newInd.fitness.betterThan(bestInd.fitness))
      return (MyGPIndividual) newInd.clone();
    return (MyGPIndividual) bestInd;
  }

  public int printRootedTreeForHumans(GPNode child, StringBuilder str, int tablevel, int printbytes) {
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


  public String stringTreeForHumans() {
    if (trees[0].printStyle == GPTree.PRINT_STYLE_C) return trees[0].child.makeCTree(true,
        trees[0].printTerminalsAsVariablesInC, trees[0].printTwoArgumentNonterminalsAsOperatorsInC);
    else if (trees[0].printStyle == GPTree.PRINT_STYLE_LATEX) return trees[0].child.makeLatexTree();
    else if (trees[0].printStyle == GPTree.PRINT_STYLE_DOT) return trees[0].child.makeGraphvizTree();
    else return "Select a PrintStyle";
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
}
