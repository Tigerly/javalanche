/*
* Copyright (C) 2011 Saarland University
* 
* This file is part of Javalanche.
* 
* Javalanche is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Javalanche is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser Public License for more details.
* 
* You should have received a copy of the GNU Lesser Public License
* along with Javalanche.  If not, see <http://www.gnu.org/licenses/>.
*/
package de.unisb.cs.st.javalanche.mutation.mutationPossibilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.unisb.cs.st.javalanche.mutation.results.Mutation;

public class Mutations implements Iterable<Mutation> {

	private Map<String, Map<Integer, List<Mutation>>> mutationMap = new HashMap<String, Map<Integer, List<Mutation>>>();

	private List<Mutation> muationList;

	public Mutations(List<Mutation> possibilities) {
		super();
		this.muationList = possibilities;
		for (Mutation mutation : possibilities) {
			Map<Integer, List<Mutation>> classPossibilityMap = mutationMap
					.get(mutation.getClassName());
			if (classPossibilityMap == null) {
				classPossibilityMap = new HashMap<Integer, List<Mutation>>();
				mutationMap.put(mutation.getClassName(), classPossibilityMap);
			}
			List<Mutation> lineList = classPossibilityMap.get(mutation
					.getLineNumber());
			if (lineList == null) {
				lineList = new ArrayList<Mutation>();
				classPossibilityMap.put(mutation.getLineNumber(), lineList);
			}
			lineList.add(mutation);
		}
	}

	public boolean contains(String className, int lineNumber) {
		if(className.contains("/")){
			throw new IllegalArgumentException("class name contains / "+ className);
		}
		if (mutationMap.containsKey(className)) {
			Map<Integer, List<Mutation>> classPossibilityMap = mutationMap
					.get(className);
			if (classPossibilityMap.containsKey(lineNumber)) {
				return true;
			}
		}
		return false;
	}

	public Mutation get(String className, int lineNumber) {
		if(className.contains("/")){
			throw new IllegalArgumentException("class name contains / "+ className);
		}
		if (mutationMap.containsKey(className)) {
			Map<Integer, List<Mutation>> classPossibilityMap = mutationMap
					.get(className);
			if (classPossibilityMap.containsKey(lineNumber)) {
				return classPossibilityMap.get(lineNumber).get(0);
			}
		}
		return null;
	}


	public boolean containsClass(String className) {
		return mutationMap.containsKey(className);
	}

	public Iterator<Mutation> iterator() {
		return muationList.iterator();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String className : mutationMap.keySet()) {
			sb.append("Mutations for class" + className);
			sb.append('\n');
			Map<Integer, List<Mutation>> tempMap = mutationMap
					.get(className);
			for (Integer i : tempMap.keySet()) {
				sb.append("in line: " + i);
				sb.append('\n');
				for (Mutation mutation : tempMap.get(i)) {
					sb.append(mutation.toString());
					sb.append('\n');
				}
			}
		}
		return sb.toString();
	}

}
