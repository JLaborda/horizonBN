package io.github.jlaborda.core.common.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import edu.cmu.tetrad.bayes.BayesBifRenderer;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

public class GraphExporter {
    /**
     * Exports a graph to the DOT format.
     *
     * @param graph The graph to export.
     * @return A string representation of the graph in DOT format.
     */
    public static void exportToDot(Graph graph, String filename) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
        out.println("digraph G {");

        for(Node node : graph.getNodes()) {
            out.println("  " + node.getName() + ";");
        }

        for(Edge edge : graph.getEdges()) {
            out.println("  " + Edges.getDirectedEdgeHead(edge).getName() + 
               " -> " + Edges.getDirectedEdgeTail(edge).getName() + ";");
        }
        out.println("}");
    }
}

    /**
     * Exports a graph to a file in xbif format.
     *
     * @param graph The graph to export.
     * @param filename The name of the file to write to.
     * @throws IOException If an I/O error occurs.
     */
     public static void exportToXBIF(Graph graph, String filename) throws IOException {

        // Render the graph as a BIF string
        BayesPm bayesPm = new BayesPm(graph);
        BayesIm bayesIm = new MlBayesIm(bayesPm);
        String bifString = BayesBifRenderer.render(bayesIm);

        // Write the BIF string to a file
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            out.println(bifString);
        }


     }

//     /**
//    * Returns a description of the classifier in XML BIF 0.3 format. See
//    * http://www-2.cs.cmu.edu/~fgcozman/Research/InterchangeFormat/ for details
//    * on XML BIF.
//    * 
//    * @return an XML BIF 0.3 description of the classifier as a string.
//    */
//   public String toXMLBIF03() {
//     if (m_Instances == null) {
//       return ("<!--No model built yet-->");
//     }

//     StringBuffer text = new StringBuffer();
//     text.append(getBIFHeader());
//     text.append("\n");
//     text.append("\n");
//     text.append("<BIF VERSION=\"0.3\">\n");
//     text.append("<NETWORK>\n");
//     text.append("<NAME>" + XMLNormalize(Utils.quote(m_Instances.relationName()))
//       + "</NAME>\n");
//     for (int iAttribute = 0; iAttribute < m_Instances.numAttributes(); iAttribute++) {
//       text.append("<VARIABLE TYPE=\"nature\">\n");
//       text.append("<NAME>"
//         + XMLNormalize(Utils.quote(m_Instances.attribute(iAttribute).name())) + "</NAME>\n");
//       for (int iValue = 0; iValue < m_Instances.attribute(iAttribute)
//         .numValues(); iValue++) {
//         text.append("<OUTCOME>"
//           + XMLNormalize(Utils.quote(m_Instances.attribute(iAttribute).value(iValue)))
//           + "</OUTCOME>\n");
//       }
//       text.append("</VARIABLE>\n");
//     }

//     for (int iAttribute = 0; iAttribute < m_Instances.numAttributes(); iAttribute++) {
//       text.append("<DEFINITION>\n");
//       text.append("<FOR>"
//         + XMLNormalize(Utils.quote(m_Instances.attribute(iAttribute).name())) + "</FOR>\n");
//       for (int iParent = 0; iParent < m_ParentSets[iAttribute].getNrOfParents(); iParent++) {
//         text
//           .append("<GIVEN>"
//             + XMLNormalize(Utils.quote(m_Instances.attribute(
//               m_ParentSets[iAttribute].getParent(iParent)).name()))
//             + "</GIVEN>\n");
//       }
//       text.append("<TABLE>\n");
//       for (int iParent = 0; iParent < m_ParentSets[iAttribute]
//         .getCardinalityOfParents(); iParent++) {
//         for (int iValue = 0; iValue < m_Instances.attribute(iAttribute)
//           .numValues(); iValue++) {
//           text.append(m_Distributions[iAttribute][iParent]
//             .getProbability(iValue));
//           text.append(' ');
//         }
//         text.append('\n');
//       }
//       text.append("</TABLE>\n");
//       text.append("</DEFINITION>\n");
//     }
//     text.append("</NETWORK>\n");
//     text.append("</BIF>\n");
//     return text.toString();
//   }




}
