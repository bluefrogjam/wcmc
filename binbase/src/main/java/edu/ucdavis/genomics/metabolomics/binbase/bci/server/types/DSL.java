package edu.ucdavis.genomics.metabolomics.binbase.bci.server.types;

import java.io.Serializable;

/**
 * a dsl calculation job
 * @author wohlgemuth
 *
 */
public class DSL implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "DSL{" +
                "id='" + id + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String content;

    /**
     * internal minix id
     */
    private String id = "no id set";

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
