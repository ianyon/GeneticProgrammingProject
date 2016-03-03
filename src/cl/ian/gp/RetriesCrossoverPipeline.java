/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package cl.ian.gp;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.koza.CrossoverPipeline;
import ec.util.Parameter;

/* 
 * RetriesCrossoverPipeline.java
 */


/**
 * CrossoverPipeline is a GPBreedingPipeline which performs a strongly-typed
 * version of
 * Koza-style "Subtree Crossover".  Two individuals are selected,
 * then a single tree is chosen in each such that the two trees
 * have the same GPTreeConstraints.  Then a random node is chosen
 * in each tree such that each node's return type is type-compatible
 * with the argument type of the slot in the parent which contains
 * the other node.
 * If by swapping subtrees at these nodes the two trees will not
 * violate maximum depth constraints, then the trees perform the
 * swap, otherwise, they repeat the hunt for random nodes.
 * <p>
 * <p>The pipeline tries at most <i>tries</i> times to a pair
 * of random nodes BOTH with valid swap constraints.  If it
 * cannot find any such pairs after <i>tries</i> times, it
 * uses the pair of its last attempt.  If either of the nodes in the pair
 * is valid, that node gets substituted with the other node.  Otherwise
 * an individual invalid node isn't changed at all (it's "reproduced").
 * <p>
 * <p><b>Compatibility with constraints.</b>
 * Since Koza-I/II only tries 1 time, and then follows this policy, this is
 * compatible with Koza.  lil-gp either tries 1 time, or tries forever.
 * Either way, this is compatible with lil-gp.  My hacked
 * <a href="http://www.cs.umd.edu/users/seanl/gp/">lil-gp kernel</a>
 * either tries 1 time, <i>n</i> times, or forever.  This is compatible
 * as well.
 * <p>
 * <p>This pipeline typically produces up to 2 new individuals (the two newly-
 * swapped individuals) per produce(...) call.  If the system only
 * needs a single individual, the pipeline will throw one of the
 * new individuals away.  The user can also have the pipeline always
 * throw away the second new individual instead of adding it to the population.
 * In this case, the pipeline will only typically
 * produce 1 new individual per produce(...) call.
 * <p>
 * <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 * 2 * minimum typical number of individuals produced by each source, unless tossSecondParent
 * is set, in which case it's simply the minimum typical number.
 * <p>
 * <p><b>Number of Sources</b><br>
 * 2
 * <p>
 * <p><b>Parameters</b><br>
 * <table>
 * <tr><td valign=top><i>base</i>.<tt>tries</tt><br>
 * <font size=-1>int &gt;= 1</font></td>
 * <td valign=top>(number of times to try finding valid pairs of nodes)</td></tr>
 * <p>
 * <tr><td valign=top><i>base</i>.<tt>maxdepth</tt><br>
 * <font size=-1>int &gt;= 1</font></td>
 * <td valign=top>(maximum valid depth of a crossed-over subtree)</td></tr>
 * <p>
 * <tr><td valign=top><i>base</i>.<tt>maxsize</tt><br>
 * <font size=-1>int &gt;= 1</font></td>
 * <td valign=top>(maximum valid size, in nodes, of a crossed-over subtree)</td></tr>
 * <p>
 * <tr><td valign=top><i>base</i>.<tt>tree.0</tt><br>
 * <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 * <td valign=top>(first tree for the crossover; if parameter doesn't exist, tree is picked at random)</td></tr>
 * <p>
 * <tr><td valign=top><i>base</i>.<tt>tree.1</tt><br>
 * <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 * <td valign=top>(second tree for the crossover; if parameter doesn't exist, tree is picked at random.  This tree <b>must</b> have the same GPTreeConstraints as <tt>tree.0</tt>, if <tt>tree.0</tt> is defined.)</td></tr>
 * <p>
 * <tr><td valign=top><i>base</i>.<tt>ns.</tt><i>n</i><br>
 * <font size=-1>classname, inherits and != GPNodeSelector,<br>
 * or String <tt>same<tt></font></td>
 * <td valign=top>(GPNodeSelector for parent <i>n</i> (n is 0 or 1) If, for <tt>ns.1</tt> the value is <tt>same</tt>, then <tt>ns.1</tt> a copy of whatever <tt>ns.0</tt> is.  Note that the default version has no <i>n</i>)</td></tr>
 * <p>
 * <tr><td valign=top><i>base</i>.<tt>toss</tt><br>
 * <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font>/td>
 * <td valign=top>(after crossing over with the first new individual, should its second sibling individual be thrown away instead of adding it to the population?)</td></tr>
 * </table>
 * <p>
 * <p><b>Default Base</b><br>
 * gp.koza.xover
 * <p>
 * <p><b>Parameter bases</b><br>
 * <table>
 * <tr><td valign=top><i>base</i>.<tt>ns.</tt><i>n</i><br>
 * <td>nodeselect<i>n</i> (<i>n</i> is 0 or 1)</td></tr>
 * </table>
 */

