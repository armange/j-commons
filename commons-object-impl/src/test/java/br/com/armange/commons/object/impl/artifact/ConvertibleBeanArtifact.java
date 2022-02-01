package br.com.armange.commons.object.impl.artifact;

import java.math.BigDecimal;

import br.com.armange.commons.object.api.typeconverter.annotation.ConvertibleBean;

@ConvertibleBean({BeanArtifact.class})
public class ConvertibleBeanArtifact {
    
    private String name;
    private Integer code;
    private long longCode;
    private BigDecimal bigCode;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public void setCode(final Integer code) {
        this.code = code;
    }
    
    public long getLongCode() {
        return longCode;
    }
    
    public void setLongCode(final long longCode) {
        this.longCode = longCode;
    }
    
    public BigDecimal getBigCode() {
        return bigCode;
    }
    
    public void setBigCode(final BigDecimal bigCode) {
        this.bigCode = bigCode;
    }
}
