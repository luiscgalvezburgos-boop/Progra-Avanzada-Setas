package es.usal.pa;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

//Run Configuration usada
//-main false -host 127.0.0.1 cliente1:es.usal.pa.AgenteCliente

public class AgenteCliente extends Agent {
	
	protected CyclicBehaviourCliente clasificar;
	private static final long serialVersionUID = 1L;
	private int contadorSetas = 0;
	private String conversacionId;
	private AID clasificador;
	
	public void setup () {
		clasificador = buscarClasificador();
		clasificar = new CyclicBehaviourCliente(this);
		addBehaviour(clasificar);
		iniciarSetas();
	}
	
	private AID buscarClasificador() {
		DFAgentDescription plantilla = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("clasificar");
		sd.addOntologies("ontologia");
		plantilla.addServices(sd);
		AID agenteEncontrado = null;
		while (agenteEncontrado == null) {
			try {
				DFAgentDescription [] arrResult = DFService.search(this, plantilla);
				
				
	            if (arrResult.length > 0) {
	                agenteEncontrado = arrResult[0].getName();
	                System.out.println("Clasificador encontrado: " + agenteEncontrado.getLocalName());
	            } else {
	                System.out.println("Esperando a que se registre el clasificador...");
	                doWait(1000);
	            }
			} catch (FIPAException e) {
				e.printStackTrace();
	            doWait(1000);
			}
		}
		return agenteEncontrado;
	}
	
	public void iniciarSetas() {
		contadorSetas++;
		conversacionId = getLocalName() + "-" + contadorSetas;
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(clasificador);
		
		msg.setOntology("ontologia");
		msg.setConversationId(conversacionId);
		send(msg);
	}
	
	public String getConversacionId() {
	    return conversacionId;
	}
}

class CyclicBehaviourCliente extends CyclicBehaviour{
	private static final long serialVersionUID = 1L;
	private AgenteCliente cliente;
	public CyclicBehaviourCliente(AgenteCliente agente) {
	    super(agente);
	    this.cliente = agente;
	}
	
	public void action() {

		
		MessageTemplate mt = MessageTemplate.and(
		        MessageTemplate.MatchConversationId(cliente.getConversacionId()),
		        MessageTemplate.MatchOntology("ontologia"));
		
		ACLMessage preguntas = myAgent.receive(mt);
		
		if (preguntas == null) {
			block();
			return;
		} else {
			String respuesta = preguntas.getContent();
			String prefijo = "[" + myAgent.getLocalName() + " | " + cliente.getConversacionId() + "] ";
			if (respuesta.equals("e") || respuesta.equals("p")) { 
				System.out.println("respuesta final: " + respuesta);
				myAgent.doWait(1000);
				cliente.iniciarSetas();
			} else {
				String eleccion = Math.random() < 0.5 ? "si" : "no";
				System.out.println(prefijo + "Pregunta: " + respuesta + " -> Respondo: " + eleccion);
				ACLMessage responde = preguntas.createReply();
				responde.setPerformative(ACLMessage.INFORM);
				responde.setContent(eleccion);
				myAgent.send(responde);
			}
			
		}	
	}
}