public class RetriesCrossoverPipeline extends CrossoverPipeline {
  private static final long serialVersionUID = 1;

  // Used for debugging
  //private static int equalTrees;
  //private static int equalNodes;
  //private static boolean breakTree;

  public void setup(final EvolutionState state, final Parameter base) {
    super.setup(state, base);

    // Used for debugging
    //equalTrees = equalNodes = 0;
  }

  /**
   * Returns true if inner1 can feasibly be swapped into inner2's position and if inner1 is different of inner2.
   */
  public boolean verifyPoints(final GPInitializer initializer, final GPNode inner1, final GPNode inner2) {
    return super.verifyPoints(initializer, inner1, inner2) && !inner1.rootedTreeEquals(inner2);
  }

  public int produce(final int min, final int max, final int start, final int subpopulation,
                     final Individual[] inds, final EvolutionState state, final int thread) {
    // how many individuals should we make?
    int n = typicalIndsProduced();
    n = Math.min(Math.max(n, min), max);

    // should we bother?
    if (!state.random[thread].nextBoolean(likelihood))
      return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

    GPInitializer initializer = ((GPInitializer) state.initializer);

    for (int q = start; q < n + start; /* no increment */) { // keep on going until we're filled up
      int[] treeIndex = getTreeIndex(subpopulation, state, thread, initializer);
      int t1 = treeIndex[0];
      int t2 = treeIndex[1];

      // prepare the nodeselectors
      nodeselect1.reset();
      nodeselect2.reset();

      // pick some nodes
      GPNode p1, p2;
      boolean[] validity = new boolean[2];
      GPNode[] validNodes = pickValidNodes(state, subpopulation, thread, t1, t2, initializer, q, n, validity, start);
      p1 = validNodes[0];
      p2 = validNodes[1];
      // validity results...
      boolean res1 = validity[0];
      boolean res2 = validity[1];

      // at this point, res1 AND res2 are valid, OR either res1 OR res2 is valid and we ran out of tries, OR
      // neither is valid and we ran out of tries.  So now we will transfer to a tree which has res1 or
      // res2 valid, otherwise it'll just get replicated.  This is compatible with both Koza and lil-gp.
      // at this point I could check to see if my sources were breeding pipelines -- but I'm too lazy to write
      // that code (it's a little complicated) to just swap one individual over or both over, -- it might still
      // entail some copying.  Perhaps in the future. It would make things faster perhaps, not requiring all that
      // cloning. Create some new individuals based on the old ones -- since GPTree doesn't deep-clone, this
      // should be just fine.  Perhaps we should change this to proto off of the main species prototype, but
      // we have to then copy so much stuff over; it's not worth it.

      q = addIndividual(inds, q, t1, res1, p1, p2);

      if (n - (q - start) >= 2 && !tossSecondParent) q = addIndividual(inds, q, t2, res2, p2, p1);

    }
    return n;
  }

  private int addIndividual(Individual[] inds, int q, int t, boolean res, GPNode p1, GPNode p2) {
    GPIndividual j = parents[0].lightClone();
    // Fill in various tree information that didn't get filled in there
    j.trees = new GPTree[parents[0].trees.length];
    // at this point, p1 or p2, or both, may be null. If not, swap one in.  Else just copy the parent.
    for (int x = 0; x < j.trees.length; x++) SwapNodesOrParent(t, res, p1, p2, j, x);
    // add the individuals to the population
    inds[q++] = j;
    return q;
  }

