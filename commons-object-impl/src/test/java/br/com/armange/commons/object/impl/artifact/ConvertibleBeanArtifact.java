package br.com.armange.commons.object.impl.artifact;

import br.com.armange.commons.object.annotation.ConvertibleBean;

@ConvertibleBean
public class ConvertibleBeanArtifact {
    
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
