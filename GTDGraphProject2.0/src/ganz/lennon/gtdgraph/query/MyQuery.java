package ganz.lennon.gtdgraph.query;

import java.io.IOException;
import java.util.*;

import ganz.lennon.gtdgraph.*;
import ganz.lennon.gtdgraph.search.SubgraphIsomorphism;

import javax.management.Query;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;

public class MyQuery extends Query {

	DirectedGraph<PropertyVertex, PropertyEdge> g;

	Map<String, Map<?, HashSet<PropertyVertex>>> indexes;
	HashMap<Long, PropertyVertex> mainIndex;

	public MyQuery(DirectedGraph<PropertyVertex, PropertyEdge> g) {
		this.g = g;
	}

	public HashSet<PropertyVertex> verticesWithPropertyAndValue(String key,
			Object value, Set<PropertyVertex> vertices) {
		HashSet<PropertyVertex> matches = new HashSet<PropertyVertex>(100);
		for (PropertyVertex v : vertices) {
			if (v.hasProperty(key))
				if (v.getValue(key).equals(value))
					matches.add(v);
		}
		return matches;
	}

	public void lineQuery(Property p1, Property p2, Property p3, 
			String edge12Label, String edge23Label){
		
		DirectedGraph<PropertyVertex, PropertyEdge> queryGraph = new DefaultDirectedGraph<PropertyVertex, PropertyEdge>(
				new ClassBasedEdgeFactory<PropertyVertex, PropertyEdge>(
						PropertyEdge.class));
		HashMap<PropertyVertex, HashSet<PropertyVertex>> ansMap = new HashMap<PropertyVertex, HashSet<PropertyVertex>>();
		
		PropertyVertex v1, v2, v3;
		PropertyEdge e12, e23;
		
		v1 = new PropertyVertex(1);
		v1.addProperty(p1.getKey(), p1.getValue());
		queryGraph.addVertex(v1);
		v2 = new PropertyVertex(2);
		v2.addProperty(p2.getKey(), p2.getValue());
		queryGraph.addVertex(v2);
		v3 = new PropertyVertex(3);
		v3.addProperty(p3.getKey(), p3.getValue());
		queryGraph.addVertex(v3);
		e12 = queryGraph.addEdge(v1, v2);
		e12.addLabel(edge12Label);
		e23 = queryGraph.addEdge(v2, v3);
		e23.addLabel(edge23Label);
		testIso(queryGraph, ansMap, mainIndex);

	}
	
	public void triangleQuery(){}

	public HashSet<PropertyVertex> getVerticesByValue(String key, Object value) {
		if (indexes.containsKey(key))
			return indexes.get(key).get(value);
		else
			return new HashSet<PropertyVertex>();
	}

	public void importIndex(String indexedOn,
			Map<Object, HashSet<PropertyVertex>> index) {
		indexes.put(indexedOn, index);
	}

	private void testIso(
			DirectedGraph<PropertyVertex, PropertyEdge> queryGraph,
			HashMap<PropertyVertex, HashSet<PropertyVertex>> ansMap,
			HashMap<Long, PropertyVertex> mainIndex) {
		try {
			SubgraphIsomorphism si = new SubgraphIsomorphism(queryGraph,
					this.g, mainIndex);
			si.setAnsMap(ansMap);
			si.computeMatch();
			si.displayIsos();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
