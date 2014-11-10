package org.fxmisc.cssfx.impl.ui.threads;

import java.util.ArrayList;
import java.util.List;

import org.fxmisc.cssfx.impl.ui.model.JavaFXVM;
import org.fxmisc.cssfx.impl.ui.model.JavaFXVMModel;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class ExternalJavaFXThreadsDetector implements Runnable {
    private final JavaFXVMModel fxVMsModel;

    public ExternalJavaFXThreadsDetector(JavaFXVMModel fxVMsModel) {
        this.fxVMsModel = fxVMsModel;
    }
    
    @Override
    public void run() {
        List<JavaFXVM> runningVMs = new ArrayList<JavaFXVM>(10);
        
        while (!Thread.interrupted()) {
            runningVMs.clear();
            List<VirtualMachineDescriptor> vmds = VirtualMachine.list();
            int i=0;
            
            for (VirtualMachineDescriptor vmd : vmds) {
                final String vmID = vmd.id();
                final String vmName = vmd.displayName().split("\\s")[0];
                
                try {
                    VirtualMachine vm = VirtualMachine.attach(vmID);
                    if (vm.getSystemProperties().getProperty("javafx.version") != null) {
                        runningVMs.add(new JavaFXVM(vmID, vmName));
                    }
                    vm.detach();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            fxVMsModel.setRunningJVMs(runningVMs);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}