  private GPNode[] pickValidNodes(EvolutionState state, int subpopulation, int thread, int t1, int t2,
                                  GPInitializer initializer, int q, int n, boolean[] validity, int start) {
    GPNode p1 = null, p2 = null;
    boolean res1 = false, res2 = false;
    //int counter = 0;
    for (int x = 0; x < numTries; x++) {
      // pick a node in individual 1
      p1 = nodeselect1.pickNode(state, subpopulation, thread, parents[0], parents[0].trees[t1]);

      // pick a node in individual 2
      p2 = nodeselect2.pickNode(state, subpopulation, thread, parents[1], parents[1].trees[t2]);

      // check for depth and swap-compatibility limits
      res1 = verifyPoints(initializer, p2, p1);  // p2 can fill p1's spot -- order is important!
      res2 = n - (q - start) < 2 || tossSecondParent || verifyPoints(initializer, p1, p2);
      // p1 can fill p2's spot -- order is important!

      // did we get something that had both nodes verified?
      // we reject if EITHER of them is invalid.  This is what lil-gp does.
      // Koza only has numTries set to 1, so it's compatible as well.
      if (res1 && res2) break;

      //counter++;
    }

    // Used for debugging
    //if (counter == numTries) equalNodes++;

    validity[0] = res1;
    validity[1] = res2;
    return new GPNode[]{p1, p2};
  }

  private void grabIndividuals(int subpopulation, EvolutionState state, int thread) {
    // grab two individuals from our sources
    if (sources[0] == sources[1])  // grab from the same source
      sources[0].produce(2, 2, 0, subpopulation, parents, state, thread);
    else { // grab from different sources
      sources[0].produce(1, 1, 0, subpopulation, parents, state, thread);
      sources[1].produce(1, 1, 1, subpopulation, parents, state, thread);
    }
  }

  private int[] getTreeIndex(int subpopulation, EvolutionState state, int thread, GPInitializer initializer) {
    int t1 = 0, t2 = 0;
    for (int retries = 0; retries < numTries; retries++) {
      grabIndividuals(subpopulation, state, thread);

      // at this point, parents[] contains our two selected individuals
      // are our tree values valid?
      if (tree1 != TREE_UNFIXED && (tree1 < 0 || tree1 >= parents[0].trees.length))
        state.output.fatal("GP Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual");
      if (tree2 != TREE_UNFIXED && (tree2 < 0 || tree2 >= parents[1].trees.length))
        state.output.fatal("GP Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual");

      if (tree1 == TREE_UNFIXED || tree2 == TREE_UNFIXED) {
        do { // pick random trees  -- their GPTreeConstraints must be the same
          t1 = getSelectedTree(state, thread, parents[0], tree1);
          t2 = getSelectedTree(state, thread, parents[1], tree2);
        } while (parents[0].trees[t1].constraints(initializer) != parents[1].trees[t2].constraints(initializer));
      } else {
        t1 = tree1;
        t2 = tree2;
        // make sure the constraints are okay
        if (parents[0].trees[t1].constraints(initializer) != parents[1].trees[t2].constraints(initializer)) // uh oh
          state.output.fatal("GP Crossover Pipeline's two tree choices are both specified by the user -- but their GPTreeConstraints are not the same");
      }

      // Used for debugging
        /*
        if (!parents[0].trees[t1].treeEquals(parents[1].trees[t2])) {
          breakTree = true;
          break;
        }*/
    }

    // Used for debugging
    //if (!breakTree) equalTrees++;

    return new int[]{t1, t2};

  }

  protected int getSelectedTree(EvolutionState state, int thread, GPIndividual parent, int treeToSelect) {
    int selectedTree;
    if (treeToSelect == TREE_UNFIXED)
      if (parent.trees.length > 1)
        selectedTree = state.random[thread].nextInt(parent.trees.length);
      else
        selectedTree = 0;
    else
      selectedTree = tree1;
    return selectedTree;
  }

  protected void SwapNodesOrParent(int tree, boolean res, GPNode oldSubTree, GPNode newSubtree, GPIndividual ind, int treePos) {
    ind.trees[treePos] = parents[0].trees[treePos].lightClone();
    ind.trees[treePos].owner = ind;
    if (treePos == tree && res) { // we've got a tree with a kicking cross position!
      ind.trees[treePos].child = parents[0].trees[treePos].child.cloneReplacing(newSubtree, oldSubTree);
      ind.evaluated = false;
    }  // it's changed
    else {
      ind.trees[treePos].child = (GPNode) (parents[0].trees[treePos].child.clone());
    }
    ind.trees[treePos].child.parent = ind.trees[treePos];
    ind.trees[treePos].child.argposition = 0;
  }
}
