package entityGraph;

import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class entityGraph {
    public static int ENTITY_ONLY = 1;
    public static int KNOWN_DOMAIN_ENTITY_ONLY = 2;
    public UndirectedGraph<String, Integer> ug =
            new UndirectedSparseMultigraph<String, Integer>();
    //	public DirectedGraph<String, Integer> ug =
    //          new DirectedSparseMultigraph<String, Integer>();
    Set<String> _filteredEdge = new HashSet<String>();
    Set<String> _filteredHost = new HashSet<String>();
    Set<String> _isSubject = new HashSet<String>();

    public entityGraph(String path, int NODETYPE) throws IOException {
        System.out.println("Notice:\tStart biulding graph. ");
        init();
        FileInputStream is = new FileInputStream(path);
        NxParser nxp = new NxParser(is);
        //	readSubject(nxp, NODETYPE);
        //	is.close();
//		is = new FileInputStream(path);
//		NxParser nxp2 = new NxParser(is);
//		readEdge(nxp2);
        readEdge(nxp, NODETYPE);
        is.close();
//		deleteDependentNode();
        System.out.println("Notice:\tFinish biulding graph. ");
    }

    private void init() {
        //_filteredEdge.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

        _filteredHost.add("xmlns.com");
        _filteredHost.add("w3c.org");
        _filteredHost.add("w3.org");
        _filteredHost.add("cyc.com");
        _filteredHost.add("bklyn-genealogy-info.com");
        _filteredHost.add("wdl.org ");
        _filteredHost.add("time.com");
        //	_filteredHost.add("dbpedia.org");
    }

    private void deleteDependentNode() {
        Iterator<String> Vertex = ug.getVertices().iterator();
        while (Vertex.hasNext()) {
            String tmpnode = Vertex.next();
            if (null == ug.getIncidentEdges(tmpnode)) {
                ug.removeVertex(tmpnode);
            }
        }
        System.out.println("Notice:\t" + ug.getVertexCount() + " nodes after delete dependent nodes.");

    }

    //	private int readSubject(NxParser nxp, int NODETYPE)
//	{
//		System.out.println("Notice:\tReading subjects.");
//		int cnt = 0;
//		for (Node[] nxx : nxp) {
//			if(nxx.length!=4) continue;
//			//check subject format
//			
//			if(NODETYPE == ENTITY_ONLY && !checkFormat.isEntity(nxx[0].toString())){
//				continue;
//			}
//			else if(NODETYPE == KNOWN_DOMAIN_ENTITY_ONLY && !checkFormat.isKnownDomainEntity(nxx[0].toString())){
//				continue;
//			}
//			URI u = null;
//			try {
//				u = new URI(nxx[0].toString());
//			} catch (URISyntaxException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if(_filteredHost.contains(u.getHost())){
//				continue;
//			}
//			if (checkFormat.isUri(nxx[0].toString()) && !ug.containsVertex( nxx[0].toString() ) ) {// check type
//				ug.addVertex(nxx[0].toString());
//				cnt++;
//			}
//		}
//		System.out.println("Got "+cnt+" subjects.");
//		return cnt;
//	}
//	private int readEdge(NxParser nxp)
//	{
//		System.out.println("Notice:\tReading edges.");
//		int cnt = 0;
//		for(Node[] nxx : nxp ){
//			if(nxx.length != 4||
//					nxx[0].toString() == nxx[2].toString() || 
//					_filteredEdge.contains(nxx[1].toString())){
//				continue;
//			}
//			if(ug.containsVertex(nxx[0].toString())) {
//				if (ug.containsVertex(nxx[2].toString())) {
//					ug.addEdge(ug.getEdgeCount(), nxx[0].toString(), nxx[2].toString());
//					cnt++;
//				} 
///*				else if (checkFormat.isEntity(nxx[2].toString())) {
//					ug.addVertex(nxx[2].toString());
//					ug.addEdge(1, nxx[0].toString(), nxx[2].toString());
//					cnt++;
//				}*/
//			}
//		}
//		System.out.println("Got "+cnt+" edges.");
//		return cnt;
//	}
    public boolean isSubject(String s) {
        return _isSubject.contains(s);
    }

    private int readEdge(NxParser nxp, int NODETYPE) {
        System.out.println("Notice:\tReading edges.");

        int cnt_node = 0, cnt_edge = 0;
        for (Node[] nxx : nxp) {
            String sub = nxx[0].toString();
            String pred = nxx[1].toString();
            String obj = nxx[2].toString();

            if (nxx.length != 4) continue;
                //if(NODETYPE == ENTITY_ONLY && !checkFormat.isEntity(sub)){
                //	continue;
                //}
            else if (NODETYPE == KNOWN_DOMAIN_ENTITY_ONLY && !checkFormat.isKnownDomainEntity(sub)) {
                continue;
            }
            //	if (checkFormat.isUri(sub)) {// check type
            if (!ug.containsVertex(sub)) {
                ug.addVertex(sub);

                //if((!sub.contains("Category"))&&(!sub.contains("ontology"))&&(!sub.contains("/terms/"))){
                boolean abandon = false;
                Iterator<String> host = _filteredHost.iterator();
                while (host.hasNext()) {
                    if (sub.contains(host.next())) {
                        abandon = true;
                        break;
                    }
                    //	}
                    if (abandon == false) _isSubject.add(sub);
                }
                cnt_node++;
            }

            if (globleVaribles.PropAsNode) {
                if (!ug.containsVertex(pred)) {
                    ug.addVertex(pred);
                    cnt_node++;
                }
                ug.addEdge(cnt_edge, sub, pred);
                cnt_edge++;
            }
            if (checkFormat.isUri(obj)) {
                //if(pred.contains("sameAs"))continue;
                if (!ug.containsVertex(obj)) {
                    ug.addVertex(obj);
                    cnt_node++;
                }
                ug.addEdge(cnt_edge, sub, obj);
                cnt_edge++;
            }
            //}
            if (cnt_node > 100000) break;
        }
        System.out.println("Got " + cnt_node + " nodes, and " + cnt_edge + " edges for entity graph.");
        return 1;
    }

    public void addEdgeBetweenSeed(Set<String> seeds, int dist) {
        System.out.println("Notice:\tAdd edge between seeds.");
        Iterator<String> it1 = seeds.iterator();

        while (it1.hasNext()) {
            String node1 = it1.next();
            if (!ug.containsVertex(node1)) {

                System.out.println("Notice:\tNo vertex " + node1 + " in graph.\n");
                ug.addVertex(node1);
            }
            Iterator<String> it2 = seeds.iterator();
            while (it2.hasNext()) {
                String node2 = it2.next();
                if (!ug.containsVertex(node2)) {

                    System.out.println("Notice:\tNo vertex " + node2 + " in graph.\n");
                    ug.addVertex(node2);
                }
                if (node1 != node2 && null == ug.findEdge(node1, node2)) {
                    String prenode = node1;
                    for (int i = 1; i < dist; i++) {
                        String tmpnode = "node" + String.format("%d", ug.getVertexCount());
                        ug.addVertex(tmpnode);
                        ug.addEdge(ug.getEdgeCount(), prenode, tmpnode);
                        prenode = tmpnode;
                    }
                    ug.addEdge(ug.getEdgeCount(), prenode, node2);
                }
            }
        }
        System.out.println("Notice:\tAdd edge between seeds done.");
    }
}
