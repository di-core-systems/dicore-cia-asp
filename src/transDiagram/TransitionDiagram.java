package transDiagram;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan Brümmer on 10.06.2017.
 * This class represents the final step in our conversion.  All actions and states in our WorkflowGraph are
 * now directly translated to ASP code.
 */
public class TransitionDiagram {
    private final List<Fluent> fluents;
    private final List<Action> actions;
    private final List<State> states;
    private final List<State> startingStates;
    private File f;

    public TransitionDiagram(List<Fluent> fluents, List<Action> actions, List<State> states,
                                List<State> startingStates) {
        this.fluents = fluents;
        this.actions = actions;
        this.states = states;
        this.startingStates = startingStates;
    }

    public File createASPCode(){

        f = new File("../ASP/logic_programs/new.lp");

        if(!(f.exists() && !f.isDirectory())){
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileWriter w = new FileWriter(f);
            // Step 1: DEFINITION OF FLUENTS
            // For this example graph I'm treating all fluents as inertial
            // But it is necessary to check if a fluent is really inertial or not.

            int n = this.getStates().size();
            w.write("#const n = " + n + ".\n");
            w.write("step(0..n).\n");

            w.write("\n");

            for(Fluent fluent: fluents){
                String name = fluent.getName();

                if(fluent.isInertial()){
                    w.write("_n_fluent(inertial, " + name + ").\n");

                    // INERTIA AXIOM FOR FLUENTS
                    w.write("_n_holds(" + name + ",I+1) :- \n" +
                            "           _n_fluent(inertial, " + name + "), \n" +
                            "           _n_holds(" + name + ",I),\n" +
                            "           not -_n_holds(" + name + ",I+1), step(I).\n");

                    w.write("-_n_holds(" + name + ",I+1) :- \n" +
                            "           _n_fluent(inertial, " + name + "), \n" +
                            "           -_n_holds(" + name + ",I),\n" +
                            "           not _n_holds(" + name + ",I+1), step(I).\n");
                } else {
                    w.write("_n_fluent(defined" + name + ").\n");

                    // CWA FOR FLUENTS
                    w.write("-_n_holds(" + name + ",I+1) :- \n" +
                            "           _n_fluent(defined" + name + "), \n" +
                            "           not holds(" + name + ",I), step(I).\n");
                }

                w.write("\n");
            }



            // DEFINITION OF ACTIONS.
            for(Action a: actions){
                w.write("_n_action(" + a.getName() +").\n");

                // CWA FOR ACTIONS
                w.write("-_n_occurs(" + a.getName() +",I) :-" +
                        " not _n_occurs(" + a.getName() + ",I), step(I).\n");

                w.write("\n");
            }

            // Step 3: Get all Fluents from all starting states
            // and define their holds-Attribute for timestamp 0.
            for(State start: startingStates){
                for(Fluent fluent: start.getFluents()) {
                    if(fluent.getValue()){
                        w.write("_n_holds(" + fluent.getName() + ",0).\n");
                    } else {
                        w.write("-_n_holds(" + fluent.getName() + ",0).\n");
                    }
                }
            }

            w.write("\n");

            // Step 4: Search through the graph via broad search.
            // Write ASP Codeblocks for each state and its prede- and successors.

            int i = 0;
            List<State> statesToVisit = new ArrayList<>();
            List<State> nextVisit = new ArrayList<>();
            List<State> visitedStates = new ArrayList<>();

            for(State startState: startingStates){
                for(Action startAction: startState.getOutgoingActions()){
                    statesToVisit.add(startAction.getEndState());
                }
                states.remove(startState);
            }

            while(true){
                for(State state: statesToVisit){
                    if(visitedStates.contains(state)){
                        continue;
                    }

                    // Standard ASP Codeblock for each predecessor (causal law).
                    if(state.getIngoingActions().isEmpty()){
                        w.write("_n_holds(" + state.getName() + ",T+1) :- \n");
                        w.write("           _n_occurs(do" + state.getName() + ", T).\n\n");
                    } else {
                        for(Action a: state.getIngoingActions()){
                            w.write("_n_holds(" + state.getName() + ",T+1) :- \n");

                            // The state "start" is merely used as an orientation where the workflow starts.
                            // It has no further impact on the workflow.
                            if(!a.getStartState().getName().equals("start")){
                                w.write("           _n_holds(" + a.getStartState().getName() + ",T),\n");
                            }

                            w.write("          -_n_holds(" + state.getName() + ",T),\n");
                            w.write("           _n_occurs(do" + state.getName() + ",T).\n\n");
                        }
                    }

                    for(Action a: state.getOutgoingActions()){
                        nextVisit.add(a.getEndState());
                    }

                    visitedStates.add(state);

                    w.write("_n_occurs(do" + state.getName() + "," + i + ").\n\n");
                }

                // If nextVisit is empty
                if(nextVisit.isEmpty()){
                    break;
                }

                for(State newState: nextVisit){
                    statesToVisit.add(newState);
                }

                nextVisit.clear();
                i++;
            }

            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    public List<State> getStates() {
        return states;
    }

    public List<Action> getActions() {
        return actions;
    }

    public List<Fluent> getFluents() {
        return fluents;
    }
}
