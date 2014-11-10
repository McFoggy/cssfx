package org.fxmisc.cssfx.impl.agent;

/*
 * #%L
 * CSSFX
 * %%
 * Copyright (C) 2014 CSSFX by Matthieu Brouillard
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.lang.instrument.Instrumentation;

public class CSSFXAgent {
    private static Instrumentation instrumentation;
    
    public static void premain(String args, Instrumentation inst) throws Exception {
        System.out.println("premain method invoked with args: " + args + " and inst: " + inst);
        instrumentation = inst;
    }

    /**
     * JVM hook to dynamically load javaagent at runtime.
     * 
     * The agent class may have an agentmain method for use when the agent is
     * started after VM startup.
     * 
     * @param args
     * @param inst
     * @throws Exception
     */
    public static void agentmain(String args, Instrumentation inst) throws Exception {
        System.out.println("agentmain  method invoked with args: " + args + " and inst: " + inst);
        instrumentation = inst;
    }

    /**
     * Programmatic hook to dynamically load javaagent at runtime.
     */
    public static void initialize() {
        System.out.println("initialize called");
        if (instrumentation == null) {
            System.out.println("inst: " + instrumentation);
        }
    }
}
