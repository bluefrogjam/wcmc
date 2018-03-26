/*
 * Created on Jun 16, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object;

import org.xml.sax.Attributes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ContentObject<Type>  extends FormatObject<Type>{

	private Map<String,Object> attachments;
	
	public ContentObject(Type value, Map<String,String> attributes) {
		super(value, attributes);
		Collection<String> storedKeys = new Vector<String>();
		storedKeys.add("graph");

		attachments = new HashMap<>();
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	public ContentObject(Type value) {
		super(value);
	}
	public ContentObject(Type value, Attributes a) {
		super(value,a);
	}
	
			
	public Map<String,Object> getAttachments() {
		return attachments;
	}

	public void setAttachments(Map<String,Object> attachments) {
		this.attachments = attachments;
	}
	
	
	public void addAttachment(String name, Object value){
		this.attachments.put(name, value);
	}
}

