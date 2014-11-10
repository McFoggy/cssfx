package org.fxmisc.cssfx.impl.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class JavaFXVMModel {
    private ObservableList<JavaFXVM> fxVMs = FXCollections.observableArrayList();
    
    public ObservableList<JavaFXVM> fxVMs() {
        return fxVMs;
    }
    
    public void setRunningJVMs(List<JavaFXVM> runningJVMs) {
        List<String> alreadyKnownVMIDs = Arrays.asList(fxVMs.stream().map((vm) -> vm.getId()).toArray(String[]::new));
        List<String> runningVMIDs = Arrays.asList(runningJVMs.stream().map((vm) -> vm.getId()).toArray(String[]::new));
        
        
        System.out.println("known VM IDs: " +  alreadyKnownVMIDs);
        System.out.println("running VM IDs: " +  runningVMIDs);
        
        ArrayList<String> toRemoveVMIDs = new ArrayList<String>(alreadyKnownVMIDs);
        toRemoveVMIDs.removeAll(runningVMIDs);
        removeDeleted(toRemoveVMIDs);
        
        ArrayList<String> newVMIDs = new ArrayList<String>(runningVMIDs);
        newVMIDs.removeAll(alreadyKnownVMIDs);
        // now runningVMIDs contains only the VMs that were not known before
        runningJVMs.stream().filter(vm -> newVMIDs.contains(vm.getId())).forEach(newVM -> fxVMs.add(newVM));
    }
    
    private void removeDeleted(List<String> deletedVMs) {
        for (Iterator<JavaFXVM> it = fxVMs.iterator(); it.hasNext();) {
            JavaFXVM javaFXVM = (JavaFXVM) it.next();
            if (deletedVMs.contains(javaFXVM.getId())) {
                // vm has been terminated
                it.remove();
            }
        }
    }
}
