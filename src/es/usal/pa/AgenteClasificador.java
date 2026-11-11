package es.usal.pa;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import smile.classification.DecisionTree;
import smile.classification.DecisionTree.Options;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.io.Read;
import smile.model.cart.DecisionTreeNavegar2;
import smile.model.cart.SplitRule;

public class AgenteClasificador extends Agent {

    private static final long serialVersionUID = 1L;
    protected ParallelBehaviour compoundBehaviour;

    @Override
    public void setup() {
        System.out.println("Soy el agente " + getLocalName());

        DecisionTree decisionTree;
        try {
            DataFrame dataFrame = Read.arff("mushrooms.arff");
            dataFrame = dataFrame.dropna();
            Options options = new Options(SplitRule.ENTROPY,
                    Integer.MAX_VALUE, Integer.MAX_VALUE, 1);
            decisionTree = DecisionTree.fit(Formula.of("class~."), dataFrame, options);
        } catch (Exception e) {
            e.printStackTrace();
            doDelete();
            return;
        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName("Clasificador");
        sd.setType("clasificar");
        sd.addOntologies("ontologia");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
            doDelete();
            return;
        }

        compoundBehaviour = new ParallelBehaviour(this, ParallelBehaviour.WHEN_ALL);
        for (int i = 0; i < 4; i++) {
            compoundBehaviour.addSubBehaviour(new CyclicBehaviourClasificar(this, decisionTree));
        }
        addBehaviour(compoundBehaviour);
        System.out.println("Servicio clasificar registrado. Esperando solicitudes.");
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println("Agente " + getLocalName() + " finalizando.");
    }
}

class CyclicBehaviourClasificar extends CyclicBehaviour {

    private static final long serialVersionUID = 1L;
    private DecisionTree decisionTree;
    private DecisionTreeNavegar2 navegador;
    private AID cliente = null;
    private String conversationId;

    public CyclicBehaviourClasificar(Agent agente, DecisionTree decisionTree) {
        super(agente);
        this.decisionTree = decisionTree;
    }

    @Override
    public void action() {
        MessageTemplate mt;

        if (cliente == null) {
            mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchOntology("ontologia"));
            ACLMessage inicio = myAgent.receive(mt);
            if (inicio == null) {
                block();
                return;
            }

            cliente = inicio.getSender();
            conversationId = inicio.getConversationId();
            navegador = new DecisionTreeNavegar2(decisionTree);
            System.out.println("Inicio de clasificación: " + conversationId);
        } else {
            mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.and(MessageTemplate.MatchOntology("ontologia"),
                            MessageTemplate.and(MessageTemplate.MatchSender(cliente),
                                    MessageTemplate.MatchConversationId(conversationId))));
            ACLMessage respuesta = myAgent.receive(mt);
            if (respuesta == null) {
                block();
                return;
            }

            String texto = respuesta.getContent();
            navegador.navegarNodo(texto.equals("si"));
        }

        ACLMessage mensaje = new ACLMessage(ACLMessage.INFORM);
        mensaje.setSender(myAgent.getAID());
        mensaje.addReceiver(cliente);
        mensaje.setOntology("ontologia");
        mensaje.setConversationId(conversationId);

        if (navegador.esNodoDecision()) {
            String clasificacion = navegador.getClasificacion("class");
            mensaje.setContent(clasificacion);
            myAgent.send(mensaje);
            System.out.println("Clasificación " + conversationId + ": " + clasificacion);
            cliente = null;
        } else {
            mensaje.setContent(navegador.condicionActual());
            myAgent.send(mensaje);
        }
    }
}
