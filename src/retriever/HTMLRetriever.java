/**
 * 
 */
package retriever;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Parantap Samajdar
 *
 */
public class HTMLRetriever {

	private String localURL;
	private Document doc;
	
	public HTMLRetriever(String URL) {
		
		localURL = URL;
		
		try {
			doc = Jsoup.connect(localURL).get();
		} catch (IOException e) {			
			e.printStackTrace();
		}		
	}
	
	public void getLinks() {
		String[] allLinks;
		Elements links = doc.select("a[href]");		
		for(Element link : links) {
			System.out.println(link.toString());
			System.out.println("\n");
		}		
	}
	
	public void getButtonss() {
		String[] allLinks;
		Elements buttons = doc.select("input");
		for(Element button : buttons) {
			System.out.println(button.toString());
			System.out.println("\n");
		}		
	}
}
