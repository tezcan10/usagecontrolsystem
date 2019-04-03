/*
 * CNR - IIT (2015-2016)
 *
 * @authors Fabio Bindi and Filippo Lauria
 */
package it.cnr.iit.xacmlutilities;

/**
 * Creates the Attribute ID of an attribute object
 *
 * @author Fabio Bindi and Filippo Lauria
 */
public final class AttributeId {

    private String attributeId = "";

    /**
     * Constructor
     */
    public AttributeId() {}

    /**
     * Constructor: creates an attributeId from a string
     *
     * @param attributeId
     *          string containing the attributeId to set
     */
    public AttributeId( String attributeId ) {
        set( attributeId );
    }

    /**
     * Sets the value of the attribute ID
     *
     * @param attributeId
     *          string containing the attributeId to set
     */
    public void set( String attributeId ) {
        this.attributeId = attributeId;
    }

    @Override
    public String toString() {
        return attributeId;
    }

    /**
     * Compares this attribute ID with another
     *
     * @param attributeId
     *          attributeId to be compared
     * @return true if the attribute IDs are equals; false otherwise
     */
    @Override
    public boolean equals( Object o ) {
        if( o == this ) {
            return true;
        }
        if( !( o instanceof AttributeId ) ) {
            return false;
        }
        return attributeId.equalsIgnoreCase( ( (AttributeId) o ).toString() );
    }

    /**
     * Checks if the attribute ID is compliant to the LDAP RFC
     *
     * @return true if the attributeId is compliant, false otherwise
     */
    public boolean isLdapAttribute() {
        return attributeId.toLowerCase().contains(
            "https://www.internet2.edu/media/medialibrary/2013/09/04/internet2-mace-dir-eduperson-201203.html"
                .toLowerCase() )
                || attributeId.toLowerCase()
                    .contains( "http://www.ietf.org/rfc/rfc4519.txt".toLowerCase() );
    }

    /**
     * Gets the name of the attribute from the whole attributeId
     *
     * @return the attribute name
     */
    public String getSplittedAttribute() {
        String[] tokens = attributeId.split( "#" );
        if( tokens.length > 1 ) {
            return tokens[1];
        }
        return attributeId;
    }

    /**
     * Creates an attributeId compliant to the LDAP RFC from the attribute name
     *
     * @param suffix
     *          the attibute name
     */
    public void createLDAPAttributeId( String suffix ) {
        if( suffix.contains( "edu" ) ) {
            attributeId = "https://www.internet2.edu/media/medialibrary/2013/09/04/internet2-mace-dir-eduperson-201203.html#"
                    + suffix;
        } else {
            attributeId = "http://www.ietf.org/rfc/rfc4519.txt#" + suffix;
        }
    }

    @Override
    public AttributeId clone() throws CloneNotSupportedException {
        return new AttributeId( attributeId );
    }

}