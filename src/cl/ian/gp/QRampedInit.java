package cl.ian.gp;

import cl.ian.gp.nodes.MeanReynoldsTerminal;
import cl.ian.gp.nodes.Mul;
import cl.ian.gp.nodes.MyERC;
import cl.ian.gp.nodes.Power;
import ec.EvolutionState;
import ec.gp.*;
import ec.gp.koza.HalfBuilder;

/**
 * Created by Ian on 15/01/2016.
 */
public class QRampedInit extends HalfBuilder {
    public GPNode newRootedTree(final EvolutionState state,
                                final GPType type,
                                final int thread,
                                final GPNodeParent parent,
                                final GPFunctionSet set,
                                final int argposition,
                                final int requestedSize) {
        // First half use known approximation
        if (state.random[thread].nextDouble() < 0.5)
            return knownApproximation(state, type, thread, parent, argposition, set);

        final int max = state.random[thread].nextInt(maxDepth - minDepth + 1) + minDepth;
        // The other half use Ramped Half Half
        if (state.random[thread].nextDouble() < pickGrowProbability)
            return growNode(state, 0, max, type, thread, parent, argposition, set);
        else
            return fullNode(state, 0, max, type, thread, parent, argposition, set);
    }

    private GPNode knownApproximation(EvolutionState state, GPType type, int thread,
                                      GPNodeParent parent, int argposition, GPFunctionSet set) {
        int t = type.type;
        GPNode[] terminals = set.terminals[t];
        GPNode[] nodes = set.nodes[t];

        if (nodes.length == 0)
            errorAboutNoNodeWithType(type, state);   // total failure

        final int C1 = state.random[thread].nextInt(40 + 1) + 1;
        final double C2 = -state.random[thread].nextDouble(false, false);

        // This tree is  C1 * (Rem(i)^C2)
        GPNode n = returnNode(nodes, Mul.class, state);
        n.resetNode(state, thread);  // give ERCs a chance to randomize
        n.argposition = (byte) argposition;
        n.parent = parent;

        GPNode currentNode = returnNode(terminals, MyERC.class, state);
        ((MyERC) currentNode).value = C1;
        assignChildInPosition(n, currentNode, 0);

        GPNode firstParent = returnNode(nodes, Power.class, state);
        firstParent.resetNode(state, thread);
        assignChildInPosition(n, firstParent, 1);

        currentNode = returnNode(terminals, MeanReynoldsTerminal.class, state);
        currentNode.resetNode(state, thread);
        assignChildInPosition(firstParent, currentNode, 0);

        currentNode = returnNode(terminals, MyERC.class, state);
        ((MyERC) currentNode).value = C2;
        assignChildInPosition(firstParent, currentNode, 1);

        //%agrega los parametros nodeid nodes y maxid a cada nodo del arbol
        // no estoy seguro si estos parametros son parte de GPlab o fueron agregados
        //Ffriction_tree = q_params_tree(Ffriction_tree);

        return n;
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