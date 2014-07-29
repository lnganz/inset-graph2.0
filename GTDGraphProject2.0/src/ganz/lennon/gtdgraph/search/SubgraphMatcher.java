package ganz.lennon.gtdgraph.search;

import ganz.lennon.gtdgraph.*;
import ganz.lennon.gtdgraph.query.MyQuery;

import java.io.IOException;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DirectedMultigraph;

public class SubgraphMatcher {

	private static final boolean OUTGOING = true;
	private static final boolean INCOMING = false;
	
	Map<String, Map<?, HashSet<PropertyVertex>>> indexes = 
			new HashMap<String, Map<?, HashSet<PropertyVertex>>>();
	
	HashMap<Long, PropertyVertex> mainIndex;
	
	DirectedGraph<PropertyVertex, PropertyEdge> g;
	
	public SubgraphMatcher(DirectedGraph<PropertyVertex, PropertyEdge> g, HashMap<Long, PropertyVertex> mainIndex){
		this.g = g;
		this.mainIndex = mainIndex;
	}
	
	public void test(){
		Scanner kb = new Scanner(System.in);
		String key1, key2, key3, value1, value2, value3, edge12, edge13, edge23;
		PropertyVertex v1 = new PropertyVertex(1), v2 = new PropertyVertex(2), v3 = new PropertyVertex(3);
		PropertyEdge e12, e13, e23;
		DirectedGraph<PropertyVertex, PropertyEdge> queryGraph = new DefaultDirectedGraph<PropertyVertex, PropertyEdge>(
				new ClassBasedEdgeFactory<PropertyVertex, PropertyEdge>(
						PropertyEdge.class));
		int intVal2;
		HashMap<PropertyVertex, HashSet<PropertyVertex>> ansMap = new HashMap<PropertyVertex, HashSet<PropertyVertex>>();
//		System.out.print("v1 key: ");
//		key1 = kb.nextLine().toUpperCase();
		key1 = "GROUP_NAME";
//		System.out.print("v1 value: ");
//		value1 = kb.nextLine();
		value1 = "Taliban";
		v1.addProperty(key1, value1);
		queryGraph.addVertex(v1);
		ansMap.put(v1, getVerticesByValue(key1, value1));
//		System.out.print("v2 key: ");
//		key2 = kb.nextLine().toUpperCase();
		key2 = "COUNTRY_CODE";
//		System.out.print("v2 value: ");
//		intVal2 = kb.nextInt();
		intVal2 = 4;
		v2.addProperty(key1, intVal2);
		queryGraph.addVertex(v2);
		ansMap.put(v2, getVerticesByValue(key2, intVal2));
//		System.out.println("v1->v2 label: ");
//		edge12 = kb.next().toUpperCase();
		edge12 = "PERPETRATED";
		queryGraph.addEdge(v1, v2).addLabel(edge12);
//		System.out.println("v3 key: ");
//		key3 = kb.next().toUpperCase();
		key3 = "CORPORATION_NAME";
//		System.out.println("v3 value: ");
//		value3 = kb.next();
		value3 = "School";
		v3.addProperty(key3, value3);
		queryGraph.addVertex(v3);
		ansMap.put(v3, getVerticesByValue(key3, value3));
//		System.out.println("v2->v3 label: ");
//		edge23 = kb.nextLine();
		edge23 = "TARGET_CORPORATION";
		queryGraph.addEdge(v2, v3).addLabel(edge23);
		
		try {
			SubgraphIsomorphism si = new SubgraphIsomorphism(queryGraph, this.g, mainIndex);
			si.setAnsMap(ansMap);
			si.computeMatch();
			si.displayIsos();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		kb.close();
	}
	
	public void testIsomorphism(){
		MyQuery query = new MyQuery(g);
		Scanner kb = new Scanner(System.in);
		String key, value, edge12, edge23;
		Property p1, p2, p3;
		System.out.print("Enter v1 key: ");
		key = kb.next();
		System.out.print("Enter v1 value: ");
		value = kb.nextLine();
		p1 = new Property(key, value);
		System.out.print("Enter v2 key: ");
		key = kb.next();
		System.out.print("Enter v2 value: ");
		value = kb.next();
		p2 = new Property(key, value);
		System.out.print("Enter v1->v2 Label: ");
		edge12 = kb.next();
		System.out.print("Enter v3 key: ");
		key = kb.next();
		System.out.print("Enter v3 value: ");
		value = kb.nextLine();
		p3 = new Property(key, value);
		System.out.print("Enter v2->v3 Label: ");
		edge23 = kb.next();
		query.lineQuery(p1, p2, p3, edge12, edge23);
		
	}
	
	public void importIndex(Map<Object, HashSet<PropertyVertex>> index, String indexedOn){
		indexes.put(indexedOn, index);
	}
	
	public void importMainIndex(HashMap<Long, PropertyVertex> mainIndex){
		this.mainIndex = mainIndex;
	}
	
	public HashSet<PropertyVertex> getVerticesByValue(String key, Object value){
		if (indexes.containsKey(key))
			return indexes.get(key).get(value);
		else
			return new HashSet<PropertyVertex>();
	}
	
//	public HashSet<PropertyEdge> getSuitableEdges(Set<PropertyVertex> set1, 
//			Set<PropertyVertex> set2, String edgeLabel){
//		HashSet<PropertyEdge> edgesToReturn = new HashSet<PropertyEdge>();
//		Set<PropertyEdge> edgesToExplore;
//		Set<PropertyVertex> adjacentVertices;
//		PropertyEdge edge;
//		if (set1.size() <= set2.size()){
//			for (PropertyVertex v1 : set1){
//				adjacentVertices = getAdjacentVertices(v1, OUTGOING);
//				for (PropertyVertex v2 : adjacentVertices){
//					edge = g.getEdge(v1,  v2);
//					if ((edge != null) && (edge.getLabels().contains(edgeLabel))){
//						edgesToReturn.add(edge);
//					}
//				}
//			}
//		}else{
//			for (PropertyVertex v1 : set2){
//				adjacentVertices = getAdjacentVertices(v1, INCOMING);
//				for (PropertyVertex v2 : adjacentVertices){
//					edge = g.getEdge(v1,  v2);
//					if ((edge != null) && (edge.getLabels().contains(edgeLabel))){
//						edgesToReturn.add(edge);
//					}
//				}
//			}
//		}
//			
//		return edgesToReturn;
//	}
	
	public void printMatches(Set<PropertyEdge> edgeset1, Set<PropertyEdge> edgeset2){
		
	}
	
	private Set<PropertyVertex> getAdjacentVertices(PropertyVertex rootV,
				boolean inOut) {
			Set<PropertyVertex> adjSet = new HashSet<PropertyVertex>();
			Set<PropertyEdge> edgeSet;
			if (inOut == OUTGOING) {
				edgeSet = g.outgoingEdgesOf(rootV);
				for (PropertyEdge e : edgeSet) {
					adjSet.add(g.getEdgeTarget(e));
				}
			} else {
				edgeSet = g.incomingEdgesOf(rootV);
				for (PropertyEdge e : edgeSet) {
					adjSet.add(g.getEdgeSource(e));
				}
			}
			return adjSet;
		}
}
