package cl.ian;

import cl.ian.loopsteps.*;
import ec.EvolutionState;
import ec.Evolve;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

public class Main {

  public static void main(String[] args) {

    ParameterDatabase frictionDatabase = Evolve.loadParameterDatabase(new String[]{"-file", "friction_factor.params"});
    EvolutionState frictionState = Evolve.initialize(frictionDatabase, 0);

    ArrayList<LoopCallable> loopSteps = LoopCallable.populateLoops(frictionDatabase, frictionState);
    System.out.println("Friction factor, number of loops to run: " + LoopCallable.totalChainedLoops(loopSteps));
    long startFrictionTime = System.nanoTime();
    LoopCallable.InitiateLoops(loopSteps);
    System.out.println("Finished Friction Factor (" + (System.nanoTime() - startFrictionTime) / 1000000000.0 + " s)");


    ParameterDatabase dragDatabase = Evolve.loadParameterDatabase(new String[]{"-file", "drag_coef.params"});
    EvolutionState dragState = Evolve.initialize(dragDatabase, 0);
    loopSteps = LoopCallable.populateLoops(dragDatabase, dragState);
    System.out.println("Friction factor, number of loops to run: " + LoopCallable.totalChainedLoops(loopSteps));
    startFrictionTime = System.nanoTime();
    LoopCallable.InitiateLoops(loopSteps);
    System.out.println("Finished Drag Coefficient (" + (System.nanoTime() - startFrictionTime) / 1000000000.0 + " s)");

    ParameterDatabase nusseltDatabase = Evolve.loadParameterDatabase(new String[]{"-file", "nusselt_number.params"});
    EvolutionState nusseltState = Evolve.initialize(nusseltDatabase, 0);
    loopSteps = LoopCallable.populateLoops(nusseltDatabase, nusseltState);
    System.out.println("Friction factor, number of loops to run: " + LoopCallable.totalChainedLoops(loopSteps));
    startFrictionTime = System.nanoTime();
    LoopCallable.InitiateLoops(loopSteps);
    System.out.println("Finished nusselt Number(" + (System.nanoTime() - startFrictionTime) / 1000000000.0 + " s)");
  }


}
