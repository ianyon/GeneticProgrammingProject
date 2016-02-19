package cl.ian;

import cl.ian.loopsteps.*;
import ec.EvolutionState;
import ec.Evolve;
import ec.app.edge.func.Loop;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

public class Main {

  public static void main(String[] args) {

    ParameterDatabase frictionDatabase = Evolve.loadParameterDatabase(new String[]{"-file", "friction_factor.params"});
    EvolutionState frictionState = Evolve.initialize(frictionDatabase, 0);

    ArrayList<LoopCallable> loopSteps = new ArrayList<>();
    loopSteps.add(new LoopPopulation(frictionDatabase, frictionState, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopCrossoverRate(frictionDatabase, frictionState, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopMaxInitialTreeDepth(frictionDatabase, frictionState, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopMaxTreeDepth(frictionDatabase, frictionState, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopDivMaxValue(frictionDatabase, frictionState, loopSteps, loopSteps.size()));

    System.out.println("Friction factor, number of loops to run: " + LoopCallable.totalChainedLoops(loopSteps));
    long startFrictionTime = System.nanoTime();
    try {
      loopSteps.get(0).call();
    } catch (Exception e) {
      System.out.println("Exception during loop call");
      e.printStackTrace();
      System.exit(-1);
    }
    System.out.println("Terminado factor de fricción (" + (System.nanoTime() - startFrictionTime) / 1000000000.0 + " s)");


    ParameterDatabase dragDatabase = Evolve.loadParameterDatabase(new String[]{"-file", "drag_coef.params"});
    ParameterDatabase nusseltDatabase = Evolve.loadParameterDatabase(new String[]{"-file", "nusselt_number.params"});
    EvolutionState dragState = Evolve.initialize(dragDatabase, 0);
    EvolutionState nusseltState = Evolve.initialize(nusseltDatabase, 0);
    /*double dragMeanTime;
    dragState.run(EvolutionState.C_STARTED_FRESH);
    Evolve.cleanup(dragState);
    dragMeanTime = (System.nanoTime() - startTime) / 1000000000.0;
    System.out.println("Terminado coeficiente de arrastre");

    double nusseltMeanTime;
    nusseltState.run(EvolutionState.C_STARTED_FRESH);
    Evolve.cleanup(nusseltState);
    nusseltMeanTime = (System.nanoTime() - startTime) / 1000000000.0;
    System.out.println("Terminado numero de Nusselt");

    System.out.println("Tiempo ejecución coeficiente de arrastre: " + dragMeanTime + " s");
    System.out.println("Tiempo ejecución número de nusselt: " + nusseltMeanTime + " s");*/
  }


}
