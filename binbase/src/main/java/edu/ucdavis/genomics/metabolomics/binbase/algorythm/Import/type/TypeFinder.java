/*
 * Created on Nov 5, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.ucdavis.genomics.metabolomics.util.SQLObject;

/**
 * obtains the correct type for the given samplename
 * @author wohlgemuth
 * @version Nov 5, 2005
 *
 * @
 */
public class TypeFinder extends SQLObject{
    /**
     * give you all registered types from the database
     */
    private PreparedStatement getTypes;
    /**
     * @author wohlgemuth
     * @version Nov 5, 2005
     * 
     */
    public TypeFinder() {
        super();
    }
    
    /**
     * returns the internal type id of the matching sample pattern
     * @author wohlgemuth
     * @version Nov 5, 2005
     * @param samplename
     * @return
     */
    public int getType(String samplename){
        ResultSet result = null;
        try{
            result = getTypes.executeQuery();
            
            while(result.next()){
                String pattern = result.getString("pattern");
                
                boolean res = samplename.matches(pattern);
                
                if(res == true){
                    return result.getInt("id");
                }
            }
        }
        catch(Exception e){
            logger.error(e.getMessage(),e);
        }
        finally{
            try {
                result.close();
            } catch (SQLException e) {
                //i don't care can only be null so put it in the debug file
                logger.debug(e.getMessage());
            }
        }
        return 0;
    }

    protected void prepareStatements() throws Exception {
        this.getTypes = this.getConnection().prepareStatement(SQL_CONFIG.getValue(CLASS +
        ".type"));
    }

}
