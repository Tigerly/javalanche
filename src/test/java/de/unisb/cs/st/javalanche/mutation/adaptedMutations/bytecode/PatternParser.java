//package de.unisb.cs.st.javalanche.mutation.adaptedMutations.bytecode;
//
//
//import org.jaxen.pattern.*;
//
//import java.util.Iterator;
//import java.util.List;
//import java.util.ListIterator;
//
//import org.jaxen.JaxenException;
//import org.jaxen.JaxenHandler;
//import org.jaxen.expr.DefaultAllNodeStep;
//import org.jaxen.expr.DefaultCommentNodeStep;
//import org.jaxen.expr.DefaultFilterExpr;
//import org.jaxen.expr.DefaultNameStep;
//import org.jaxen.expr.DefaultProcessingInstructionNodeStep;
//import org.jaxen.expr.DefaultStep;
//import org.jaxen.expr.DefaultTextNodeStep;
//import org.jaxen.expr.DefaultXPathFactory;
//import org.jaxen.expr.Expr;
//import org.jaxen.expr.FilterExpr;
//import org.jaxen.expr.LocationPath;
//import org.jaxen.expr.Predicate;
//import org.jaxen.expr.PredicateSet;
//import org.jaxen.expr.Step;
//import org.jaxen.expr.UnionExpr;
//import org.jaxen.saxpath.Axis;
//import org.jaxen.saxpath.XPathReader;
//import org.jaxen.saxpath.helpers.XPathReaderFactory;
//
///**
// * <code>PatternParser</code> is a helper class for parsing XSLT patterns
// * 
// * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
// */
//public class PatternParser {
//	private static final boolean TRACE = false;
//	private static final boolean USE_HANDLER = false;
//
//	public static Pattern parse(String text) throws JaxenException,
//			org.jaxen.saxpath.SAXPathException {
//		if (USE_HANDLER) {
//			XPathReader reader = XPathReaderFactory.createReader();
//			PatternHandler handler = new PatternHandler();
//
//			handler.setXPathFactory(new DefaultXPathFactory());
//			reader.setXPathHandler(handler);
//			reader.parse(text);
//
//			return handler.getPattern();
//		} else {
//			XPathReader reader = XPathReaderFactory.createReader();
//			JaxenHandler handler = new JaxenHandler();
//
//			handler.setXPathFactory(new DefaultXPathFactory());
//			reader.setXPathHandler(handler);
//			reader.parse(text);
//
//			Pattern pattern = convertExpr(handler.getXPathExpr().getRootExpr());
//			return pattern.simplify();
//		}
//	}
//
//	protected static Pattern convertExpr(Expr expr) throws JaxenException {
//		if (TRACE) {
//			System.out.println("Converting: " + expr + " into a pattern.");
//		}
//
//		if (expr instanceof LocationPath) {
//			return convertExpr((LocationPath) expr);
//		} else if (expr instanceof FilterExpr) {
//			LocationPathPattern answer = new LocationPathPattern();
//			answer.addFilter((FilterExpr) expr);
//			return answer;
//		} else if (expr instanceof UnionExpr) {
//			UnionExpr unionExpr = (UnionExpr) expr;
//			Pattern lhs = convertExpr(unionExpr.getLHS());
//			Pattern rhs = convertExpr(unionExpr.getRHS());
//			return new UnionPattern(lhs, rhs);
//		} else {
//			LocationPathPattern answer = new LocationPathPattern();
//			answer.addFilter(new DefaultFilterExpr(expr, new PredicateSet()));
//			return answer;
//		}
//	}
//
//	protected static LocationPathPattern convertExpr(LocationPath locationPath)
//			throws JaxenException {
//		LocationPathPattern answer = new LocationPathPattern();
//		// answer.setAbsolute( locationPath.isAbsolute() );
//		List steps = locationPath.getSteps();
//
//		// go through steps backwards
//		LocationPathPattern path = answer;
//		boolean first = true;
//		for (ListIterator iter = steps.listIterator(steps.size()); iter
//				.hasPrevious();) {
//			Step step = (Step) iter.previous();
//			if (first) {
//				first = false;
//				path = convertStep(path, step);
//			} else {
//				if (navigationStep(step)) {
//					LocationPathPattern parent = new LocationPathPattern();
//					int axis = step.getAxis();
//					if (axis == Axis.DESCENDANT
//							|| axis == Axis.DESCENDANT_OR_SELF) {
//						path.setAncestorPattern(parent);
//					} else {
//						path.setParentPattern(parent);
//					}
//					path = parent;
//				}
//				path = convertStep(path, step);
//			}
//		}
//		if (locationPath.isAbsolute()) {
//			LocationPathPattern parent = new LocationPathPattern(
//					NodeTypeTest.DOCUMENT_TEST);
//			path.setParentPattern(parent);
//		}
//		return answer;
//	}
//
//	protected static LocationPathPattern convertStep(LocationPathPattern path,
//			Step step) throws JaxenException {
//		if (step instanceof DefaultAllNodeStep) {
//			int axis = step.getAxis();
//			if (axis == Axis.ATTRIBUTE) {
//				path.setNodeTest(NodeTypeTest.ATTRIBUTE_TEST);
//			} else {
//				path.setNodeTest(NodeTypeTest.ELEMENT_TEST);
//			}
//		} else if (step instanceof DefaultCommentNodeStep) {
//			path.setNodeTest(NodeTypeTest.COMMENT_TEST);
//		} else if (step instanceof DefaultProcessingInstructionNodeStep) {
//			path.setNodeTest(NodeTypeTest.PROCESSING_INSTRUCTION_TEST);
//		} else if (step instanceof DefaultTextNodeStep) {
//			path.setNodeTest(TextNodeTest.SINGLETON);
//		} else if (step instanceof DefaultCommentNodeStep) {
//			path.setNodeTest(NodeTypeTest.COMMENT_TEST);
//		} else if (step instanceof DefaultNameStep) {
//			DefaultNameStep nameStep = (DefaultNameStep) step;
//			String localName = nameStep.getLocalName();
//			String prefix = nameStep.getPrefix();
//			int axis = nameStep.getAxis();
//			short nodeType = Pattern.ELEMENT_NODE;
//			if (axis == Axis.ATTRIBUTE) {
//				nodeType = Pattern.ATTRIBUTE_NODE;
//			}
//			if (nameStep.isMatchesAnyName()) {
//				if (prefix.length() == 0 || prefix.equals("*")) {
//					if (axis == Axis.ATTRIBUTE) {
//						path.setNodeTest(NodeTypeTest.ATTRIBUTE_TEST);
//					} else {
//						path.setNodeTest(NodeTypeTest.ELEMENT_TEST);
//					}
//				} else {
//					path.setNodeTest(new NamespaceTest(prefix, nodeType));
//				}
//			} else {
//				path.setNodeTest(new NameTest(localName, nodeType));
//				// XXXX: should support namespace in the test too
//			}
//			return convertDefaultStep(path, nameStep);
//		} else if (step instanceof DefaultStep) {
//			return convertDefaultStep(path, (DefaultStep) step);
//		} else {
//			throw new JaxenException("Cannot convert: " + step
//					+ " to a Pattern");
//		}
//		return path;
//	}
//
//	protected static LocationPathPattern convertDefaultStep(
//			LocationPathPattern path, DefaultStep step) throws JaxenException {
//		List predicates = step.getPredicates();
//		if (!predicates.isEmpty()) {
//			FilterExpr filter = new DefaultFilterExpr(new PredicateSet());
//			for (Iterator iter = predicates.iterator(); iter.hasNext();) {
//				filter.addPredicate((Predicate) iter.next());
//			}
//			path.addFilter(filter);
//		}
//		return path;
//	}
//
//	protected static boolean navigationStep(Step step) {
//		if (step instanceof DefaultNameStep) {
//			return true;
//		} else if (step.getClass().equals(DefaultStep.class)) {
//			return !step.getPredicates().isEmpty();
//		} else {
//			return true;
//		}
//	}
//
//}

