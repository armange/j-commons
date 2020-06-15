package br.com.armange.commons.object.impl.artifact;

import java.math.BigDecimal;

public class BeanArtifact extends AbstractBeanArtifact {

    private String name;
    private long longCode;
    private BigDecimal bigDecimal;
    
    public String getName() {
        return name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public long getLongCode() {
        return longCode;
    }
    
    public void setLongCode(final long longCode) {
        this.longCode = longCode;
    }
    
    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }
    
    public void setBigDecimal(final BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }
}
