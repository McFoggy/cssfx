package org.fxmisc.cssfx.impl.ui.model;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;

public class JavaFXVM {
    private final ReadOnlyStringProperty id;
    private final ReadOnlyStringProperty name;
    
    public JavaFXVM(String id, String name) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
    }

    public ReadOnlyStringProperty id() {
        return id;
    }
    
    public String getId() {
        return id().get();
    }
    
    public ReadOnlyStringProperty name() {
        return name;
    }
    
    public String getName() {
        return name().get();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id.get() == null) ? 0 : id.get().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JavaFXVM other = (JavaFXVM) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
